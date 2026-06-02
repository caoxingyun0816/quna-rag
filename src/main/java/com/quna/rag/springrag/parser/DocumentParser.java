package com.quna.rag.springrag.parser;

import com.quna.rag.springrag.model.ParsedDocument;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档解析器统一接口，约定不同文件格式的 supports 判断和 parse 解析方法。
 */
public interface DocumentParser {

    /**
     * 判断文件格式是否支持
     * @param filename 文件名
     * @return 是否支持
     */
    boolean supports(String filename);

    /**
     * 解析文件
     * @param file 文件
     * @return 解析结果
     */
    ParsedDocument parse(MultipartFile file) throws Exception;

}
