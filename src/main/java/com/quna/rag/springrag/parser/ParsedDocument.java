package com.quna.rag.springrag.parser;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedDocument {
    private String filename;
    private String fileType;
    private String title;
    private String text;
    private List<ParsedSection> sections = new ArrayList<>();
}
