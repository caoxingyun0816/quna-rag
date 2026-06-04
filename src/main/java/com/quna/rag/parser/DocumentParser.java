package com.quna.rag.parser;

import com.quna.rag.springrag.model.ParsedDocument;
import org.springframework.web.multipart.MultipartFile;

/**
 * 标准文档解析器接口。
 */
public interface DocumentParser {
    boolean supports(String filename);

    ParsedDocument parse(MultipartFile file) throws Exception;
}
