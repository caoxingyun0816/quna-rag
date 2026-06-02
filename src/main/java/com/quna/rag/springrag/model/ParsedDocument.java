package com.quna.rag.springrag.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析后的文档模型，保存文件名、类型、全文和按标题识别出的段落。
 */
@Data
public class ParsedDocument {
    private String filename;
    private String fileType;
    private String title;
    private String text;
    private List<ParsedSection> sections = new ArrayList<>();
}
