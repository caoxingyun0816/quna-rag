package com.quna.rag.springrag.service;

import com.quna.rag.springrag.config.SpringRagProperties;
import com.quna.rag.springrag.model.RagHit;
import com.quna.rag.springrag.model.RagSearchRequest;
import com.quna.rag.springrag.rerank.RagReranker;
import com.quna.rag.springrag.retrieval.HybridRetriever;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Object> search(RagSearchRequest request) {
        requireQuestion(request);
        int topK = request.getRerankTopK() == null ? properties.getRerankTopK() : request.getRerankTopK();
        double minScore = request.getMinScore() == null ? properties.getMinScore() : request.getMinScore();
        List<RagHit> candidates = hybridRetriever.retrieve(request);
        List<RagHit> hits = reranker.rerank(request, candidates, topK, minScore);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", request.getQuestion());
        result.put("totalCandidates", candidates.size());
        result.put("total", hits.size());
        result.put("hits", hits);
        return result;
    }

    private void requireQuestion(RagSearchRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new IllegalArgumentException("question 不能为空");
        }
        request.setQuestion(request.getQuestion().trim());
    }
}
