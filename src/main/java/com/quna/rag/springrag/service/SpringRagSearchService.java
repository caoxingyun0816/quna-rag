package com.quna.rag.springrag.service;

import com.quna.rag.springrag.config.SpringRagProperties;
import com.quna.rag.springrag.model.RagHit;
import com.quna.rag.springrag.model.RagSearchRequest;
import com.quna.rag.springrag.model.RagSearchResult;
import com.quna.rag.springrag.rerank.RagReranker;
import com.quna.rag.springrag.retrieval.HybridRetriever;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG 检索服务，对外提供混合召回、重排、阈值过滤后的统一搜索结果。
 */
@Service
public class SpringRagSearchService {
    private final HybridRetriever hybridRetriever;
    private final RagReranker reranker;
    private final SpringRagProperties properties;

    public SpringRagSearchService(HybridRetriever hybridRetriever, RagReranker reranker, SpringRagProperties properties) {
        this.hybridRetriever = hybridRetriever;
        this.reranker = reranker;
        this.properties = properties;
    }

    public RagSearchResult search(RagSearchRequest request) {
        requireQuestion(request);
        int topK = request.getRerankTopK() == null ? properties.getRerankTopK() : request.getRerankTopK();
        double minScore = request.getMinScore() == null ? properties.getMinScore() : request.getMinScore();
        List<RagHit> candidates = hybridRetriever.retrieve(request);
        List<RagHit> hits = reranker.rerank(request, candidates, topK, minScore);
        return new RagSearchResult(request.getQuestion(), candidates.size(), hits.size(), hits);
    }

    private void requireQuestion(RagSearchRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new IllegalArgumentException("question 不能为空");
        }
        request.setQuestion(request.getQuestion().trim());
    }
}
