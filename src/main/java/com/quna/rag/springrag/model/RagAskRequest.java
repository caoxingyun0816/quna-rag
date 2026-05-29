package com.quna.rag.springrag.model;

import lombok.Data;
/**
 * 问答请求模型，复用检索请求字段，并额外控制是否返回引用来源。
 */

@Data
public class RagAskRequest extends RagSearchRequest {
    private Boolean includeSources = true;
}
