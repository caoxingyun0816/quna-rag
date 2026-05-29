package com.quna.rag.springrag.parser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParsedSection {
    private String titlePath;
    private String content;
}
