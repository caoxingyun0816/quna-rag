package com.quna.rag.springrag.parser;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
