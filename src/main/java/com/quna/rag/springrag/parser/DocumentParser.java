package com.quna.rag.springrag.parser;

import org.springframework.web.multipart.MultipartFile;
/**
 * 文档解析器统一接口，约定不同文件格式的 supports 判断和 parse 解析方法。
 */

public interface DocumentParser {
    boolean supports(String filename);

    ParsedDocument parse(MultipartFile file) throws Exception;
}
