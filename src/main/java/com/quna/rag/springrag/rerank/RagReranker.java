package com.quna.rag.springrag.rerank;

import com.quna.rag.springrag.model.RagHit;
import com.quna.rag.springrag.model.RagSearchRequest;

import java.util.List;

public interface RagReranker {
    List<RagHit> rerank(RagSearchRequest request, List<RagHit> candidates, int topK, double minScore);
}
