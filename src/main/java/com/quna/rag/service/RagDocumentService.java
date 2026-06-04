package com.quna.rag.service;

import com.quna.rag.dto.request.RagDocumentListRequest;
import com.quna.rag.dto.request.RagDocumentUploadRequest;
import com.quna.rag.dto.response.RagDocumentResponse;
import com.quna.rag.dto.response.RagDocumentUploadResponse;
import com.quna.rag.springrag.model.RagDocumentEntity;
import com.quna.rag.springrag.model.RagUploadResult;
import com.quna.rag.springrag.service.SpringRagIngestService;
import com.quna.rag.springrag.store.RagDocumentMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档管理服务，提供标准化上传、列表和删除能力。
 */
@Service
public class RagDocumentService {
    private final RagKnowledgeBaseService knowledgeBaseService;
    private final SpringRagIngestService ingestService;
    private final RagDocumentMapper documentMapper;

    public RagDocumentService(RagKnowledgeBaseService knowledgeBaseService,
                              SpringRagIngestService ingestService,
                              RagDocumentMapper documentMapper) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.ingestService = ingestService;
        this.documentMapper = documentMapper;
    }

    public RagDocumentUploadResponse upload(MultipartFile file, RagDocumentUploadRequest request) throws Exception {
        Long kbId = request == null ? null : request.getKbId();
        String collectionCode = knowledgeBaseService.collectionCode(kbId);
        String projectCode = request == null ? "" : request.getProjectCode();
        String bizModule = request == null ? "" : request.getBizModule();
        String tags = request == null ? "" : request.getTags();
        RagUploadResult result = ingestService.upload(file, collectionCode, projectCode, bizModule,
                inferDocType(file), tags);
        return new RagDocumentUploadResponse(result.getDocId(), knowledgeBaseService.kbId(result.getCollectionCode()),
                result.getCollectionCode(), result.getStatus(), result.getChunkCount(), result.getDuplicate(),
                result.getContentHash(), result.getFileUrl());
    }

    public List<RagDocumentResponse> list(RagDocumentListRequest request) {
        String collectionCode = request == null ? null : collectionCodeOrNull(request.getKbId());
        String projectCode = request == null ? null : request.getProjectCode();
        String bizModule = request == null ? null : request.getBizModule();
        String docType = request == null ? null : request.getDocType();
        return documentMapper.selectList(collectionCode, projectCode, bizModule, docType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public boolean delete(Long docId) {
        ingestService.deleteDocument(docId);
        return true;
    }

    private String collectionCodeOrNull(Long kbId) {
        return kbId == null ? null : knowledgeBaseService.collectionCode(kbId);
    }

    private String inferDocType(MultipartFile file) {
        String filename = file == null ? "" : file.getOriginalFilename();
        String ext = StringUtils.substringAfterLast(StringUtils.defaultString(filename), ".");
        return StringUtils.lowerCase(ext);
    }

    private RagDocumentResponse toResponse(RagDocumentEntity entity) {
        RagDocumentResponse response = new RagDocumentResponse();
        response.setId(entity.getId());
        response.setKbId(knowledgeBaseService.kbId(entity.getCollectionCode()));
        response.setKbCode(entity.getCollectionCode());
        response.setDocName(entity.getFilename());
        response.setDocType(entity.getDocType());
        response.setProjectCode(entity.getProject());
        response.setBizModule(entity.getModule());
        response.setTags(entity.getTags());
        response.setStatus(entity.getStatus());
        response.setChunkCount(entity.getChunkCount());
        response.setFileSize(entity.getFileSize());
        response.setFileUrl(entity.getFileUrl());
        response.setContentHash(entity.getContentHash());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }
}
