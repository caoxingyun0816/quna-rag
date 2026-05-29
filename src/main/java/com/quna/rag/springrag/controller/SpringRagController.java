package com.quna.rag.springrag.controller;

import com.quna.rag.springrag.model.RagAskRequest;
import com.quna.rag.springrag.model.RagSearchRequest;
import com.quna.rag.springrag.service.SpringRagAskService;
import com.quna.rag.springrag.service.SpringRagIngestService;
import com.quna.rag.springrag.service.SpringRagSearchService;
import com.quna.rag.springrag.store.RagDocumentMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Spring RAG 对外接口层，负责文档上传、混合检索和基于检索结果问答的 HTTP 入口。
 */
@RestController
@RequestMapping("/api/springrag")
public class SpringRagController {
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

    @PostMapping("/doc/upload")
    public Map<String, Object> upload(@RequestParam MultipartFile file,
                                      @RequestParam String collectionCode,
                                      @RequestParam(required = false) String project,
                                      @RequestParam(required = false) String module,
                                      @RequestParam(required = false) String docType,
                                      @RequestParam(required = false) String tags) throws Exception {
        return Map.of("code", 200, "data", ingestService.upload(file, collectionCode, project, module, docType, tags));
    }

    @GetMapping("/doc/list")
    public Map<String, Object> list(@RequestParam(required = false) String collectionCode,
                                    @RequestParam(required = false) String project,
                                    @RequestParam(required = false) String module,
                                    @RequestParam(required = false) String docType) {
        return Map.of("code", 200, "data", documentMapper.selectList(collectionCode, project, module, docType));
    }

    @PostMapping("/search")
    public Map<String, Object> search(@RequestBody RagSearchRequest request) {
        return Map.of("code", 200, "data", searchService.search(request));
    }

    @PostMapping("/ask")
    public Map<String, Object> ask(@RequestBody RagAskRequest request) {
        return Map.of("code", 200, "data", askService.ask(request));
    }
}
