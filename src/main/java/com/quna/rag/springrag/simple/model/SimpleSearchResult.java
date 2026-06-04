package com.quna.rag.springrag.simple.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 简单 RAG 向量查询结果，直接返回向量库命中的切片列表。
 */
@Data
@AllArgsConstructor
public class SimpleSearchResult {
    private String question;
    private Integer total;
    private List<SimpleVectorHit> hits;
}
