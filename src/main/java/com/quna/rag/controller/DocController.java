package com.quna.rag.controller;

import com.quna.rag.service.DocService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doc")
public class DocController {
    private final DocService docService;

    public DocController(DocService docService) {
        this.docService = docService;
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam MultipartFile file, HttpServletRequest request) throws Exception {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        docService.upload(file, userId);
        return Map.of("code", 200, "msg", "上传成功");
    }

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String source,
                                    @RequestParam(required = false) String status,
                                    HttpServletRequest request) {
        Long userId = currentUserId(request);
        return Map.of("code", 200, "data", docService.list(userId, keyword, source, status));
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        return Map.of("code", 200, "data", docService.detail(id, userId));
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = currentUserId(request);
        boolean updated = docService.updateStatus(id, userId, String.valueOf(body.getOrDefault("status", "已入库")));
        return Map.of("code", updated ? 200 : 404, "msg", updated ? "更新成功" : "文档不存在");
    }

    @GetMapping("/chunk-config")
    public Map<String, Object> chunkConfig(HttpServletRequest request) {
        return Map.of("code", 200, "data", docService.getChunkConfig(currentUserId(request)));
    }

    @PutMapping("/chunk-config")
    public Map<String, Object> saveChunkConfig(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        docService.saveChunkConfig(currentUserId(request), body);
        return Map.of("code", 200, "msg", "保存成功");
    }

    @GetMapping("/vector-config")
    public Map<String, Object> vectorConfig(HttpServletRequest request) {
        return Map.of("code", 200, "data", docService.getVectorConfig(currentUserId(request)));
    }

    @PutMapping("/vector-config")
    public Map<String, Object> saveVectorConfig(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        docService.saveVectorConfig(currentUserId(request), body);
        return Map.of("code", 200, "msg", "保存成功");
    }

    @GetMapping("/permissions")
    public Map<String, Object> permissions(HttpServletRequest request) {
        List<Map<String, Object>> data = docService.permissions(currentUserId(request));
        return Map.of("code", 200, "data", data);
    }

    @PutMapping("/permissions")
    public Map<String, Object> savePermission(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        docService.savePermission(currentUserId(request), body);
        return Map.of("code", 200, "msg", "保存成功");
    }

    @PostMapping("/search-test")
    public Map<String, Object> searchTest(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return Map.of("code", 200, "data", docService.searchTest(currentUserId(request), body));
    }

    private Long currentUserId(HttpServletRequest request) {
        return Long.parseLong(request.getAttribute("userId").toString());
    }
}
