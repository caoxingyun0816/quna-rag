package com.quna.rag.springrag.splitter;

import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.parser.ParsedDocument;

import java.util.List;
import java.util.Map;
/**
 * 文档切片接口，定义解析后的文档如何拆成可检索、可向量化的切片。
 */

public interface RagDocumentSplitter {
    List<RagChunk> split(ParsedDocument document, RagCollectionType collectionType, Map<String, Object> metadata);
}
