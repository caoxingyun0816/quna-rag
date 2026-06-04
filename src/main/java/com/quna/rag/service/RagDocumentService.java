package com.quna.rag.service;

import com.alibaba.fastjson.JSON;
import com.quna.rag.common.QunaRuntimeException;
import com.quna.rag.dto.request.RagDocumentListRequest;
import com.quna.rag.dto.request.RagDocumentUploadRequest;
import com.quna.rag.dto.response.RagDocumentResponse;
import com.quna.rag.dto.response.RagDocumentUploadResponse;
import com.quna.rag.mapper.RagDocumentChunkMapper;
import com.quna.rag.mapper.RagDocumentMapper;
import com.quna.rag.entity.RagDocument;
import com.quna.rag.entity.RagDocumentChunk;
import com.quna.rag.entity.RagKnowledgeBase;
import com.quna.rag.entity.RagParseStatus;
import com.quna.rag.entity.RagVectorStatus;
import com.quna.rag.parser.ParsedDocument;
import com.quna.rag.util.FileValidationUtil;
import com.quna.rag.util.RagHashUtil;
import com.quna.rag.vector.VectorClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文档管理服务，基于标准表完成上传、异步解析、切片入库、向量入库和删除。
 */
@Service
public class RagDocumentService {
    private static final ExecutorService INDEX_EXECUTOR = Executors.newFixedThreadPool(4);

    private final RagKnowledgeBaseService knowledgeBaseService;
    private final RagDocumentMapper documentMapper;
    private final RagDocumentChunkMapper chunkMapper;
    private final RagFileStorageService fileStorageService;
    private final RagParseService parseService;
    private final RagTextSplitter splitter;
    private final RagKeywordService keywordService;
    private final RagEmbeddingService embeddingService;
    private final VectorClient vectorClient;

