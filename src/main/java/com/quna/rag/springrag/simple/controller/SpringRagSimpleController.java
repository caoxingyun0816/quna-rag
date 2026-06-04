package com.quna.rag.springrag.simple.controller;

import com.quna.rag.common.BasicAction;
import com.quna.rag.common.RestResponse;
import com.quna.rag.springrag.simple.model.SimpleSearchRequest;
import com.quna.rag.springrag.simple.model.SimpleSearchResult;
import com.quna.rag.springrag.simple.model.SimpleUploadResult;
import com.quna.rag.springrag.simple.service.SpringRagSimpleIngestService;
import com.quna.rag.springrag.simple.service.SpringRagSimpleSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简单 RAG 接口层，提供字符切片上传和纯向量查询两个基础能力。
 */
@RestController
@RequestMapping("/api/springrag/simple")
public class SpringRagSimpleController extends BasicAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringRagSimpleController.class);

    private final SpringRagSimpleIngestService ingestService;
    private final SpringRagSimpleSearchService searchService;

    public SpringRagSimpleController(SpringRagSimpleIngestService ingestService,
                                     SpringRagSimpleSearchService searchService) {
        this.ingestService = ingestService;
        this.searchService = searchService;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @PostMapping("/doc/upload")
    public RestResponse<SimpleUploadResult> upload(@RequestParam MultipartFile file,
                                                   @RequestParam String collectionCode,
                                                   @RequestParam(required = false) String project,
                                                   @RequestParam(required = false) String module,
                                                   @RequestParam(required = false) String docType,
                                                   @RequestParam(required = false) String tags,
                                                   @RequestParam(required = false) Integer chunkSize,
                                                   @RequestParam(required = false) Integer overlap) throws Exception {
        return RestResponse.success(ingestService.upload(file, collectionCode, project, module, docType, tags,
                chunkSize, overlap));
    }

    @PostMapping("/search")
    public RestResponse<SimpleSearchResult> search(@RequestBody SimpleSearchRequest request) {
        return RestResponse.success(searchService.search(request));
    }
}
