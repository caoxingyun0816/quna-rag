package com.quna.rag.springrag.model;

import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * 解析阶段的段落模型，表示一个标题路径及其对应正文。
 */

@Data
@AllArgsConstructor
public class ParsedSection {
    private String titlePath;
    private String content;
}