    public RagDocumentService(RagKnowledgeBaseService knowledgeBaseService,
                              RagDocumentMapper documentMapper,
                              RagDocumentChunkMapper chunkMapper,
                              RagFileStorageService fileStorageService,
                              RagParseService parseService,
                              RagTextSplitter splitter,
                              RagKeywordService keywordService,
                              RagEmbeddingService embeddingService,
                              VectorClient vectorClient) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.fileStorageService = fileStorageService;
        this.parseService = parseService;
        this.splitter = splitter;
        this.keywordService = keywordService;
        this.embeddingService = embeddingService;
        this.vectorClient = vectorClient;
    }

    @Transactional
    public RagDocumentUploadResponse upload(MultipartFile file, RagDocumentUploadRequest request) throws Exception {

        FileValidationUtil.validateFile(file, FileValidationUtil.MAX_FILE_SIZE, "文件");
        if (request == null) {
            throw new QunaRuntimeException("参数错误");
        }

        RagKnowledgeBase kb = knowledgeBaseService.require(request.getKbId());
        byte[] bytes = file.getBytes();
        String fileHash = RagHashUtil.sha256(bytes);
        RagDocument existing = documentMapper.selectByKbAndFileMd5(kb.getId(), fileHash);
        if (existing != null) {
            throw new QunaRuntimeException("文件已存在");
        }

        String filename = StringUtils.defaultIfBlank(file.getOriginalFilename(), "unknown");
        RagFileStorageService.StoredFile storedFile = fileStorageService.upload(request.getProjectCode(), filename, bytes);
        RagDocument document = new RagDocument();
        document.setKbId(kb.getId());
        document.setDocCode("DOC-" + UUID.randomUUID().toString().replace("-", ""));
        document.setDocName(filename);
        document.setDocType(extension(filename));
        document.setBizModule(StringUtils.trimToEmpty(request.getBizModule()));
        document.setProjectCode(StringUtils.trimToEmpty(request.getProjectCode()));
        document.setSourceType(1);
        document.setFileUrl(storedFile.fileUrl());
        document.setFileSize(file.getSize());
        document.setFileMd5(fileHash);
        document.setParseStatus(RagParseStatus.RUNNING.getCode());
        document.setVectorStatus(RagVectorStatus.WAITING.getCode());
        document.setChunkCount(0);
        document.setVersionNo(1);
        document.setStatus(1);
        document.setIsDeleted(0);
        documentMapper.insert(document);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    CompletableFuture.runAsync(() -> buildDocument(document.getId()), INDEX_EXECUTOR);
                }
            });
        } else {
            CompletableFuture.runAsync(() -> buildDocument(document.getId()), INDEX_EXECUTOR);
        }

        return new RagDocumentUploadResponse(
                document.getId(),
                kb.getId(),
                kb.getKbCode(),
                "BUILDING",
                0,
                false,
                fileHash,
                storedFile.fileUrl()
        );
    }

    public void buildDocument(Long docId) {
        RagDocument document = documentMapper.selectById(docId);
        if (document == null) {
            return;
        }
        RagKnowledgeBase kb = knowledgeBaseService.require(document.getKbId());
        try {
            documentMapper.updateBuildStatus(docId, RagParseStatus.RUNNING.getCode(), RagVectorStatus.RUNNING.getCode(), null);
            MultipartFile storedFile = new StoredMultipartFile(fileStorageService.downloadToTemp(document.getFileUrl(), document.getDocName()), document.getDocName());
            ParsedDocument parsedDocument = parseService.parse(storedFile);
            List<RagTextSplitter.Chunk> chunks = splitter.split(parsedDocument);
            List<Document> vectorDocuments = new ArrayList<>();
            for (RagTextSplitter.Chunk chunk : chunks) {
                RagDocumentChunk entity = toChunkEntity(kb, document, chunk);
                chunkMapper.insert(entity);
                String vectorId = "rag-chunk-" + entity.getId();
                entity.setVectorId(vectorId);
                chunkMapper.updateVectorId(entity.getId(), vectorId);
                vectorDocuments.add(new Document(vectorId, embeddingService.buildEmbeddingText(entity.getTitle(), entity.getContent(), entity.getKeywords()),
                        metadata(kb, document, entity)));
            }
            if (!vectorDocuments.isEmpty()) {
                vectorClient.add(kb.getKbCode(), vectorDocuments);
            }
            documentMapper.updateIndexed(docId, RagParseStatus.SUCCESS.getCode(), RagVectorStatus.SUCCESS.getCode(), vectorDocuments.size(), null);
        } catch (Exception e) {
            documentMapper.updateBuildStatus(docId, RagParseStatus.FAILED.getCode(), RagVectorStatus.FAILED.getCode(), e.getMessage());
            throw new IllegalStateException("文档构建失败: " + e.getMessage(), e);
        }
    }

    public List<RagDocumentResponse> list(RagDocumentListRequest request) {
        Long kbId = request == null ? null : request.getKbId();
        String projectCode = request == null ? null : request.getProjectCode();
        String bizModule = request == null ? null : request.getBizModule();
        String docType = request == null ? null : request.getDocType();
        return documentMapper.selectList(kbId, projectCode, bizModule, docType).stream().map(this::toResponse).toList();
    }

    public boolean delete(Long docId) {
        RagDocument document = documentMapper.selectById(docId);
        if (document == null) {
            return true;
        }
        RagKnowledgeBase kb = knowledgeBaseService.require(document.getKbId());
        List<String> vectorIds = chunkMapper.selectByDocId(docId).stream()
                .map(RagDocumentChunk::getVectorId)
                .filter(StringUtils::isNotBlank)
                .toList();
        if (!vectorIds.isEmpty()) {
            vectorClient.delete(kb.getKbCode(), vectorIds);
        }
        chunkMapper.logicalDeleteByDocId(docId);
        documentMapper.logicalDelete(docId);
        return true;
    }

    private RagDocumentChunk toChunkEntity(RagKnowledgeBase kb, RagDocument document, RagTextSplitter.Chunk chunk) {
        String content = StringUtils.trimToEmpty(chunk.getContent());
        RagDocumentChunk entity = new RagDocumentChunk();
        entity.setKbId(kb.getId());
        entity.setDocId(document.getId());
        entity.setChunkCode("CHK-" + UUID.randomUUID().toString().replace("-", ""));
        entity.setChunkIndex(chunk.getIndex());
        entity.setTitle(StringUtils.left(StringUtils.trimToEmpty(chunk.getTitle()), 500));
        entity.setContent(content);
        entity.setSummary(StringUtils.left(content.replaceAll("\\s+", " "), 1000));
        entity.setTokenCount(content.length());
        entity.setContentHash(RagHashUtil.sha256(content));
        entity.setKeywords(keywordService.extract(content));
        entity.setStatus(1);
        entity.setIsDeleted(0);
        entity.setMetadata(JSON.toJSONString(metadata(kb, document, entity)));
        return entity;
    }

    private Map<String, Object> metadata(RagKnowledgeBase kb, RagDocument document, RagDocumentChunk chunk) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("kbId", kb.getId());
        metadata.put("kbCode", kb.getKbCode());
        metadata.put("docId", document.getId());
        metadata.put("chunkId", chunk.getId());
        metadata.put("chunkCode", chunk.getChunkCode());
        metadata.put("docName", document.getDocName());
        metadata.put("docType", document.getDocType());
        metadata.put("projectCode", document.getProjectCode());
        metadata.put("bizModule", document.getBizModule());
        metadata.put("title", chunk.getTitle());
        return metadata;
    }

    private RagDocumentResponse toResponse(RagDocument entity) {
        RagDocumentResponse response = new RagDocumentResponse();
        response.setId(entity.getId());
        response.setKbId(entity.getKbId());
        response.setDocName(entity.getDocName());
        response.setDocType(entity.getDocType());
        response.setProjectCode(entity.getProjectCode());
        response.setBizModule(entity.getBizModule());
        response.setStatus(statusText(entity));
        response.setChunkCount(entity.getChunkCount());
        response.setFileSize(entity.getFileSize());
        response.setFileUrl(entity.getFileUrl());
        response.setContentHash(entity.getFileMd5());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }

    private String statusText(RagDocument document) {
        RagParseStatus parseStatus = RagParseStatus.fromCode(document.getParseStatus());
        RagVectorStatus vectorStatus = RagVectorStatus.fromCode(document.getVectorStatus());
        if (parseStatus == RagParseStatus.FAILED || vectorStatus == RagVectorStatus.FAILED) {
            return "FAILED";
        }
        if (parseStatus == RagParseStatus.SUCCESS && vectorStatus == RagVectorStatus.SUCCESS) {
            return "INDEXED";
        }
        return "BUILDING";
    }

    private String extension(String filename) {
        int index = filename == null ? -1 : filename.lastIndexOf('.');
        return index < 0 ? "txt" : filename.substring(index + 1).toLowerCase();
    }

}
