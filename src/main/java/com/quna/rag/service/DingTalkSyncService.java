package com.quna.rag.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.quna.rag.util.DingTalkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DingTalkSyncService {
    private final DingTalkUtil dingTalkUtil;

    public DingTalkSyncService(DingTalkUtil dingTalkUtil) {
        this.dingTalkUtil = dingTalkUtil;
    }
    
    /**
     * 获取节点列表（通用接口，支持根目录和子目录）
     */
    public List<Map<String, Object>> getNodes(String parentId) throws Exception {
        if (parentId == null || parentId.isEmpty()) {
            throw new RuntimeException("父节点 ID 不能为空");
        }
        
        JSONArray docs = dingTalkUtil.getKnowledgeBaseDocs(parentId);
        List<Map<String, Object>> nodes = new ArrayList<>();
        
        for (int i = 0; i < docs.size(); i++) {
            JSONObject doc = docs.getJSONObject(i);
            String type = doc.getString("type");
            Map<String, Object> node = new HashMap<>();
            node.put("key", doc.getString("nodeId"));
            node.put("title", doc.getString("name"));
            node.put("type", type);
            node.put("children", null); // null 表示未加载
            
            nodes.add(node);
        }
        
        log.info("获取到节点 {} 下的 {} 个子节点", parentId, nodes.size());
        return nodes;
    }
    
    /**
     * 同步单个文档到系统
     * 注意：docId 参数实际是 dentryUuid，会先转换为实际的 spaceId 和 dentryId
     */
    public void syncDoc(String dentryUuid, String docName, String docType, Long userId) throws Exception {
        log.info("开始同步文档: {}, 名称: {}, 类型: {}", dentryUuid, docName, docType);
        
        // 判断文件类型，选择不同处理方式
        if ("FILE".equals(docType)) {
            // 上传的文件：下载后处理（dentryUuid 会内部转换为实际的 spaceId 和 dentryId）
            byte[] fileBytes = dingTalkUtil.downloadDocFile(dentryUuid);
            java.io.File tempFile = java.io.File.createTempFile("dingtalk_", "_" + docName);
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                fos.write(fileBytes);
            }
            processFile(tempFile, docName, userId);
            tempFile.delete();
        } else if (isOnlineDocument(docType)) {
            // 在线文档：通过内容 API 获取（dentryUuid 会内部转换为实际的 spaceId 和 dentryId）
            String rawContent = dingTalkUtil.getDocContent(dentryUuid);
            
            // 检查内容是否为空
            if (rawContent == null || rawContent.trim().isEmpty()) {
                log.warn("文档内容为空: {}, 类型: {}", dentryUuid, docType);
                throw new RuntimeException("文档内容为空，可能权限不足或文档已被删除");
            }
            
            // 清理内容格式
            String cleanedContent = cleanContent(rawContent, docType);
            log.info("文档内容获取成功: {}, 原始长度: {}, 清理后长度: {}", 
                    dentryUuid, rawContent.length(), cleanedContent.length());
            
            processAndSave(cleanedContent, docName, userId);
        } else {
            // 未知类型，尝试作为在线文档处理
            log.warn("未知文档类型: {}, 尝试作为在线文档处理", docType);
            try {
                String rawContent = dingTalkUtil.getDocContent(dentryUuid);
                if (rawContent != null && !rawContent.trim().isEmpty()) {
                    String cleanedContent = cleanContent(rawContent, docType);
                    processAndSave(cleanedContent, docName, userId);
                } else {
                    throw new RuntimeException("无法获取文档内容");
                }
            } catch (Exception e) {
                log.error("作为在线文档处理失败，尝试下载: {}", e.getMessage());
                // 降级：尝试下载
                byte[] fileBytes = dingTalkUtil.downloadDocFile(dentryUuid);
                java.io.File tempFile = java.io.File.createTempFile("dingtalk_", "_" + docName);
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                    fos.write(fileBytes);
                }
                processFile(tempFile, docName, userId);
                tempFile.delete();
            }
        }
        
        log.info("文档同步成功: {}", dentryUuid);
    }
    
    /**
     * 判断是否为在线文档（需要获取内容）
     */
    private boolean isOnlineDocument(String docType) {
        if (docType == null) return false;
        String upperType = docType.toUpperCase();
        // 钉钉在线文档类型：DOC(文档)、SHEET(表格)、SLIDE(演示)、ADOC(高级文档)、MD(Markdown)
        return upperType.equals("DOC") || upperType.equals("SHEET") || upperType.equals("SLIDE") ||
               upperType.equals("ADOC") || upperType.equals("MD") || upperType.equals("MARKDOWN") ||
               upperType.equals("TXT") || upperType.equals("DOCX");
    }
    
    /**
     * 清理文档内容格式
     * @param rawContent 原始内容
     * @param docType 文档类型
     * @return 清理后的纯文本内容
     */
    private String cleanContent(String rawContent, String docType) {
        if (rawContent == null) return "";
        
        String content = rawContent;
        
        // 如果是 HTML 格式，去除 HTML 标签
        if (content.contains("<") && content.contains(">")) {
            content = content.replaceAll("<[^>]+>", " ");  // 去除所有HTML标签
            content = content.replaceAll("&nbsp;", " ");   // 替换空格实体
            content = content.replaceAll("&lt;", "<");     // 替换特殊字符
            content = content.replaceAll("&gt;", ">");
            content = content.replaceAll("&amp;", "&");
            content = content.replaceAll("&quot;", "\"");
        }
        
        // 去除多余空白行（连续3个以上换行符替换为2个）
        content = content.replaceAll("\n{3,}", "\n\n");
        
        // 去除首尾空白
        content = content.trim();
        
        log.debug("内容清理完成: 原始长度={}, 清理后长度={}", rawContent.length(), content.length());
        return content;
    }
    
    /**
     * 批量同步文档
     */
    public Map<String, Object> syncBatchDocs(List<Map<String, String>> docs, Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();
        
        for (Map<String, String> docInfo : docs) {
            String docId = docInfo.get("docId");  // 实际是 dentryUuid
            String docName = docInfo.get("docName");
            String docType = docInfo.get("docType");
            String spaceId = docInfo.get("spaceId");  // 实际也是 dentryUuid，但下载/获取内容时只用 docId 即可
            
            log.info("同步文档: {}, 名称: {}, 类型: {}, spaceId: {}", docId, docName, docType, spaceId);
            
            try {
                // docId 实际是 dentryUuid，内部会自动转换
                syncDoc(docId, docName, docType, userId);
                successList.add(docId);
            } catch (Exception e) {
                log.error("文档同步失败: {}", docId, e);
                failList.add(docId + ": " + e.getMessage());
            }
        }
        
        result.put("success", successList);
        result.put("failed", failList);
        result.put("total", docs.size());
        result.put("successCount", successList.size());
        result.put("failCount", failList.size());
        
        return result;
    }

    public void processFile(File file, String filename, Long userId) throws Exception {
        String text = "";
        processAndSave(text, filename, userId, "钉钉", file.length());
    }

    private void processAndSave(String text, String filename, Long userId, String 钉钉, long length) {
    }

    private void processAndSave(String text, String filename, Long userId) {
    }
}
