package com.quna.rag.springrag.rerank;

import com.quna.rag.springrag.model.RagHit;
import com.quna.rag.springrag.model.RagSearchRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 基于规则的轻量重排器，结合标题、正文、项目模块匹配给候选结果追加相关性加分。
 */
@Component
public class RuleBasedReranker implements RagReranker {
    @Override
    public List<RagHit> rerank(RagSearchRequest request, List<RagHit> candidates, int topK, double minScore) {
        List<String> tokens = tokens(request.getQuestion());
        return candidates.stream()
                .peek(hit -> hit.setScore(score(hit, request, tokens)))
                .filter(hit -> hit.getScore() >= minScore)
                .sorted(Comparator.comparingDouble(RagHit::getScore).reversed())
                .limit(topK)
                .toList();
    }

    private double score(RagHit hit, RagSearchRequest request, List<String> tokens) {
        double score = hit.getScore() + bonus(hit, request, tokens);
        if (hit.isExactKeywordHit()) {
            score = Math.max(score, 0.75);
        }
        return score;
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
        if (same(hit.getProject(), request.getProject())) bonus += 0.08;
        if (same(hit.getModule(), request.getModule())) bonus += 0.08;
        if (same(hit.getDocType(), request.getDocType())) bonus += 0.08;
        if (hit.isVectorHit() && hit.isKeywordHit()) bonus += 0.08;
        return Math.min(0.35, bonus);
    }

    private List<String> tokens(String question) {
        if (question == null) return List.of();
        String text = question.toLowerCase(Locale.ROOT);
        Set<String> tokens = new LinkedHashSet<>();
        addIfContains(tokens, text, "候选", "候选人", "简历", "姓名", "学历", "工作年限");
        addIfContains(tokens, text, "简历", "候选人", "学历", "工作年限", "当前职位");
        addIfContains(tokens, text, "打分", "评分", "分数", "综合评分", "total_score", "score");
        addIfContains(tokens, text, "评分", "打分", "分数", "综合评分", "total_score", "score");
        addIfContains(tokens, text, "规则", "必要条件评分", "加分项", "扣分项", "等级", "grade", "label");
        addIfContains(tokens, text, "推荐", "强烈推荐", "不推荐", "recommend", "reject", "pending");
        tokens.addAll(Arrays.stream(text.split("[^a-z0-9_./\\-\\u4e00-\\u9fa5]+"))
                .filter(item -> !item.isBlank())
                .toList());
        return new ArrayList<>(tokens);
    }

    private void addIfContains(Set<String> tokens, String query, String trigger, String... values) {
        if (!query.contains(trigger)) {
            return;
        }
        tokens.add(trigger);
        tokens.addAll(Arrays.asList(values));
    }

    private boolean same(Object left, String right) {
        return right != null && !right.isBlank() && String.valueOf(left).equalsIgnoreCase(right);
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
