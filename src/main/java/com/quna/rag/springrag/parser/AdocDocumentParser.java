package com.quna.rag.springrag.parser;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
