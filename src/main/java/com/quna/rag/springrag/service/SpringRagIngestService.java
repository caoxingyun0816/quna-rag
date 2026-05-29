package com.quna.rag.springrag.service;

import com.quna.rag.springrag.metadata.RagMetadataBuilder;
import com.quna.rag.springrag.model.RagChunkEntity;
import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.model.RagDocumentEntity;
import com.quna.rag.springrag.model.RagUploadResult;
import com.quna.rag.springrag.parser.*;
import com.quna.rag.springrag.splitter.RagChunk;
import com.quna.rag.springrag.splitter.RagDocumentSplitter;
import com.quna.rag.springrag.store.RagChunkMapper;
import com.quna.rag.springrag.store.RagDocumentMapper;
import com.quna.rag.springrag.store.RagVectorStoreRouter;
import com.quna.rag.springrag.util.RagHashUtil;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SpringRagIngestService {
    private final DocumentParserResolver parserResolver;
    private final RagDocumentSplitter splitter;
    private final RagMetadataBuilder metadataBuilder;
    private final RagDocumentMapper documentMapper;
    private final RagChunkMapper chunkMapper;
    private final RagVectorStoreRouter vectorStoreRouter;
    private final RagKeywordExtractor keywordExtractor;
    private final RagEmbeddingTextBuilder embeddingTextBuilder;
    private final FileValidationService fileValidationService;
    private final ContentTypeDetectionService contentTypeDetectionService;
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB


    public SpringRagIngestService(DocumentParserResolver parserResolver,
                                  RagDocumentSplitter splitter,
                                  RagMetadataBuilder metadataBuilder,
                                  RagDocumentMapper documentMapper,
                                  RagChunkMapper chunkMapper,
                                  RagVectorStoreRouter vectorStoreRouter,
                                  RagKeywordExtractor keywordExtractor,
                                  RagEmbeddingTextBuilder embeddingTextBuilder,
                                  FileValidationService fileValidationService,
                                  ContentTypeDetectionService contentTypeDetectionService) {
        this.parserResolver = parserResolver;
        this.splitter = splitter;
        this.metadataBuilder = metadataBuilder;
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.vectorStoreRouter = vectorStoreRouter;
        this.keywordExtractor = keywordExtractor;
        this.embeddingTextBuilder = embeddingTextBuilder;
        this.fileValidationService = fileValidationService;
        this.contentTypeDetectionService = contentTypeDetectionService;
    }

    @Transactional(rollbackFor = Exception.class)
    public RagUploadResult upload(MultipartFile file, String collectionCode, String project,
                                  String module, String docType, String tags) throws Exception {

        fileValidationService.validateFile(file, MAX_FILE_SIZE, "文件");

        // 2. 验证文件类型
        String contentType = contentTypeDetectionService.detectContentType(file);

        String filename = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        validateContentType(contentType, filename);

        RagCollectionType collectionType = RagCollectionType.fromCode(collectionCode);
        DocumentParser parser = parserResolver.resolve(filename);
        ParsedDocument parsedDocument = parser.parse(file);

        String content = value(parsedDocument.getText());
        if (content.isBlank()) {
            throw new IllegalArgumentException("无法从文件中提取有效文本内容");
        }
        String contentHash = RagHashUtil.sha256(content);
        RagDocumentEntity existing = documentMapper.selectByCollectionAndContentHash(collectionType.getCode(), contentHash);
        if (existing != null) {
            return new RagUploadResult(existing.getId(), existing.getCollectionCode(),
                    existing.getChunkCount(), existing.getStatus(), true, existing.getContentHash());
        }
        Map<String, Object> docMetadata = metadataBuilder.documentMetadata(collectionType, parsedDocument, project, module, docType, tags);

        RagDocumentEntity document = new RagDocumentEntity();
        document.setCollectionCode(collectionType.getCode());
        document.setFilename(parsedDocument.getFilename());
        document.setFileType(parsedDocument.getFileType());
        document.setSource("UPLOAD");
        document.setProject(value(project));
        document.setModule(value(module));
        document.setDocType(value(docType));
        document.setTags(value(tags));
        document.setPermissionScope("ALL");
        document.setVisibleRoles("");
        document.setStatus("INDEXING");
        document.setChunkCount(0);
        document.setFileSize(file.getSize());
        document.setContentHash(contentHash);
        documentMapper.insert(document);

        List<RagChunk> chunks = splitter.split(parsedDocument, collectionType, docMetadata);
        List<Document> vectorDocuments = new ArrayList<>();
        for (RagChunk chunk : chunks) {
            String chunkContent = value(chunk.getContent());
            if (chunkContent.isBlank()) {
                continue;
            }
            RagChunkEntity entity = new RagChunkEntity();
            entity.setDocId(document.getId());
            entity.setCollectionCode(collectionType.getCode());
            entity.setChunkIndex(chunk.getIndex());
            entity.setTitlePath(value(chunk.getTitlePath()));
            entity.setContent(chunkContent);
            entity.setContentHash(RagHashUtil.sha256(chunkContent));
            entity.setKeywords(keywordExtractor.extract(chunkContent));
            chunkMapper.insert(entity);

            Map<String, Object> chunkMetadata = metadataBuilder.chunkMetadata(docMetadata, document.getId(),
                    entity.getId(), chunk.getIndex(), entity.getTitlePath());
            String embeddingText = embeddingTextBuilder.build(entity.getTitlePath(), chunkContent, entity.getKeywords());
            vectorDocuments.add(new Document(embeddingText, metadataBuilder.sanitize(chunkMetadata)));
        }

        if (!vectorDocuments.isEmpty()) {
            vectorStoreRouter.get(collectionType).add(vectorDocuments);
        }
        int chunkCount = vectorDocuments.size();
        documentMapper.updateStatusAndChunkCount(document.getId(), "INDEXED", chunkCount);
        return new RagUploadResult(document.getId(), collectionType.getCode(), chunkCount, "INDEXED", false, contentHash);
    }

    /**
     * 验证文件类型
     */
    private void validateContentType(String contentType, String fileName) {
        fileValidationService.validateContentType(
                contentType,
                fileName,
                fileValidationService::isKnowledgeBaseMimeType,
                fileValidationService::isMarkdownExtension,
                "不支持的文件类型: " + contentType + "，支持的类型：PDF、DOCX、DOC、TXT、MD等"
        );
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }
}
