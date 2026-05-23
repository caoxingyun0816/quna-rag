package com.quna.rag.service;

import com.quna.rag.entity.Document;
import com.quna.rag.mapper.DocConfigMapper;
import com.quna.rag.mapper.DocMapper;
import com.quna.rag.util.FileParseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocService {
    private final FileParseUtil fileParseUtil;
    private final RagService ragService;
    private final DocMapper docMapper;
    private final DocConfigMapper docConfigMapper;
    @Value("${llm.embedding-url:}")
    private String defaultEmbeddingUrl;
    @Value("${llm.embedding-model:text-embedding-v2}")
    private String defaultEmbeddingModel;
    @Value("${milvus.dim:1536}")
    private Integer defaultDimension;

    public DocService(FileParseUtil fileParseUtil, RagService ragService, DocMapper docMapper, DocConfigMapper docConfigMapper) {
        this.fileParseUtil = fileParseUtil;
        this.ragService = ragService;
        this.docMapper = docMapper;
        this.docConfigMapper = docConfigMapper;
    }

    public void upload(MultipartFile file, Long userId) throws Exception {
        String text = fileParseUtil.parse(file);
        processAndSave(text, file.getOriginalFilename(), userId, "本地上传", file.getSize());
    }
    
    /**
     * 处理本地文件（用于钉钉同步）
     */
    public void processFile(File file, String filename, Long userId) throws Exception {
        String text = fileParseUtil.parseFile(file);
        processAndSave(text, filename, userId, "钉钉", file.length());
    }

    public void processAndSave(String text, String filename, Long userId) {
        processAndSave(text, filename, userId, "钉钉", null);
    }

    public Document processAndSave(String text, String filename, Long userId, String source, Long fileSize) {
        List<String> chunks = ragService.split(text, getChunkSize(userId));
        for (String chunk : chunks) ragService.insert(chunk);
        return saveDocRecord(filename, userId, source, fileSize, chunks.size());
    }

    public void saveDocRecord(String filename, Long userId) {
        saveDocRecord(filename, userId, "本地上传", null, 0);
    }

    public Document saveDocRecord(String filename, Long userId, String source, Long fileSize, int chunkCount) {
        Document doc = new Document();
        doc.setFilename(filename);
        doc.setUserId(userId);
        doc.setSource(source);
        doc.setStatus("已入库");
        doc.setFileType(resolveFileType(filename));
        doc.setFileSize(fileSize);
        doc.setChunkCount(chunkCount);
        doc.setPermissionScope("私有");
        docMapper.insert(doc);
        return doc;
    }

    public RagService getRagService() {
        return ragService;
    }

    public List<Document> list(Long userId, String keyword, String source, String status) {
        return docMapper.selectByUser(userId, keyword, source, status);
    }

    public Document detail(Long id, Long userId) {
        return docMapper.selectByIdAndUser(id, userId);
    }

    public boolean updateStatus(Long id, Long userId, String status) {
        return docMapper.updateStatus(id, userId, status) > 0;
    }

    public Map<String, Object> getChunkConfig(Long userId) {
        Map<String, Object> saved = docConfigMapper.selectChunkConfig(userId);
        if (saved != null) return saved;
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("chunkSize", 500);
        defaults.put("chunkOverlap", 0);
        defaults.put("separators", "段落,换行,句号");
        defaults.put("enableTitleEnhance", true);
        return defaults;
    }

    public void saveChunkConfig(Long userId, Map<String, Object> body) {
        Map<String, Object> config = new LinkedHashMap<>(body);
        config.put("userId", userId);
        config.putIfAbsent("chunkSize", 500);
        config.putIfAbsent("chunkOverlap", 0);
        config.putIfAbsent("separators", "段落,换行,句号");
        config.putIfAbsent("enableTitleEnhance", true);
        docConfigMapper.insertChunkConfig(config);
    }

    public Map<String, Object> getVectorConfig(Long userId) {
        Map<String, Object> saved = docConfigMapper.selectVectorConfig(userId);
        if (saved != null) {
            saved.put("endpoint", maskEndpoint(String.valueOf(saved.getOrDefault("endpoint", ""))));
            return saved;
        }
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("provider", "DashScope");
        defaults.put("model", defaultEmbeddingModel);
        defaults.put("dimension", defaultDimension);
        defaults.put("endpoint", maskEndpoint(defaultEmbeddingUrl));
        defaults.put("enabled", true);
        return defaults;
    }

    public void saveVectorConfig(Long userId, Map<String, Object> body) {
        Map<String, Object> config = new LinkedHashMap<>(body);
        config.put("userId", userId);
        config.putIfAbsent("provider", "DashScope");
        config.putIfAbsent("model", defaultEmbeddingModel);
        config.putIfAbsent("dimension", defaultDimension);
        config.putIfAbsent("endpoint", defaultEmbeddingUrl);
        config.putIfAbsent("enabled", true);
        docConfigMapper.insertVectorConfig(config);
    }

    public List<Map<String, Object>> permissions(Long userId) {
        return docConfigMapper.selectPermissions(userId);
    }

    public void savePermission(Long userId, Map<String, Object> body) {
        Long documentId = Long.valueOf(String.valueOf(body.get("documentId")));
        Document doc = detail(documentId, userId);
        if (doc == null) {
            throw new IllegalArgumentException("文档不存在或无权限");
        }
        Map<String, Object> permission = new LinkedHashMap<>(body);
        permission.putIfAbsent("permission", "READ");
        docConfigMapper.insertPermission(permission);
        Object scope = body.get("permissionScope");
        if (scope != null) {
            docMapper.updatePermissionScope(documentId, userId, String.valueOf(scope));
        }
    }

    public Map<String, Object> searchTest(Long userId, Map<String, Object> body) {
        String question = String.valueOf(body.getOrDefault("question", "")).trim();
        if (question.isEmpty()) {
            throw new IllegalArgumentException("检索问题不能为空");
        }
        List<Map<String, Object>> hits = ragService.searchHits(question, getTopK(body));
        String context = ragService.buildContext(hits);
        boolean includeAnswer = Boolean.parseBoolean(String.valueOf(body.getOrDefault("includeAnswer", false)));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", question);
        result.put("context", context);
        result.put("hits", hits);
        result.put("answer", includeAnswer ? ragService.getAiUtil().chat(question, context) : "");
        result.put("includeAnswer", includeAnswer);
        return result;
    }

    private int getChunkSize(Long userId) {
        Object chunkSize = getChunkConfig(userId).get("chunkSize");
        try {
            return Math.max(100, Integer.parseInt(String.valueOf(chunkSize)));
        } catch (Exception e) {
            return 500;
        }
    }

    private int getTopK(Map<String, Object> body) {
        try {
            return Math.max(1, Math.min(20, Integer.parseInt(String.valueOf(body.getOrDefault("topK", 3)))));
        } catch (Exception e) {
            return 3;
        }
    }

    private String resolveFileType(String filename) {
        if (filename == null || !filename.contains(".")) return "未知";
        return filename.substring(filename.lastIndexOf('.') + 1).toUpperCase();
    }

    private String maskEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) return "";
        int index = endpoint.indexOf("/api/");
        return index > 0 ? endpoint.substring(0, index) + "/api/***" : endpoint;
    }
}
