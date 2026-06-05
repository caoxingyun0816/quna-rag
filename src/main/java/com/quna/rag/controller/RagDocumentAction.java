package com.quna.rag.controller;

import com.quna.rag.common.BasicAction;
import com.quna.rag.common.RestResponse;
import com.quna.rag.dto.request.RagChunkListRequest;
import com.quna.rag.dto.request.RagDocumentListRequest;
import com.quna.rag.dto.request.RagDocumentUploadRequest;
import com.quna.rag.dto.response.RagChunkResponse;
import com.quna.rag.dto.response.RagDocumentResponse;
import com.quna.rag.dto.response.RagDocumentUploadResponse;
import com.quna.rag.service.RagChunkService;
import com.quna.rag.service.RagDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * RAG 文档管理入口，负责文档上传、列表、删除和切片查看。
 */
@RestController
@RequestMapping("/api/rag/document")
public class RagDocumentAction extends BasicAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(RagDocumentAction.class);

    private final RagDocumentService documentService;
    private final RagChunkService chunkService;

    public RagDocumentAction(RagDocumentService documentService, RagChunkService chunkService) {
        this.documentService = documentService;
        this.chunkService = chunkService;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @PostMapping("/upload")
    public RestResponse<RagDocumentUploadResponse> upload(@RequestParam MultipartFile file,
                                                          @ModelAttribute RagDocumentUploadRequest request) throws Exception {
        return RestResponse.success(documentService.upload(file, request));
    }

    @GetMapping("/list")
    public RestResponse<List<RagDocumentResponse>> list(@ModelAttribute RagDocumentListRequest request) {
        return RestResponse.success(documentService.list(request));
    }

    @DeleteMapping("/{docId}")
    public RestResponse<Boolean> delete(@PathVariable Long docId) {
        return RestResponse.success(documentService.delete(docId));
    }

    @PostMapping("/{docId}/rebuild")
    public RestResponse<Boolean> rebuild(@PathVariable Long docId) {
        return RestResponse.success(documentService.rebuild(docId));
    }

    @GetMapping("/chunks")
    public RestResponse<List<RagChunkResponse>> chunks(@ModelAttribute RagChunkListRequest request) {
        return RestResponse.success(chunkService.list(request));
    }

    @GetMapping("/{docId}/chunks")
    public RestResponse<List<RagChunkResponse>> chunks(@PathVariable Long docId) {
        RagChunkListRequest request = new RagChunkListRequest();
        request.setDocId(docId);
        return RestResponse.success(chunkService.list(request));
    }

    @DeleteMapping("/chunks/{chunkId}")
    public RestResponse<Boolean> deleteChunk(@PathVariable Long chunkId) {
        return RestResponse.success(chunkService.delete(chunkId));
    }
}
