package com.quna.rag.springrag.splitter;

import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * 内存中的切片模型，表示切片序号、标题路径和正文内容。
 */

@Data
@AllArgsConstructor
public class RagChunk {
    private int index;
    private String titlePath;
    private String content;
}
