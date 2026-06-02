package com.quna.rag.springrag.service;

import com.quna.rag.common.QunaRuntimeException;
import com.quna.rag.springrag.metadata.RagMetadataBuilder;
import com.quna.rag.springrag.model.*;
import com.quna.rag.springrag.parser.*;
import com.quna.rag.springrag.splitter.RagChunk;
import com.quna.rag.springrag.splitter.RagDocumentSplitter;
import com.quna.rag.springrag.store.RagChunkMapper;
import com.quna.rag.springrag.store.RagDocumentMapper;
import com.quna.rag.springrag.store.RagVectorStoreRouter;
import com.quna.rag.springrag.util.RagHashUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RAG 入库服务，串联文件校验、解析、切片、去重、数据库保存和向量库写入。
 */
@Service
@Slf4j
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
    private final RagOssStorageService ossStorageService;
    private static final ExecutorService BUILD_EXECUTOR = Executors.newFixedThreadPool(4);
    private static final String STATUS_BUILDING = "BUILDING";
    private static final String STATUS_INDEXED = "INDEXED";
    private static final String STATUS_FAILED = "FAILED";


    public SpringRagIngestService(DocumentParserResolver parserResolver,
                                  RagDocumentSplitter splitter,
                                  RagMetadataBuilder metadataBuilder,
                                  RagDocumentMapper documentMapper,
                                  RagChunkMapper chunkMapper,
                                  RagVectorStoreRouter vectorStoreRouter,
                                  RagKeywordExtractor keywordExtractor,
                                  RagEmbeddingTextBuilder embeddingTextBuilder,
                                  FileValidationService fileValidationService,
                                  RagOssStorageService ossStorageService) {
        this.parserResolver = parserResolver;
        this.splitter = splitter;
        this.metadataBuilder = metadataBuilder;
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.vectorStoreRouter = vectorStoreRouter;
        this.keywordExtractor = keywordExtractor;
        this.embeddingTextBuilder = embeddingTextBuilder;
        this.fileValidationService = fileValidationService;
        this.ossStorageService = ossStorageService;
    }

    public RagUploadResult upload(MultipartFile file, String collectionCode, String project,
                                  String module, String docType, String tags) throws Exception {

        // 校验文件
        fileValidationService.validateFile(file, FileValidationService.MAX_FILE_SIZE, "文件");

        RagCollectionType collectionType = RagCollectionType.fromCode(collectionCode);
        byte[] fileBytes = file.getBytes();
        String contentHash = RagHashUtil.sha256(fileBytes);
        RagDocumentEntity existing = documentMapper.selectByCollectionAndContentHash(collectionType.getCode(), contentHash);
        if (existing != null) {
            return new RagUploadResult(existing.getId(), existing.getCollectionCode(),
                    existing.getChunkCount(), existing.getStatus(), true, existing.getContentHash(), existing.getFileUrl());
        }

        // 上传 OSS
        String filename = file.getOriginalFilename();
        RagOssStorageService.StoredObject storedObject = ossStorageService.uploadToOss(project, filename, fileBytes);

        // 保存文档
        RagDocumentEntity document = new RagDocumentEntity();
        document.setCollectionCode(collectionType.getCode());
        document.setFilename(filename);
        document.setFileType(FileValidationService.extension(filename));
        document.setSource("UPLOAD");
        document.setProject(StringUtils.trimToEmpty(project));
        document.setModule(StringUtils.trimToEmpty(module));
        document.setDocType(StringUtils.trimToEmpty(docType));
        document.setTags(StringUtils.trimToEmpty(tags));
        document.setPermissionScope("ALL");
        document.setVisibleRoles("");
        document.setStatus(STATUS_BUILDING);
        document.setChunkCount(0);
        document.setFileSize(file.getSize());
        document.setFileUrl(storedObject.fileUrl());
        document.setContentHash(contentHash);
        documentMapper.insert(document);

        // 异步构建向量
        CompletableFuture.runAsync(() -> buildDocument(document.getId()), BUILD_EXECUTOR);
        return new RagUploadResult(document.getId(), collectionType.getCode(), 0, STATUS_BUILDING, false,
                contentHash, storedObject.fileUrl());
    }

    public void buildDocument(Long docId) {
        RagDocumentEntity document = documentMapper.selectById(docId);
        if (document == null) {
            log.error("文档不存在: docId={}", docId);
            return;
        }
        RagCollectionType collectionType = RagCollectionType.fromCode(document.getCollectionCode());
        try {
            Path filePath = ossStorageService.downloadToTemp(document.getFileUrl(), document.getFilename());
            MultipartFile storedFile = new StoredFileMultipartFile(filePath, document.getFilename());

            // 解析文件
            DocumentParser parser = parserResolver.resolve(document.getFilename());
            ParsedDocument parsedDocument = parser.parse(storedFile);
            String content = StringUtils.trimToEmpty(parsedDocument.getText());
            if (StringUtils.isBlank(content)) {
                log.error("SpringRAG 文档构建失败: docId={}, filename={}, error={}",
                        docId, document.getFilename(), "无法从文件中提取有效文本内容");
                throw new QunaRuntimeException("无法从文件中提取有效文本内容");
            }

            insertChunksAndVectors(document, parsedDocument, collectionType);
        } catch (Exception e) {
            log.error("SpringRAG 文档构建失败: docId={}, filename={}, error={}",
                    docId, document.getFilename(), e.getMessage(), e);
            documentMapper.updateStatus(docId, STATUS_FAILED);
        }
    }

    public void deleteDocument(Long docId) {
        RagDocumentEntity document = documentMapper.selectById(docId);
        if (document == null) {
            return;
        }
        deleteChunksAndVectors(document);
        documentMapper.deleteById(docId);
    }

    private void insertChunksAndVectors(RagDocumentEntity document, ParsedDocument parsedDocument,
                                        RagCollectionType collectionType) {
        Map<String, Object> docMetadata = metadataBuilder.documentMetadata(collectionType, parsedDocument,
                document.getProject(), document.getModule(), document.getDocType(), document.getTags());
        List<RagChunk> chunks = splitter.split(parsedDocument, collectionType, docMetadata);
        List<Document> vectorDocuments = new ArrayList<>();
        for (RagChunk chunk : chunks) {
            String chunkContent = StringUtils.trimToEmpty(chunk.getContent());
            if (chunkContent.isBlank()) {
                continue;
            }
            RagChunkEntity entity = new RagChunkEntity();
            entity.setDocId(document.getId());
            entity.setCollectionCode(collectionType.getCode());
            entity.setChunkIndex(chunk.getIndex());
            entity.setTitlePath(StringUtils.trimToEmpty(chunk.getTitlePath()));
            entity.setContent(chunkContent);
            entity.setContentHash(RagHashUtil.sha256(chunkContent));
            entity.setKeywords(keywordExtractor.extract(chunkContent));
            chunkMapper.insert(entity);

            Map<String, Object> chunkMetadata = metadataBuilder.chunkMetadata(docMetadata, document.getId(),
                    entity.getId(), chunk.getIndex(), entity.getTitlePath());
            String embeddingText = embeddingTextBuilder.build(entity.getTitlePath(), chunkContent, entity.getKeywords());
            vectorDocuments.add(new Document(vectorDocumentId(entity.getId()), embeddingText, metadataBuilder.sanitize(chunkMetadata)));
        }

        // 数据库保存原始切片，向量库保存面向语义检索的 embedding 文本，两边通过 chunkId/docId 对齐。
        if (!vectorDocuments.isEmpty()) {
            vectorStoreRouter.get(collectionType).add(vectorDocuments);
        }
        int chunkCount = vectorDocuments.size();
        documentMapper.updateStatusAndChunkCount(document.getId(), STATUS_INDEXED, chunkCount);
    }

    private void deleteChunksAndVectors(RagDocumentEntity document) {
        List<RagChunkEntity> chunks = chunkMapper.selectByDocId(document.getId());
        if (!chunks.isEmpty()) {
            List<String> vectorIds = chunks.stream()
                    .map(RagChunkEntity::getId)
                    .map(this::vectorDocumentId)
                    .toList();
            vectorStoreRouter.get(RagCollectionType.fromCode(document.getCollectionCode())).delete(vectorIds);
        }
        chunkMapper.deleteByDocId(document.getId());
    }

    private String vectorDocumentId(Long chunkId) {
        return "rag-chunk-" + chunkId;
    }

}
