package com.quna.rag.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.quna.rag.config.RagOssProperties;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

/**
 * RAG 原始文件存储服务，按 projectCode / 年 / 月 上传到 OSS。
 */
@Service
public class RagFileStorageService {
    private final RagOssProperties properties;
    private final OSS ossClient;

    public RagFileStorageService(RagOssProperties properties, OSS ossClient) {
        this.properties = properties;
        this.ossClient = ossClient;
    }

    public StoredFile upload(String projectCode, String filename, byte[] bytes) throws IOException {
        String objectKey = buildObjectKey(projectCode, filename);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes == null ? new byte[0] : bytes)) {
            ossClient.putObject(properties.getBucketName(), objectKey, inputStream);
        }
        return new StoredFile(publicUrl(objectKey), objectKey);
    }

    public Path downloadToTemp(String fileUrl, String filename) throws IOException {
        String objectKey = objectKeyFromUrl(fileUrl);
        Path tempFile = Files.createTempFile("rag-", suffix(filename));
        try (OSSObject object = ossClient.getObject(properties.getBucketName(), objectKey);
             InputStream inputStream = object.getObjectContent()) {
            Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

    private String buildObjectKey(String projectCode, String filename) {
        String safeProject = sanitize(projectCode == null || projectCode.isBlank() ? "default" : projectCode);
        LocalDate now = LocalDate.now();
        return trimSlash(properties.getParentsFolderName())
                + "/rag/" + safeProject
                + "/" + now.getYear()
                + "/" + pad(now.getMonthValue())
                + "/" + UUID.randomUUID() + "-" + sanitizeFilename(filename);
    }

    private String publicUrl(String objectKey) {
        String baseUrl = properties.getFileUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://" + properties.getBucketName() + "." + stripProtocol(properties.getEndPoint()) + "/";
        }
        return trimRightSlash(baseUrl) + "/" + objectKey;
    }

    private String objectKeyFromUrl(String fileUrl) {
        String baseUrl = trimRightSlash(properties.getFileUrl());
        if (fileUrl != null && !baseUrl.isBlank() && fileUrl.startsWith(baseUrl + "/")) {
            return fileUrl.substring(baseUrl.length() + 1);
        }
        String marker = "/" + trimSlash(properties.getParentsFolderName()) + "/rag/";
        int index = fileUrl == null ? -1 : fileUrl.indexOf(marker);
        if (index >= 0) {
            return fileUrl.substring(index + 1);
        }
        throw new IllegalArgumentException("无法解析 OSS 文件地址: " + fileUrl);
    }

    private void validateConfig() {
        if (isBlank(properties.getEndPoint()) || isBlank(properties.getAccessKeyId())
                || isBlank(properties.getAccessKeySecret()) || isBlank(properties.getBucketName())) {
            throw new IllegalStateException("OSS 配置不完整");
        }
    }

    private String suffix(String filename) {
        int index = filename == null ? -1 : filename.lastIndexOf('.');
        return index < 0 ? ".tmp" : filename.substring(index);
    }

    private String sanitizeFilename(String filename) {
        String value = filename == null || filename.isBlank() ? "unknown" : filename;
        return value.replaceAll("[\\\\/:*?\"<>|\\r\\n]+", "_");
    }

    private String sanitize(String value) {
        return value.trim().replaceAll("[^a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+", "_");
    }

    private String trimSlash(String value) {
        return value == null ? "" : value.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String trimRightSlash(String value) {
        return value == null ? "" : value.replaceAll("/+$", "");
    }

    private String stripProtocol(String value) {
        return value == null ? "" : value.replaceFirst("^https?://", "");
    }

    private String pad(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record StoredFile(String fileUrl, String objectKey) {
    }
}
