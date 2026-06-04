package com.quna.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OSS 配置，对应 application.yml 中 oss 前缀。
 */
@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class RagOssProperties {
    private String imgUrl;
    private String fileUrl;
    private String endPoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String parentsFolderName;
}
