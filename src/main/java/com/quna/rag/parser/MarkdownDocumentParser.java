package com.quna.rag.parser;

import com.quna.rag.springrag.model.ParsedDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Markdown 文档解析器，保留标题、代码块和表格结构。
 */
@Component("standardMarkdownDocumentParser")
public class MarkdownDocumentParser implements DocumentParser {
    private final com.quna.rag.springrag.parser.MarkdownDocumentParser delegate;

    public MarkdownDocumentParser(com.quna.rag.springrag.parser.MarkdownDocumentParser delegate) {
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
