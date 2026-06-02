package com.quna.rag.springrag.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

/**
 * OSS mock 存储服务，当前先把上传文件落到本地目录，并生成稳定的 mock URL。
 */
@Service
public class RagMockOssStorageService {
    private static final String URL_PREFIX = "mock-oss://springrag/";

    private final Path rootPath;

    public RagMockOssStorageService(@Value("${rag.springrag.mock-oss-root:}") String configuredRoot) {
        String root = configuredRoot == null || configuredRoot.isBlank()
                ? System.getProperty("java.io.tmpdir") + "/quna-rag/mock-oss"
                : configuredRoot;
        this.rootPath = Path.of(root).toAbsolutePath().normalize();
    }

    public StoredObject store(String filename, byte[] bytes) throws IOException {
        String safeFilename = sanitizeFilename(filename);
        LocalDate now = LocalDate.now();
        String objectKey = now.getYear() + "/" + pad(now.getMonthValue()) + "/" + pad(now.getDayOfMonth())
                + "/" + UUID.randomUUID() + "/" + safeFilename;
        Path target = rootPath.resolve(objectKey).normalize();
        if (!target.startsWith(rootPath)) {
            throw new IOException("文件路径非法");
        }
        Files.createDirectories(target.getParent());
        Files.write(target, bytes == null ? new byte[0] : bytes);
        return new StoredObject(URL_PREFIX + objectKey, target);
    }

    public Path resolve(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(URL_PREFIX)) {
            throw new IllegalArgumentException("暂不支持的文件 URL: " + fileUrl);
        }
        Path target = rootPath.resolve(fileUrl.substring(URL_PREFIX.length())).normalize();
        if (!target.startsWith(rootPath)) {
            throw new IllegalArgumentException("文件 URL 路径非法");
        }
        return target;
    }

    private String sanitizeFilename(String filename) {
        String value = filename == null || filename.isBlank() ? "unknown" : filename;
        return value.replaceAll("[\\\\/:*?\"<>|\\r\\n]+", "_");
    }

    private String pad(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    public record StoredObject(String fileUrl, Path path) {
    }
}
