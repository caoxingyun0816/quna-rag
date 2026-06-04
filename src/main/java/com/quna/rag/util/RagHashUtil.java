package com.quna.rag.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * RAG hash 工具。
 */
public final class RagHashUtil {
    private RagHashUtil() {
    }

    public static String sha256(byte[] bytes) {
        return digest(bytes == null ? new byte[0] : bytes);
    }

    public static String sha256(String text) {
        return digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
    }

    private static String digest(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("计算 SHA-256 失败", e);
        }
    }
}
