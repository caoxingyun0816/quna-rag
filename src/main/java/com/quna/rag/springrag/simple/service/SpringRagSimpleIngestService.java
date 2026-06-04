package com.quna.rag.springrag.simple.service;

import com.quna.rag.springrag.metadata.RagMetadataBuilder;
import com.quna.rag.springrag.model.ParsedDocument;
import com.quna.rag.springrag.model.RagChunkEntity;
import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.model.RagDocumentEntity;
import com.quna.rag.springrag.parser.DocumentParser;
import com.quna.rag.springrag.parser.DocumentParserResolver;
import com.quna.rag.springrag.parser.FileValidationService;
import com.quna.rag.springrag.service.RagEmbeddingTextBuilder;
import com.quna.rag.springrag.service.RagKeywordExtractor;
import com.quna.rag.springrag.service.RagOssStorageService;
import com.quna.rag.springrag.simple.model.SimpleUploadResult;
import com.quna.rag.springrag.simple.splitter.CharacterOverlapTextSplitter;
import com.quna.rag.springrag.splitter.RagChunk;
import com.quna.rag.springrag.store.RagChunkMapper;
import com.quna.rag.springrag.store.RagDocumentMapper;
import com.quna.rag.springrag.store.RagVectorStoreRouter;
import com.quna.rag.springrag.util.RagHashUtil;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 简单 RAG 上传服务：解析全文、字符切片、写 MySQL 和 Milvus。
 */
@Service
public class SpringRagSimpleIngestService {
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    private static final String STATUS_INDEXED = "INDEXED";

    private final DocumentParserResolver parserResolver;
    private final FileValidationService fileValidationService;
    private final RagOssStorageService ossStorageService;
    private final RagDocumentMapper documentMapper;
    private final RagChunkMapper chunkMapper;
    private final RagVectorStoreRouter vectorStoreRouter;
    private final RagMetadataBuilder metadataBuilder;
    private final RagKeywordExtractor keywordExtractor;
    private final RagEmbeddingTextBuilder embeddingTextBuilder;
    private final CharacterOverlapTextSplitter splitter;

    public SpringRagSimpleIngestService(DocumentParserResolver parserResolver,
                                        FileValidationService fileValidationService,
                                        RagOssStorageService ossStorageService,
                                        RagDocumentMapper documentMapper,
                                        RagChunkMapper chunkMapper,
                                        RagVectorStoreRouter vectorStoreRouter,
                                        RagMetadataBuilder metadataBuilder,
                                        RagKeywordExtractor keywordExtractor,
                                        RagEmbeddingTextBuilder embeddingTextBuilder,
                                        CharacterOverlapTextSplitter splitter) {
        this.parserResolver = parserResolver;
        this.fileValidationService = fileValidationService;
        this.ossStorageService = ossStorageService;
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.vectorStoreRouter = vectorStoreRouter;
        this.metadataBuilder = metadataBuilder;
        this.keywordExtractor = keywordExtractor;
        this.embeddingTextBuilder = embeddingTextBuilder;
        this.splitter = splitter;
    }

    public SimpleUploadResult upload(MultipartFile file, String collectionCode, String project,
                                     String module, String docType, String tags,
                                     Integer chunkSize, Integer overlap) throws Exception {
        fileValidationService.validateFile(file, MAX_FILE_SIZE, "文件");
        String filename = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();

        RagCollectionType collectionType = RagCollectionType.fromCode(collectionCode);
        byte[] fileBytes = file.getBytes();
        String contentHash = RagHashUtil.sha256(fileBytes);
        RagDocumentEntity existing = documentMapper.selectByCollectionAndContentHash(collectionType.getCode(), contentHash);
        if (existing != null) {
            return new SimpleUploadResult(existing.getId(), existing.getCollectionCode(), existing.getChunkCount(),
                    existing.getStatus(), true, existing.getContentHash(), existing.getFileUrl());
        }

        RagOssStorageService.StoredObject storedObject = ossStorageService.uploadToOss(project, filename, fileBytes);
        DocumentParser parser = parserResolver.resolve(filename);
        ParsedDocument parsedDocument = parser.parse(file);
        String content = value(parsedDocument.getText());
        if (content.isBlank()) {
            throw new IllegalArgumentException("无法从文件中提取有效文本内容");
        }

        RagDocumentEntity document = new RagDocumentEntity();
        document.setCollectionCode(collectionType.getCode());
        document.setFilename(parsedDocument.getFilename());
        document.setFileType(parsedDocument.getFileType());
        document.setSource("SIMPLE_UPLOAD");
        document.setProject(value(project));
        document.setModule(value(module));
        document.setDocType(value(docType));
        document.setTags(value(tags));
        document.setPermissionScope("ALL");
        document.setVisibleRoles("");
        document.setStatus("BUILDING");
        document.setChunkCount(0);
        document.setFileSize(file.getSize());
        document.setFileUrl(storedObject.fileUrl());
        document.setContentHash(contentHash);
        documentMapper.insert(document);

        int chunkCount = insertChunksAndVectors(document, parsedDocument, collectionType, chunkSize, overlap);
        documentMapper.updateStatusAndChunkCount(document.getId(), STATUS_INDEXED, chunkCount);
        return new SimpleUploadResult(document.getId(), collectionType.getCode(), chunkCount, STATUS_INDEXED,
                false, contentHash, storedObject.fileUrl());
    }

    private int insertChunksAndVectors(RagDocumentEntity document, ParsedDocument parsedDocument,
                                       RagCollectionType collectionType, Integer chunkSize, Integer overlap) {
        Map<String, Object> docMetadata = metadataBuilder.documentMetadata(collectionType, parsedDocument,
                document.getProject(), document.getModule(), document.getDocType(), document.getTags());
        List<RagChunk> chunks = splitter.split(parsedDocument.getText(), chunkSize, overlap);
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
            entity.setTitlePath(chunk.getTitlePath());
            entity.setContent(chunkContent);
            entity.setContentHash(RagHashUtil.sha256(chunkContent));
            entity.setKeywords(keywordExtractor.extract(chunkContent));
            chunkMapper.insert(entity);

            Map<String, Object> chunkMetadata = metadataBuilder.chunkMetadata(docMetadata, document.getId(),
                    entity.getId(), chunk.getIndex(), entity.getTitlePath());
            String embeddingText = embeddingTextBuilder.build(entity.getTitlePath(), chunkContent, entity.getKeywords());
            vectorDocuments.add(new Document(vectorDocumentId(entity.getId()), embeddingText, metadataBuilder.sanitize(chunkMetadata)));
        }
        if (!vectorDocuments.isEmpty()) {
            vectorStoreRouter.get(collectionType).add(vectorDocuments);
        }
        return vectorDocuments.size();
    }

    private void validateContentType(String contentType, String fileName) {
        fileValidationService.validateContentType(
                contentType,
                fileName,
                fileValidationService::isKnowledgeBaseMimeType,
                fileValidationService::isMarkdownExtension,
                "不支持的文件类型: " + contentType + "，支持的类型：PDF、DOCX、DOC、TXT、MD等"
        );
    }

    private String vectorDocumentId(Long chunkId) {
        return "rag-chunk-" + chunkId;
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }
}
