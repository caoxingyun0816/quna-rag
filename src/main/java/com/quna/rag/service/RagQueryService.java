package com.quna.rag.service;

import com.quna.rag.dto.request.RagQueryRequest;
import com.quna.rag.dto.response.RagQueryResponse;
import com.quna.rag.dto.response.RagReferenceResponse;
import com.quna.rag.springrag.model.RagAskRequest;
import com.quna.rag.springrag.model.RagAskResult;
import com.quna.rag.springrag.model.RagHit;
import com.quna.rag.springrag.service.SpringRagAskService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG 问答服务，基于检索结果拼接上下文并调用大模型生成答案。
 */
@Service
public class RagQueryService {
    private final RagVectorSearchService vectorSearchService;
    private final SpringRagAskService askService;

    public RagQueryService(RagVectorSearchService vectorSearchService, SpringRagAskService askService) {
        this.vectorSearchService = vectorSearchService;
        this.askService = askService;
    }

    public RagQueryResponse ask(RagQueryRequest request) {
        RagAskRequest askRequest = new RagAskRequest();
        askRequest.setQuestion(request.getQuestion());
        askRequest.setCollectionCode(vectorSearchService.toSpringRequest(request).getCollectionCode());
        askRequest.setProject(request.getProjectCode());
        askRequest.setModule(request.getBizModule());
        askRequest.setDocType(request.getDocType());
        askRequest.setVectorTopK(request.getVectorTopK());
        askRequest.setKeywordTopK(request.getKeywordTopK());
        askRequest.setRerankTopK(request.getTopK());
        askRequest.setMinScore(request.getMinScore());
        askRequest.setIncludeSources(request.getIncludeSources());

        RagAskResult result = askService.ask(askRequest);
        RagQueryResponse response = new RagQueryResponse();
        response.setQuestion(result.getQuestion());
        response.setAnswer(result.getAnswer());
        List<RagHit> sources = result.getSources() == null ? List.of() : result.getSources();
        response.setTotalCandidates(sources.size());
        response.setTotal(sources.size());
        response.setReferences(sources.stream().map(this::toReference).toList());
        return response;
    }

    private RagReferenceResponse toReference(RagHit hit) {
        RagReferenceResponse response = new RagReferenceResponse();
        response.setDocId(hit.getDocId());
        response.setChunkId(hit.getChunkId());
        response.setDocName(hit.getFilename());
        response.setTitle(hit.getTitlePath());
        response.setScore(hit.getScore());
        response.setVectorScore(hit.getVectorScore());
        response.setKeywordScore(hit.getKeywordScore());
        response.setVectorHit(hit.isVectorHit());
        response.setKeywordHit(hit.isKeywordHit());
        return response;
    }
}
