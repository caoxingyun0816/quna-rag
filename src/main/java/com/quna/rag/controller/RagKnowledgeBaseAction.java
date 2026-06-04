package com.quna.rag.controller;

import com.quna.rag.common.BasicAction;
import com.quna.rag.common.RestResponse;
import com.quna.rag.dto.request.RagKnowledgeBaseCreateRequest;
import com.quna.rag.entity.RagKnowledgeBase;
import com.quna.rag.service.RagKnowledgeBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RAG 知识库管理入口。
 */
@RestController
@RequestMapping("/api/rag/kb")
public class RagKnowledgeBaseAction extends BasicAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(RagKnowledgeBaseAction.class);

    private final RagKnowledgeBaseService knowledgeBaseService;

    public RagKnowledgeBaseAction(RagKnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @GetMapping("/list")
    public RestResponse<List<RagKnowledgeBase>> list() {
        return RestResponse.success(knowledgeBaseService.list());
    }

    @PostMapping("/create")
    public RestResponse<RagKnowledgeBase> create(@RequestBody RagKnowledgeBaseCreateRequest request) {
        return RestResponse.success(knowledgeBaseService.create(request));
    }
}
