package com.quna.rag.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.quna.rag.springrag.config.RagOssProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author caoxingyun
 * @Date 2026/6/2 15:15
 */
@Configuration
public class OssConfig {
    private final RagOssProperties properties;

    public OssConfig(RagOssProperties properties) {
        this.properties = properties;
    }

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient() {
        return new OSSClientBuilder().build(
                properties.getEndPoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret()
        );
    }
}
