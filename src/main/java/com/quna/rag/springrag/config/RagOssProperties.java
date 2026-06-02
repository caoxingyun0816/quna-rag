package com.quna.rag.springrag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OSS 配置，承接 application.yml 中 oss 前缀的文件上传参数。
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
    private String parentsFolderName = "manager";
}
