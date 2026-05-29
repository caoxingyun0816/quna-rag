package com.quna.rag.springrag.rerank;

import com.quna.rag.springrag.model.RagHit;
import com.quna.rag.springrag.model.RagSearchRequest;

import java.util.List;
/**
 * RAG 重排接口，定义将混合检索候选结果重新打分、过滤和截断的扩展点。
 */

public interface RagReranker {
    List<RagHit> rerank(RagSearchRequest request, List<RagHit> candidates, int topK, double minScore);
}
