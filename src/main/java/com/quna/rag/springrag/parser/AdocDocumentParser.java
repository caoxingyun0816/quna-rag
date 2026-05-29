package com.quna.rag.springrag.parser;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
/**
 * AsciiDoc 文档解析器，将 .adoc/.asciidoc 文件按文本方式读取并保留标题结构。
 */

@Component
public class AdocDocumentParser extends AbstractTextDocumentParser {
    @Override
    public boolean supports(String filename) {
        return filename.toLowerCase().endsWith(".adoc");
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        return fromText(file, readUtf8(file), true);
    }
}
