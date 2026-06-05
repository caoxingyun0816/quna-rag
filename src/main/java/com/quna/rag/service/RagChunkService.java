package com.quna.rag.service;

import com.quna.rag.dto.request.RagChunkListRequest;
import com.quna.rag.dto.response.RagChunkResponse;
import com.quna.rag.entity.RagKnowledgeBase;
import com.quna.rag.mapper.RagDocumentChunkMapper;
import com.quna.rag.entity.RagDocumentChunk;
import com.quna.rag.vector.VectorClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文档切片服务，负责切片查询、切片删除和向量数据同步清理。
 */
@Service
public class RagChunkService {
    private final RagDocumentChunkMapper chunkMapper;
    private final RagKnowledgeBaseService knowledgeBaseService;
    private final VectorClient vectorClient;

    public RagChunkService(RagDocumentChunkMapper chunkMapper,
                           RagKnowledgeBaseService knowledgeBaseService,
                           VectorClient vectorClient) {
        this.chunkMapper = chunkMapper;
        this.knowledgeBaseService = knowledgeBaseService;
        this.vectorClient = vectorClient;
    }

    public List<RagChunkResponse> list(RagChunkListRequest request) {
        Long kbId = request == null ? null : request.getKbId();
        Long docId = request == null ? null : request.getDocId();
        String title = request == null ? null : request.getTitle();
        String keyword = request == null ? null : request.getKeyword();
        return chunkMapper.selectList(kbId, docId, StringUtils.trimToNull(title), StringUtils.trimToNull(keyword))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public boolean delete(Long chunkId) {
        RagDocumentChunk chunk = chunkMapper.selectById(chunkId);
        if (chunk == null) {
            return true;
        }
        if (StringUtils.isNotBlank(chunk.getVectorId())) {
            RagKnowledgeBase kb = knowledgeBaseService.require(chunk.getKbId());
            vectorClient.delete(kb.getKbCode(), List.of(chunk.getVectorId()));
        }
        chunkMapper.logicalDelete(chunkId);
        chunkMapper.refreshDocumentChunkCount(chunk.getDocId());
        return true;
    }

    private RagChunkResponse toResponse(RagDocumentChunk entity) {
        RagChunkResponse response = new RagChunkResponse();
        response.setId(entity.getId());
        response.setDocId(entity.getDocId());
        response.setKbId(entity.getKbId());
        response.setDocName(entity.getDocName());
        response.setDocType(entity.getDocType());
        response.setProjectCode(entity.getProjectCode());
        response.setBizModule(entity.getBizModule());
        response.setChunkIndex(entity.getChunkIndex());
        response.setTitle(entity.getTitle());
        response.setContent(entity.getContent());
        response.setKeywords(entity.getKeywords());
        response.setContentHash(entity.getContentHash());
        response.setCreateTime(entity.getCreateTime());
        return response;
    }
}
