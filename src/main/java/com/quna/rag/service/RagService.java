package com.quna.rag.service;

import com.quna.rag.util.AiUtil;
import io.milvus.client.MilvusClient;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.SearchResultsWrapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RagService {
    private final MilvusClient milvusClient;
    private final AiUtil aiUtil;
    @Value("${milvus.collection}") private String collName;
    @Value("${milvus.dim}") private int dim;

    public RagService(MilvusClient milvusClient, AiUtil aiUtil) {
        this.milvusClient = milvusClient;
        this.aiUtil = aiUtil;
    }

    @PostConstruct
    public void init() {
        try {
            var hasResponse = milvusClient.hasCollection(HasCollectionParam.newBuilder()
                    .withCollectionName(collName)
                    .build());
            if (!hasResponse.getData()) {
                milvusClient.createCollection(CreateCollectionParam.newBuilder()
                        .withCollectionName(collName)
                        .addFieldType(FieldType.newBuilder().withName("id").withDataType(io.milvus.grpc.DataType.Int64).withPrimaryKey(true).withAutoID(true).build())
                        .addFieldType(FieldType.newBuilder().withName("content").withDataType(io.milvus.grpc.DataType.VarChar).withMaxLength(65535).build())
                        .addFieldType(FieldType.newBuilder().withName("vector").withDataType(io.milvus.grpc.DataType.FloatVector).withDimension(dim).build())
                        .build());
                log.info("Milvus 集合创建成功: {}, 维度: {}", collName, dim);
            }
            
            // Milvus 集合必须加载后才能进行插入和搜索操作
            milvusClient.loadCollection(LoadCollectionParam.newBuilder()
                    .withCollectionName(collName)
                    .build());
            
            // 等待加载完成（轮询检查集合状态）
            Thread.sleep(2000);
            
            log.info("Milvus 集合已加载: {}", collName);
        } catch (Exception e) {
            log.error("Milvus 集合初始化失败: {}", e.getMessage());
            throw new RuntimeException("Milvus 初始化失败", e);
        }
    }

    public List<String> split(String text) {
        return split(text, 500);
    }

    public List<String> split(String text, int chunkSize) {
        List<String> chunks = new java.util.ArrayList<>();
        chunkSize = Math.max(100, Math.min(chunkSize, 2000));
        
        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, text.length());
            chunks.add(text.substring(i, end));
        }
        
        log.info("文本分割完成: 原文{}字符, 分割为{}块", text.length(), chunks.size());
        return chunks;
    }

    public void insert(String content) {
        // 清理文本中的非法 UTF-16 字符，避免 Milvus Protobuf 编码错误
        String cleanedContent = cleanInvalidUtf16(content);
        
        List<Float> vec = aiUtil.embedding(cleanedContent);
        if (vec == null || vec.isEmpty()) {
            throw new RuntimeException("向量生成失败");
        }
        
        List<List<Float>> vectors = new java.util.ArrayList<>();
        vectors.add(vec);
        List<String> contents = new java.util.ArrayList<>();
        contents.add(cleanedContent);
        
        List<InsertParam.Field> fields = new java.util.ArrayList<>();
        fields.add(new InsertParam.Field("content", contents));
        fields.add(new InsertParam.Field("vector", vectors));
        
        milvusClient.insert(InsertParam.newBuilder()
                .withCollectionName(collName)
                .withFields(fields)
                .build());
    }

    /**
     * 清理文本中的非法 UTF-16 字符（未配对的代理字符）
     * 避免 Milvus Protobuf 编码时出现 Unpaired surrogate 错误
     */
    private String cleanInvalidUtf16(String text) {
        if (text == null) return null;
        
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // 检查是否是高位代理（High Surrogate）
            if (Character.isHighSurrogate(c)) {
                // 如果后面有低位代理，保留整个代理对
                if (i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1))) {
                    cleaned.append(c).append(text.charAt(i + 1));
                    i++; // 跳过下一个字符
                }
                // 否则跳过这个未配对的高位代理
            }
            // 检查是否是低位代理（Low Surrogate）
            else if (Character.isLowSurrogate(c)) {
                // 跳过未配对的低位代理
            }
            // 普通字符直接保留
            else {
                cleaned.append(c);
            }
        }
        
        String result = cleaned.toString();
        if (result.length() != text.length()) {
            log.warn("清理了 {} 个非法 UTF-16 字符，原文长度: {}, 清理后长度: {}",
                    text.length() - result.length(), text.length(), result.length());
        }
        
        return result;
    }

    public String search(String question) {
        List<Float> vec = aiUtil.embedding(question);
        if (vec == null || vec.isEmpty()) {
            return "向量生成失败，请检查配置";
        }
        
        // Milvus SDK 要求使用 List<List<Float>> 或 List<io.milvus.grpc.FloatVector>
        List<List<Float>> vectors = new java.util.ArrayList<>();
        vectors.add(vec);
        
        var res = milvusClient.search(SearchParam.newBuilder()
                .withCollectionName(collName)
                .withVectorFieldName("vector")
                .withVectors(vectors)
                .withTopK(3)
                .withOutFields(List.of("content"))
                .build());
        
        SearchResultsWrapper wrapper = new SearchResultsWrapper(res.getData().getResults());
        // 检查是否有搜索结果
        if (wrapper.getRowRecords().isEmpty()) {
            return aiUtil.chat(question, "");
        }
        
        StringBuilder sb = new StringBuilder();
        for (Object o : wrapper.getFieldData("content", 0)) sb.append(o).append("\n");
        return aiUtil.chat(question, sb.toString());
    }

    /**
     * 流式搜索 - 返回检索到的上下文
     */

    public String searchContext(String question) {
        return buildContext(searchHits(question, 3));
    }

    public List<Map<String, Object>> searchHits(String question, int topK) {
        List<Float> vec = aiUtil.embedding(question);
        if (vec == null || vec.isEmpty()) {
            return List.of();
        }

        List<List<Float>> vectors = new java.util.ArrayList<>();
        vectors.add(vec);

        var res = milvusClient.search(SearchParam.newBuilder()
                .withCollectionName(collName)
                .withVectorFieldName("vector")
                .withVectors(vectors)
                .withTopK(topK)
                .withOutFields(List.of("content"))
                .build());

        SearchResultsWrapper wrapper = new SearchResultsWrapper(res.getData().getResults());
        List<Map<String, Object>> hits = new java.util.ArrayList<>();
        int index = 1;
        for (Object content : wrapper.getFieldData("content", 0)) {
            hits.add(Map.of(
                    "rank", index++,
                    "content", content == null ? "" : String.valueOf(content)
            ));
        }
        return hits;
    }

    public String buildContext(List<Map<String, Object>> hits) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> hit : hits) {
            Object content = hit.get("content");
            if (content != null) {
                sb.append(content).append("\n");
            }
        }
        return sb.toString();
    }

    public AiUtil getAiUtil() {
        return aiUtil;
    }
}
