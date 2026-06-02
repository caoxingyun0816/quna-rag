package com.quna.rag.springrag.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
/**
 * 关键词抽取器，从切片正文中提取英文标识符、接口路径和中文关键词，供全文索引检索使用。
 */

@Component
public class RagKeywordExtractor {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9_./\\-]{1,}|[\\u4e00-\\u9fa5]{2,}");
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]+");
    private static final Pattern QUOTED_CHINESE_PHRASE = Pattern.compile("[\"“”']([\\u4e00-\\u9fa5]{2,12})[\"“”']");
    private static final int MAX_KEYWORDS = 30;
    private static final List<String> DOMAIN_PHRASES = List.of(
            "强烈推荐", "不推荐", "推荐", "待定", "解析失败", "核心优势", "主要不足", "加分亮点", "潜在风险", "面试建议"
    );
    private static final Set<String> STOPWORDS = Set.of(
            "the", "and", "for", "with", "this", "that", "from", "into", "public", "private", "class",
            "return", "void", "string", "true", "false", "null", "text", "java", "json", "yaml", "xml",
            "项目", "文档", "标题", "正文", "集合", "模块", "说明", "配置", "最终", "通过", "访问"
    );

    public String extract(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        Matcher matcher = TOKEN_PATTERN.matcher(content);
        List<String> tokens = new ArrayList<>();
        tokens.addAll(phraseCandidates(content));
        while (matcher.find()) {
            String token = normalize(matcher.group());
            if (CHINESE_PATTERN.matcher(token).matches() && token.length() > 6) {
                tokens.addAll(chineseCandidates(token));
            } else if (isUseful(token)) {
                tokens.add(token);
            }
        }
        if (tokens.isEmpty()) {
            return "";
        }
        Map<String, Long> frequency = tokens.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Set<String> keywords = frequency.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(MAX_KEYWORDS)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return String.join(" ", keywords);
    }

    private List<String> phraseCandidates(String content) {
        List<String> candidates = new ArrayList<>();
        for (String phrase : DOMAIN_PHRASES) {
            if (content.contains(phrase)) {
                candidates.add(phrase);
            }
        }
        Matcher matcher = QUOTED_CHINESE_PHRASE.matcher(content);
        while (matcher.find()) {
            String phrase = matcher.group(1);
            if (isUseful(phrase)) {
                candidates.add(phrase);
            }
        }
        return candidates;
    }

    private List<String> chineseCandidates(String text) {
        List<String> candidates = new ArrayList<>();
        for (int size = 2; size <= 4; size++) {
            for (int i = 0; i + size <= text.length(); i++) {
                String token = text.substring(i, i + size);
                if (isUseful(token)) {
                    candidates.add(token);
                }
            }
        }
        return candidates;
    }

    private String normalize(String token) {
        return token == null ? "" : token.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isUseful(String token) {
        if (token.length() < 2 || STOPWORDS.contains(token)) {
            return false;
        }
        return token.chars().anyMatch(Character::isLetter);
    }
}
