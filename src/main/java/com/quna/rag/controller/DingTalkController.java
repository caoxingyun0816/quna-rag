package com.quna.rag.controller;

import com.quna.rag.service.DingTalkSyncService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dingtalk")
public class DingTalkController {
    private final DingTalkSyncService syncService;
    
    @Value("${dingtalk.kb-space-id:}")
    private String kbSpaceId;

    public DingTalkController(DingTalkSyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/tree")
    public Map<String, Object> getTree(@RequestParam(required = false) String parentId) throws Exception {
        // 如果没有 parentId，获取根目录；否则获取指定父节点下的子节点
        String targetId = (parentId == null || parentId.isEmpty()) ? kbSpaceId : parentId;
        
        if (kbSpaceId == null || kbSpaceId.isEmpty()) {
            return Map.of("code", 500, "msg", "钉钉知识库空间ID未配置");
        }
        
        return Map.of(
            "code", 200,
            "data", syncService.getNodes(targetId),
            "spaceId", kbSpaceId  // 返回给前端
        );
    }

    @PostMapping("/sync")
    public Map<String, Object> sync(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        
        List<Map<String, String>> docs = (List<Map<String, String>>) body.get("docs");
        if (docs == null || docs.isEmpty()) {
            return Map.of("code", 400, "msg", "请选择要同步的文档");
        }
        
        return Map.of("code", 200, "data", syncService.syncBatchDocs(docs, userId));
    }
}
