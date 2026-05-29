package com.quna.rag.springrag.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
/**
 * 哈希工具类，统一计算文档和切片内容的 SHA-256，用于去重和一致性判断。
 */

public final class RagHashUtil {
    private RagHashUtil() {
    }

    public static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : encoded) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("计算 SHA-256 失败", e);
        }
    }
}
