package com.quna.rag;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.quna.rag.mapper")
public class QunaRagApplication {
    public static void main(String[] args) {
        SpringApplication.run(QunaRagApplication.class, args);
    }
}