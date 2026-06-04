package com.quna.rag.service;

import com.quna.rag.dto.request.RagChunkListRequest;
import com.quna.rag.dto.response.RagChunkResponse;
import com.quna.rag.mapper.RagDocumentChunkMapper;
import com.quna.rag.entity.RagDocumentChunk;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文档切片服务，负责切片查询和后续切片管理能力。
 */
@Service
public class RagChunkService {
    private final RagDocumentChunkMapper chunkMapper;

    public RagChunkService(RagDocumentChunkMapper chunkMapper) {
        this.chunkMapper = chunkMapper;
    }

    public List<RagChunkResponse> list(RagChunkListRequest request) {
        Long docId = request == null ? null : request.getDocId();
        if (docId == null) {
            return List.of();
        }
        return chunkMapper.selectByDocId(docId).stream().map(this::toResponse).toList();
    }

    private RagChunkResponse toResponse(RagDocumentChunk entity) {
        RagChunkResponse response = new RagChunkResponse();
        response.setId(entity.getId());
        response.setDocId(entity.getDocId());
        response.setKbId(entity.getKbId());
        response.setChunkIndex(entity.getChunkIndex());
        response.setTitle(entity.getTitle());
        response.setContent(entity.getContent());
        response.setKeywords(entity.getKeywords());
        response.setContentHash(entity.getContentHash());
        response.setCreateTime(entity.getCreateTime());
        return response;
    }
}
