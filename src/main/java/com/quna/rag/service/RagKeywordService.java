package com.quna.rag.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 关键词抽取服务，提取中文短语、英文标识符、接口路径和代码符号，服务于 MySQL 关键词检索。
 */
@Service
public class RagKeywordService {
    private static final Pattern TOKEN = Pattern.compile("[a-zA-Z][a-zA-Z0-9_./\\-]{1,}|/[a-zA-Z0-9_./{}\\-]+|[\\u4e00-\\u9fa5]{2,}");

    public String extract(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        Set<String> terms = new LinkedHashSet<>();
        Matcher matcher = TOKEN.matcher(text.toLowerCase());
        while (matcher.find() && terms.size() < 80) {
            String value = matcher.group();
            if (value.length() <= 80) {
                terms.add(value);
            }
        }
        return String.join(" ", terms);
    }
}
