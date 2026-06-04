package com.quna.rag.parser;

import com.quna.rag.springrag.model.ParsedDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel 文档解析器，将 Sheet 内容转换为可检索文本。
 */
@Component("standardExcelDocumentParser")
public class ExcelDocumentParser implements DocumentParser {
    private final com.quna.rag.springrag.parser.ExcelDocumentParser delegate;

    public ExcelDocumentParser(com.quna.rag.springrag.parser.ExcelDocumentParser delegate) {
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
