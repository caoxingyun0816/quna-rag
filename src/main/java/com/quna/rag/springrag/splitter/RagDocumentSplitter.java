package com.quna.rag.springrag.splitter;

import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.parser.ParsedDocument;

import java.util.List;
import java.util.Map;

public interface RagDocumentSplitter {
    List<RagChunk> split(ParsedDocument document, RagCollectionType collectionType, Map<String, Object> metadata);
}
