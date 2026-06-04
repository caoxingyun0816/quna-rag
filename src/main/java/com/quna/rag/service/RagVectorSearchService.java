package com.quna.rag.service;

import com.quna.rag.dto.request.RagQueryRequest;
import com.quna.rag.dto.response.RagSearchHitResponse;
import com.quna.rag.dto.response.RagSearchResponse;
import com.quna.rag.mapper.RagDocumentChunkMapper;
import com.quna.rag.entity.RagDocumentChunk;
import com.quna.rag.entity.RagKnowledgeBase;
import com.quna.rag.vector.VectorClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * RAG 检索服务，封装向量召回、关键词召回、合并去重和重排。
 */
@Service
public class RagVectorSearchService {
    private final RagKnowledgeBaseService knowledgeBaseService;
    private final RagDocumentChunkMapper chunkMapper;
    private final VectorClient vectorClient;
    private final RagRerankService rerankService;
    private final int defaultVectorTopK;
    private final int defaultKeywordTopK;
    private final int defaultRerankTopK;
    private final double defaultMinScore;

    public RagVectorSearchService(RagKnowledgeBaseService knowledgeBaseService,
                                  RagDocumentChunkMapper chunkMapper,
                                  VectorClient vectorClient,
                                  RagRerankService rerankService,
                                  @Value("${rag.config.vector-top-k:20}") int defaultVectorTopK,
                                  @Value("${rag.config.keyword-top-k:20}") int defaultKeywordTopK,
                                  @Value("${rag.config.rerank-top-k:5}") int defaultRerankTopK,
                                  @Value("${rag.config.min-score:0.0}") double defaultMinScore) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.chunkMapper = chunkMapper;
        this.vectorClient = vectorClient;
        this.rerankService = rerankService;
        this.defaultVectorTopK = defaultVectorTopK;
        this.defaultKeywordTopK = defaultKeywordTopK;
        this.defaultRerankTopK = defaultRerankTopK;
        this.defaultMinScore = defaultMinScore;
    }

    public RagSearchResponse search(RagQueryRequest request) {
        requireQuestion(request);
        RagKnowledgeBase kb = knowledgeBaseService.require(request.getKbId());
        Map<Long, RagSearchHitResponse> merged = new LinkedHashMap<>();
        vectorHits(kb, request).forEach(hit -> merge(merged, hit));
        keywordHits(request).forEach(hit -> merge(merged, hit));

        List<RagSearchHitResponse> candidates = merged.values().stream().peek(this::score).toList();
        int topK = request.getTopK() == null ? defaultRerankTopK : request.getTopK();
        double minScore = request.getMinScore() == null ? defaultMinScore : request.getMinScore();
        List<RagSearchHitResponse> hits = Boolean.FALSE.equals(request.getEnableRerank())
                ? candidates.stream().limit(topK).toList()
                : rerankService.rerank(candidates, topK, minScore);

        RagSearchResponse response = new RagSearchResponse();
        response.setQuestion(request.getQuestion());
        response.setTotalCandidates(candidates.size());
        response.setTotal(hits.size());
        response.setHits(hits);
        return response;
    }

    private List<RagSearchHitResponse> vectorHits(RagKnowledgeBase kb, RagQueryRequest request) {
        int topK = request.getVectorTopK() == null ? defaultVectorTopK : request.getVectorTopK();
        return vectorClient.search(kb.getKbCode(), request.getQuestion(), topK, filterExpression(request)).stream()
                .map(this::fromVectorDocument)
                .toList();
    }

    private List<RagSearchHitResponse> keywordHits(RagQueryRequest request) {
        int topK = request.getKeywordTopK() == null ? defaultKeywordTopK : request.getKeywordTopK();
        List<RagDocumentChunk> chunks = new ArrayList<>();
        try {
            chunks.addAll(chunkMapper.keywordSearch(request.getKbId(), request.getQuestion(), request.getProjectCode(),
                    request.getBizModule(), request.getDocType(), topK));
        } catch (Exception ignored) {
            // 未建 FULLTEXT 或分词不适配时继续走 LIKE，保证查询接口可用。
        }
        chunks.addAll(chunkMapper.likeSearch(request.getKbId(), request.getQuestion(), request.getProjectCode(),
                request.getBizModule(), request.getDocType(), topK));
        return chunks.stream().map(this::fromChunk).toList();
    }

    private RagSearchHitResponse fromVectorDocument(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        RagSearchHitResponse hit = new RagSearchHitResponse();
        hit.setChunkId(longValue(metadata.get("chunkId")));
        hit.setDocId(longValue(metadata.get("docId")));
        hit.setDocName(stringValue(metadata.get("docName")));
        hit.setDocType(stringValue(metadata.get("docType")));
        hit.setProjectCode(stringValue(metadata.get("projectCode")));
        hit.setBizModule(stringValue(metadata.get("bizModule")));
        hit.setTitle(stringValue(metadata.get("title")));
        hit.setContent(document.getText());
        hit.setVectorScore(document.getScore() == null ? 0d : document.getScore());
        hit.setScore(hit.getVectorScore());
        hit.setVectorHit(true);
        hit.setKeywordHit(false);
        return hit;
    }

    private RagSearchHitResponse fromChunk(RagDocumentChunk chunk) {
        RagSearchHitResponse hit = new RagSearchHitResponse();
        hit.setChunkId(chunk.getId());
        hit.setDocId(chunk.getDocId());
        hit.setDocName(chunk.getDocName());
        hit.setDocType(chunk.getDocType());
        hit.setProjectCode(chunk.getProjectCode());
        hit.setBizModule(chunk.getBizModule());
        hit.setTags(chunk.getTags());
        hit.setChunkIndex(chunk.getChunkIndex());
        hit.setTitle(chunk.getTitle());
        hit.setContent(chunk.getContent());
        hit.setKeywordScore(chunk.getKeywordScore() == null ? 1d : chunk.getKeywordScore());
        hit.setScore(hit.getKeywordScore());
        hit.setVectorHit(false);
        hit.setKeywordHit(true);
        return hit;
    }

    private void merge(Map<Long, RagSearchHitResponse> merged, RagSearchHitResponse hit) {
        if (hit.getChunkId() == null) {
            return;
        }
        RagSearchHitResponse existing = merged.get(hit.getChunkId());
        if (existing == null) {
            merged.put(hit.getChunkId(), hit);
            return;
        }
        existing.setVectorHit(Boolean.TRUE.equals(existing.getVectorHit()) || Boolean.TRUE.equals(hit.getVectorHit()));
        existing.setKeywordHit(Boolean.TRUE.equals(existing.getKeywordHit()) || Boolean.TRUE.equals(hit.getKeywordHit()));
        existing.setVectorScore(Math.max(nullToZero(existing.getVectorScore()), nullToZero(hit.getVectorScore())));
        existing.setKeywordScore(Math.max(nullToZero(existing.getKeywordScore()), nullToZero(hit.getKeywordScore())));
        if (Boolean.TRUE.equals(hit.getKeywordHit())) {
            existing.setContent(hit.getContent());
            existing.setTitle(hit.getTitle());
        }
    }

    private void score(RagSearchHitResponse hit) {
        double vectorScore = nullToZero(hit.getVectorScore());
        double keywordScore = normalizeKeyword(nullToZero(hit.getKeywordScore()));
        double score = vectorScore * 0.6 + keywordScore * 0.4;
        if (Boolean.TRUE.equals(hit.getVectorHit()) && Boolean.TRUE.equals(hit.getKeywordHit())) {
            score += 0.1;
        }
        hit.setScore(score);
    }

    private String filterExpression(RagQueryRequest request) {
        StringBuilder filter = new StringBuilder("kbId == " + request.getKbId());
        appendEquals(filter, "projectCode", request.getProjectCode());
        appendEquals(filter, "bizModule", request.getBizModule());
        appendEquals(filter, "docType", request.getDocType());
        return filter.toString();
    }

    private void appendEquals(StringBuilder filter, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        filter.append(" && ").append(key).append(" == '").append(value.replace("'", "\\'")).append("'");
    }

    private void requireQuestion(RagQueryRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new IllegalArgumentException("question 不能为空");
        }
        request.setQuestion(request.getQuestion().trim());
    }

    private double normalizeKeyword(double score) {
        return score <= 0 ? 0 : Math.min(1d, score / (score + 1d));
    }

    private double nullToZero(Double value) {
        return value == null ? 0d : value;
    }

    private Long longValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
