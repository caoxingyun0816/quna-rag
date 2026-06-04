package com.quna.rag.parser;

import com.quna.rag.springrag.model.ParsedDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Word 文档解析器，底层复用当前 SpringRAG Word 解析实现。
 */
@Component("standardWordDocumentParser")
public class WordDocumentParser implements DocumentParser {
    private final com.quna.rag.springrag.parser.WordDocumentParser delegate;

    public WordDocumentParser(com.quna.rag.springrag.parser.WordDocumentParser delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean supports(String filename) {
        return delegate.supports(filename);
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        return delegate.parse(file);
    }
}
