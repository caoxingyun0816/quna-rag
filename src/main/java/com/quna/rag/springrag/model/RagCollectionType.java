package com.quna.rag.springrag.model;

import java.util.Arrays;
import java.util.List;

public enum RagCollectionType {
    BUSINESS_DOC("business_doc", "rag_business_doc", "业务文档"),
    TECH_DOC("tech_doc", "rag_tech_doc", "技术文档");

    private final String code;
    private final String milvusCollection;
    private final String label;

    RagCollectionType(String code, String milvusCollection, String label) {
        this.code = code;
        this.milvusCollection = milvusCollection;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getMilvusCollection() {
        return milvusCollection;
    }

    public String getLabel() {
        return label;
    }

    public static RagCollectionType fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的集合类型: " + code));
    }

    public static List<RagCollectionType> resolve(String code) {
        if (code == null || code.isBlank() || "all".equalsIgnoreCase(code)) {
            return List.of(BUSINESS_DOC, TECH_DOC);
        }
        return List.of(fromCode(code));
    }
}
