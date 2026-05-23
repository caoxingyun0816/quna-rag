package com.quna.rag.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatHistory {
    private Long id;
    private Long userId;
    private String question;
    private String answer;
    private LocalDateTime createTime;
}