package com.quna.rag.springrag.retrieval;

import com.quna.rag.springrag.config.SpringRagProperties;
import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.model.RagHit;
import com.quna.rag.springrag.model.RagSearchRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * 混合检索编排器，并行融合向量检索和关键词检索结果，并按切片 ID 去重。
 */

@Component
public class HybridRetriever {
    private final VectorRetriever vectorRetriever;
    private final KeywordRetriever keywordRetriever;
    private final SpringRagProperties properties;

    public HybridRetriever(VectorRetriever vectorRetriever, KeywordRetriever keywordRetriever, SpringRagProperties properties) {
        this.vectorRetriever = vectorRetriever;
        this.keywordRetriever = keywordRetriever;
        this.properties = properties;
    }

    public List<RagHit> retrieve(RagSearchRequest request) {
        Map<String, RagHit> merged = new LinkedHashMap<>();
        int vectorTopK = request.getVectorTopK() == null ? properties.getVectorTopK() : request.getVectorTopK();
        int keywordTopK = request.getKeywordTopK() == null ? properties.getKeywordTopK() : request.getKeywordTopK();

        // 一个请求可以查单个集合，也可以查全部集合；同一个 chunk 同时被两路召回时只保留一份。
        for (RagCollectionType collectionType : RagCollectionType.resolve(request.getCollectionCode())) {
            merge(merged, vectorRetriever.retrieve(collectionType, request, vectorTopK));
            merge(merged, keywordRetriever.retrieve(collectionType, request, keywordTopK));
        }
        return merged.values().stream().peek(this::score).toList();
    }

    private void merge(Map<String, RagHit> merged, List<RagHit> hits) {
        for (RagHit hit : hits) {
            String key = hit.getCollectionCode() + ":" + hit.getChunkId();
            RagHit existing = merged.get(key);
            if (existing == null) {
                merged.put(key, hit);
                continue;
            }
            existing.setVectorHit(existing.isVectorHit() || hit.isVectorHit());
            existing.setKeywordHit(existing.isKeywordHit() || hit.isKeywordHit());
            existing.setVectorScore(Math.max(existing.getVectorScore(), hit.getVectorScore()));
            existing.setKeywordScore(Math.max(existing.getKeywordScore(), hit.getKeywordScore()));
        }
    }

    private void score(RagHit hit) {
        // 向量分负责语义相关性，关键词分负责精确匹配；双路命中说明结果更稳，额外加一点奖励。
        double score = hit.getVectorScore() * 0.6 + normalizeKeywordScore(hit.getKeywordScore()) * 0.4;
        if (hit.isVectorHit() && hit.isKeywordHit()) {
            score += 0.1;
        }
        hit.setScore(score);
    }

    private double normalizeKeywordScore(double score) {
        if (score <= 0) return 0;
        return Math.min(1d, score / (score + 1d));
    }
}
