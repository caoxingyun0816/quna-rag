package com.quna.rag.entity;

import java.util.Arrays;

/**
 * 文档向量化状态枚举。
 */
public enum RagVectorStatus {
    WAITING(0, "WAITING", "待向量化"),
    RUNNING(1, "RUNNING", "向量化中"),
    SUCCESS(2, "SUCCESS", "向量化成功"),
    FAILED(3, "FAILED", "向量化失败");

    private final int code;
    private final String name;
    private final String label;

    RagVectorStatus(int code, String name, String label) {
        this.code = code;
        this.name = name;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public static RagVectorStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> code != null && item.code == code)
                .findFirst()
                .orElse(WAITING);
    }
}
