package com.quna.rag.springrag.parser;

import com.quna.rag.springrag.model.ParsedDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Markdown 文档解析器，读取 .md/.markdown 文件并保留标题层级供后续语义切片使用。
 */
@Component
public class MarkdownDocumentParser extends AbstractTextDocumentParser {
    @Override
    public boolean supports(String filename) {
        String name = filename.toLowerCase();
        return name.endsWith(".md") || name.endsWith(".markdown");
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        return fromText(file, readUtf8(file), true);
    }
}
