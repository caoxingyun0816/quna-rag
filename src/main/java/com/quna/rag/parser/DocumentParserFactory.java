package com.quna.rag.parser;

import com.quna.rag.common.QunaRuntimeException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档解析器工厂，根据文件名选择合适解析器。
 */
@Component
public class DocumentParserFactory {
    private final List<DocumentParser> parsers;

    public DocumentParserFactory(List<DocumentParser> parsers) {
        this.parsers = parsers;
    }

    public DocumentParser resolve(String filename) {
        return parsers.stream()
                .filter(parser -> parser.supports(filename))
                .findFirst()
                .orElseThrow(() -> new QunaRuntimeException("暂不支持该文件类型: " + filename));
    }
}
