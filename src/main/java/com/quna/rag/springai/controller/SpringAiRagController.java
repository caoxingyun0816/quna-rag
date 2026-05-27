package com.quna.rag.springai.controller;

import com.quna.rag.springai.model.SpringAiDocIngestRequest;
import com.quna.rag.springai.model.SpringAiRagAskRequest;
import com.quna.rag.springai.model.SpringAiRagSearchRequest;
import com.quna.rag.springai.service.SpringAiDocumentIngestService;
import com.quna.rag.springai.service.SpringAiRagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/springai")
public class SpringAiRagController {

    private final SpringAiRagService springAiRagService;
    private final SpringAiDocumentIngestService ingestService;

    public SpringAiRagController(SpringAiRagService springAiRagService,
                                 SpringAiDocumentIngestService ingestService) {
        this.springAiRagService = springAiRagService;
        this.ingestService = ingestService;
    }

    @PostMapping("/rag/ask")
    public Map<String, Object> ask(@RequestBody SpringAiRagAskRequest request) {
        return Map.of("code", 200, "data", springAiRagService.ask(request));
    }

    @PostMapping("/rag/search")
    public Map<String, Object> search(@RequestBody SpringAiRagSearchRequest request) {
        return Map.of("code", 200, "data", springAiRagService.search(request));
    }

    @PostMapping("/doc/ingest-text")
    public Map<String, Object> ingestText(@RequestBody SpringAiDocIngestRequest request) {
        return Map.of("code", 200, "data", ingestService.ingestText(request));
    }
}
