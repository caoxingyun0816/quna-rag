package com.quna.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "sso.config")
public class QunaSsoAuthProperties {
    private boolean enabled = true;
    private List<String> permitUrls = new ArrayList<>();
}
