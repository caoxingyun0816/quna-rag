package com.quna.rag.springrag.model;

import lombok.Data;

@Data
public class RagAskRequest extends RagSearchRequest {
    private Boolean includeSources = true;
}
