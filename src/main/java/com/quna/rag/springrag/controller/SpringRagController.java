package com.quna.rag.springrag.controller;

import com.quna.rag.common.BasicAction;
import com.quna.rag.common.RestResponse;
import com.quna.rag.springrag.model.RagAskRequest;
import com.quna.rag.springrag.model.RagAskResult;
import com.quna.rag.springrag.model.RagDocumentEntity;
import com.quna.rag.springrag.model.RagSearchRequest;
import com.quna.rag.springrag.model.RagSearchResult;
import com.quna.rag.springrag.model.RagUploadResult;
import com.quna.rag.springrag.service.SpringRagAskService;
import com.quna.rag.springrag.service.SpringRagIngestService;
import com.quna.rag.springrag.service.SpringRagSearchService;
import com.quna.rag.springrag.store.RagDocumentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Spring RAG 对外接口层，负责文档上传、混合检索和基于检索结果问答的 HTTP 入口。
 */
@RestController
@RequestMapping("/api/springrag")
public class SpringRagController extends BasicAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringRagController.class);

    private final SpringRagIngestService ingestService;
    private final SpringRagSearchService searchService;
    private final SpringRagAskService askService;
    private final RagDocumentMapper documentMapper;

    public SpringRagController(SpringRagIngestService ingestService,
                               SpringRagSearchService searchService,
                               SpringRagAskService askService,
                               RagDocumentMapper documentMapper) {
        this.ingestService = ingestService;
        this.searchService = searchService;
        this.askService = askService;
        this.documentMapper = documentMapper;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @PostMapping("/doc/upload")
    public RestResponse<RagUploadResult> upload(@RequestParam MultipartFile file,
                                                @RequestParam String collectionCode,
                                                @RequestParam(required = false) String project,
                                                @RequestParam(required = false) String module,
                                                @RequestParam(required = false) String docType,
                                                @RequestParam(required = false) String tags) throws Exception {
        return RestResponse.success(ingestService.upload(file, collectionCode, project, module, docType, tags));
    }

    @GetMapping("/doc/list")
    public RestResponse<List<RagDocumentEntity>> list(@RequestParam(required = false) String collectionCode,
                                                      @RequestParam(required = false) String project,
                                                      @RequestParam(required = false) String module,
                                                      @RequestParam(required = false) String docType) {
        return RestResponse.success(documentMapper.selectList(collectionCode, project, module, docType));
    }

    @DeleteMapping("/doc/{docId}")
    public RestResponse<Boolean> delete(@PathVariable Long docId) {
        ingestService.deleteDocument(docId);
        return RestResponse.success(true);
    }

    @PostMapping("/search")
    public RestResponse<RagSearchResult> search(@RequestBody RagSearchRequest request) {
        return RestResponse.success(searchService.search(request));
    }

    @PostMapping("/ask")
    public RestResponse<RagAskResult> ask(@RequestBody RagAskRequest request) {
        return RestResponse.success(askService.ask(request));
    }
}
