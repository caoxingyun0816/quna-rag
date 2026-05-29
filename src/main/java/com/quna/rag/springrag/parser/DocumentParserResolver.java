package com.quna.rag.springrag.parser;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentParserResolver {
    private final List<DocumentParser> parsers;

    public DocumentParserResolver(List<DocumentParser> parsers) {
        this.parsers = parsers;
    }

    public DocumentParser resolve(String filename) {
        return parsers.stream()
                .filter(parser -> parser.supports(filename == null ? "" : filename))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("暂不支持该文件格式: " + filename));
    }
}
