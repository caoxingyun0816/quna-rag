package com.quna.rag.controller;

import com.quna.rag.common.BasicAction;
import com.quna.rag.common.RestResponse;
import com.quna.rag.dto.request.RagQueryRequest;
import com.quna.rag.dto.response.RagQueryResponse;
import com.quna.rag.dto.response.RagSearchResponse;
import com.quna.rag.service.RagQueryService;
import com.quna.rag.service.RagVectorSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * RAG 查询入口，提供检索测试和问答增强两个接口。
 */
@RestController
@RequestMapping("/api/rag/query")
public class RagQueryAction extends BasicAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(RagQueryAction.class);

    private final RagVectorSearchService vectorSearchService;
    private final RagQueryService queryService;

    public RagQueryAction(RagVectorSearchService vectorSearchService, RagQueryService queryService) {
        this.vectorSearchService = vectorSearchService;
        this.queryService = queryService;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @PostMapping("/search")
    public RestResponse<RagSearchResponse> search(@RequestBody RagQueryRequest request) {
        return RestResponse.success(vectorSearchService.search(request));
    }

    @PostMapping("/ask")
    public RestResponse<RagQueryResponse> ask(@RequestBody RagQueryRequest request) {
        return RestResponse.success(queryService.ask(request));
    }

    @PostMapping(value = "/ask-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askStream(@RequestBody RagQueryRequest request) {
        return queryService.askStream(request);
    }
}
