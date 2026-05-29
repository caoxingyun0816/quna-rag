package com.quna.rag.springrag.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RagUploadResult {
    private Long docId;
    private String collectionCode;
    private Integer chunkCount;
    private String status;
    private Boolean duplicate;
    private String contentHash;
}
