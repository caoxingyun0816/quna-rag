package com.quna.rag.service;

import com.quna.rag.dto.request.RagQueryRequest;
import com.quna.rag.dto.response.RagSearchHitResponse;
import com.quna.rag.dto.response.RagSearchResponse;
import com.quna.rag.springrag.model.RagHit;
import com.quna.rag.springrag.model.RagSearchRequest;
import com.quna.rag.springrag.model.RagSearchResult;
import com.quna.rag.springrag.service.SpringRagSearchService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG 检索服务，封装向量召回、关键词召回、合并去重和重排。
 */
@Service
public class RagVectorSearchService {
    private final RagKnowledgeBaseService knowledgeBaseService;
    private final SpringRagSearchService searchService;

    public RagVectorSearchService(RagKnowledgeBaseService knowledgeBaseService, SpringRagSearchService searchService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.searchService = searchService;
    }

    public RagSearchResponse search(RagQueryRequest request) {
        RagSearchResult result = searchService.search(toSpringRequest(request));
        RagSearchResponse response = new RagSearchResponse();
        response.setQuestion(result.getQuestion());
        response.setTotalCandidates(result.getTotalCandidates());
        response.setTotal(result.getTotal());
        response.setHits(result.getHits().stream().map(this::toHitResponse).toList());
        return response;
    }

    RagSearchRequest toSpringRequest(RagQueryRequest request) {
        RagSearchRequest springRequest = new RagSearchRequest();
        springRequest.setQuestion(request.getQuestion());
        springRequest.setCollectionCode(knowledgeBaseService.collectionCode(request.getKbId()));
        springRequest.setProject(request.getProjectCode());
        springRequest.setModule(request.getBizModule());
        springRequest.setDocType(request.getDocType());
        springRequest.setVectorTopK(request.getVectorTopK());
        springRequest.setKeywordTopK(request.getKeywordTopK());
        springRequest.setRerankTopK(request.getTopK());
        springRequest.setMinScore(request.getMinScore());
        return springRequest;
    }

    RagSearchHitResponse toHitResponse(RagHit hit) {
        RagSearchHitResponse response = new RagSearchHitResponse();
        response.setDocId(hit.getDocId());
        response.setChunkId(hit.getChunkId());
        response.setDocName(hit.getFilename());
        response.setDocType(hit.getDocType());
        response.setProjectCode(hit.getProject());
        response.setBizModule(hit.getModule());
        response.setTags(hit.getTags());
        response.setChunkIndex(hit.getChunkIndex());
        response.setTitle(hit.getTitlePath());
        response.setContent(hit.getContent());
        response.setScore(hit.getScore());
        response.setVectorScore(hit.getVectorScore());
        response.setKeywordScore(hit.getKeywordScore());
        response.setVectorHit(hit.isVectorHit());
        response.setKeywordHit(hit.isKeywordHit());
        return response;
    }

    List<RagSearchHitResponse> toHitResponses(List<RagHit> hits) {
        return hits.stream().map(this::toHitResponse).toList();
    }
}
