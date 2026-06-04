package com.quna.rag.parser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 解析后的文档文本模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedDocument {
    private String filename;
    private String docType;
    private String text;
    private boolean markdownLike;
}
