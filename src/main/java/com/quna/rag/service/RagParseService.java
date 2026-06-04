package com.quna.rag.service;

import com.quna.rag.parser.DocumentParser;
import com.quna.rag.parser.DocumentParserFactory;
import com.quna.rag.parser.ParsedDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档解析服务，屏蔽 PDF、Word、Markdown、Excel 等不同文件类型的解析差异。
 */
@Service
public class RagParseService {
    private final DocumentParserFactory parserResolver;

    public RagParseService(DocumentParserFactory parserResolver) {
        this.parserResolver = parserResolver;
    }

    public ParsedDocument parse(MultipartFile file) throws Exception {
        DocumentParser parser = parserResolver.resolve(file.getOriginalFilename());
        return parser.parse(file);
    }
}
