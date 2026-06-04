package com.quna.rag.service;

import com.quna.rag.dto.response.RagSearchHitResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 规则重排服务，第一版按融合分排序。
 */
@Service
public class RagRerankService {
    public List<RagSearchHitResponse> rerank(List<RagSearchHitResponse> hits, int topK, double minScore) {
        return hits.stream()
                .filter(hit -> hit.getScore() == null || hit.getScore() >= minScore)
                .sorted(Comparator.comparing(RagSearchHitResponse::getScore, Comparator.nullsLast(Double::compareTo)).reversed())
                .limit(topK)
                .toList();
    }
}
