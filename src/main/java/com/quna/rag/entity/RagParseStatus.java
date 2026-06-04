package com.quna.rag.entity;

import java.util.Arrays;

/**
 * 文档解析状态枚举。
 */
public enum RagParseStatus {
    WAITING(0, "WAITING", "待解析"),
    RUNNING(1, "RUNNING", "解析中"),
    SUCCESS(2, "SUCCESS", "解析成功"),
    FAILED(3, "FAILED", "解析失败");

    private final int code;
    private final String name;
    private final String label;

    RagParseStatus(int code, String name, String label) {
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

    public static RagParseStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> code != null && item.code == code)
                .findFirst()
                .orElse(WAITING);
    }
}
