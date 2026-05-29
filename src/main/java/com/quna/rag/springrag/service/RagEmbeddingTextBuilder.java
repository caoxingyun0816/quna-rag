package com.quna.rag.springrag.service;

import com.quna.rag.springrag.config.SpringRagProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 向量化文本构建器，在原文过长时抽取标题、关键词、路由和字段，控制 Embedding 输入长度。
 */

@Component
public class RagEmbeddingTextBuilder {
    private static final Pattern ROUTE_PATTERN = Pattern.compile("`?(GET|POST|PUT|DELETE|PATCH)?\\s*(/[a-zA-Z0-9_./{}?=&\\-]+)`?");
    private static final Pattern TABLE_ROW_PATTERN = Pattern.compile("^\\|(.+)\\|$");
    private static final Pattern JSON_FIELD_PATTERN = Pattern.compile("\"([a-zA-Z_][a-zA-Z0-9_]*)\"\\s*:");

    private final SpringRagProperties properties;

    public RagEmbeddingTextBuilder(SpringRagProperties properties) {
        this.properties = properties;
    }

    public String build(String titlePath, String content, String keywords) {
        String text = value(content);
        int maxLength = Math.max(properties.getChunk().getEmbeddingMaxLength(), 1000);
        if (text.length() <= maxLength) {
            return withTitle(titlePath, text);
        }

        // Embedding 接口有输入长度限制，超长切片保留标题、关键词、接口路由和字段，再拼接首尾摘要。
        List<String> parts = new ArrayList<>();
        add(parts, "标题：" + value(titlePath));
        add(parts, "关键词：" + value(keywords));
        add(parts, "路由：" + String.join(" ", routes(text)));
        add(parts, "字段：" + String.join(" ", fields(text)));
        add(parts, "正文摘要：");
        add(parts, headTail(text, maxLength - length(parts) - 20));
        return limit(String.join("\n", parts), maxLength);
    }

    private String withTitle(String titlePath, String content) {
        if (value(titlePath).isBlank()) {
            return content;
        }
        return "标题：" + value(titlePath) + "\n\n" + content;
    }

    private Set<String> routes(String content) {
        Set<String> routes = new LinkedHashSet<>();
        Matcher matcher = ROUTE_PATTERN.matcher(content);
        while (matcher.find() && routes.size() < 20) {
            routes.add(matcher.group().replace("`", "").trim());
        }
        return routes;
    }

    private Set<String> fields(String content) {
        Set<String> fields = new LinkedHashSet<>();
        Matcher jsonMatcher = JSON_FIELD_PATTERN.matcher(content);
        while (jsonMatcher.find() && fields.size() < 80) {
            fields.add(jsonMatcher.group(1));
        }
        for (String line : content.split("\\R")) {
            Matcher rowMatcher = TABLE_ROW_PATTERN.matcher(line.trim());
            if (!rowMatcher.matches()) {
                continue;
            }
            String[] cells = rowMatcher.group(1).split("\\|");
            if (cells.length == 0) {
                continue;
            }
            String firstCell = cells[0].trim().replace("`", "");
            if (isFieldName(firstCell)) {
                fields.add(firstCell);
            }
            if (fields.size() >= 80) {
                break;
            }
        }
        return fields;
    }

    private boolean isFieldName(String value) {
        if (value.isBlank() || value.equals("---") || value.equals("参数名")) {
            return false;
        }
        return value.matches("[a-zA-Z_][a-zA-Z0-9_.]*|[\\u4e00-\\u9fa5a-zA-Z0-9_.]{2,}");
    }

    private String headTail(String text, int maxLength) {
        if (maxLength <= 0) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        int headLength = Math.max(maxLength * 2 / 3, 1);
        int tailLength = Math.max(maxLength - headLength - 20, 0);
        return text.substring(0, headLength) + "\n...\n" + text.substring(text.length() - tailLength);
    }

    private String limit(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private int length(List<String> parts) {
        return parts.stream().mapToInt(String::length).sum() + parts.size();
    }

    private void add(List<String> parts, String text) {
        if (!value(text).isBlank()) {
            parts.add(text);
        }
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }
}
