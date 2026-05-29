package com.quna.rag.springrag.rerank;

import com.quna.rag.springrag.model.RagHit;
import com.quna.rag.springrag.model.RagSearchRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 基于规则的轻量重排器，结合标题、正文、项目模块匹配给候选结果追加相关性加分。
 */
@Component
public class RuleBasedReranker implements RagReranker {
    @Override
    public List<RagHit> rerank(RagSearchRequest request, List<RagHit> candidates, int topK, double minScore) {
        List<String> tokens = tokens(request.getQuestion());
        return candidates.stream()
                .peek(hit -> hit.setScore(hit.getScore() + bonus(hit, request, tokens)))
                .filter(hit -> hit.getScore() >= minScore)
                .sorted(Comparator.comparingDouble(RagHit::getScore).reversed())
                .limit(topK)
                .toList();
    }

    private double bonus(RagHit hit, RagSearchRequest request, List<String> tokens) {
        double bonus = 0d;
        String title = lower(hit.getTitlePath());
        String content = lower(hit.getContent());
        for (String token : tokens) {
            if (token.length() < 2) continue;
            if (title.contains(token)) bonus += 0.08;
            if (content.contains(token)) bonus += 0.03;
        }
        if (same(hit.getMetadata().get("project"), request.getProject())) bonus += 0.08;
        if (same(hit.getMetadata().get("module"), request.getModule())) bonus += 0.08;
        if (same(hit.getMetadata().get("docType"), request.getDocType())) bonus += 0.08;
        if (hit.isVectorHit() && hit.isKeywordHit()) bonus += 0.08;
        return Math.min(0.35, bonus);
    }

    private List<String> tokens(String question) {
        if (question == null) return List.of();
        return Arrays.stream(question.toLowerCase(Locale.ROOT).split("[^a-z0-9_./\\-\\u4e00-\\u9fa5]+"))
                .filter(item -> !item.isBlank())
                .toList();
    }

    private boolean same(Object left, String right) {
        return right != null && !right.isBlank() && String.valueOf(left).equalsIgnoreCase(right);
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
