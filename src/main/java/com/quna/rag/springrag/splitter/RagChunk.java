package com.quna.rag.springrag.splitter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RagChunk {
    private int index;
    private String titlePath;
    private String content;
}
