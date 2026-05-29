package com.quna.rag.springrag.parser;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentParser {
    boolean supports(String filename);

    ParsedDocument parse(MultipartFile file) throws Exception;
}
