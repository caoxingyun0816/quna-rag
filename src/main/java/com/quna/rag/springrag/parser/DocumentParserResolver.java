package com.quna.rag.springrag.parser;

import com.quna.rag.common.QunaRuntimeException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档解析器选择器，根据上传文件名从多个解析器中找到最合适的实现。
 */
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
                .orElseThrow(() -> new QunaRuntimeException("暂不支持该文件格式: " + filename));
    }
}
