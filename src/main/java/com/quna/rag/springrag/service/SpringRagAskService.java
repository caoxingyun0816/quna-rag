package com.quna.rag.springrag.service;

import com.quna.rag.springrag.model.RagAskRequest;
import com.quna.rag.springrag.model.RagHit;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 问答服务，先检索相关切片，再组织提示词调用大模型生成回答。
 */
@Service
public class SpringRagAskService {
    private final SpringRagSearchService searchService;
    private final ChatClient chatClient;

    public SpringRagAskService(SpringRagSearchService searchService,
                               @Qualifier("springAiRagChatClient") ChatClient chatClient) {
        this.searchService = searchService;
        this.chatClient = chatClient;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> ask(RagAskRequest request) {
        Map<String, Object> searchResult = searchService.search(request);
        List<RagHit> hits = (List<RagHit>) searchResult.get("hits");
        String answer;
        if (hits == null || hits.isEmpty()) {
            answer = "没有找到足够相关的文档。";
        } else {
            answer = chatClient.prompt()
                    .user(prompt(request.getQuestion(), hits))
                    .call()
                    .content();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", request.getQuestion());
        result.put("answer", answer);
        if (Boolean.TRUE.equals(request.getIncludeSources())) {
            result.put("sources", hits);
        }
        return result;
    }

    private String prompt(String question, List<RagHit> hits) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            RagHit hit = hits.get(i);
            context.append("资料").append(i + 1).append("\n")
                    .append("文档：").append(hit.getFilename()).append("\n")
                    .append("标题：").append(hit.getTitlePath()).append("\n")
                    .append(hit.getContent()).append("\n\n");
        }
        return """
                你是趣拿企业知识库助手。请只依据给定资料回答问题。
                如果资料不足以回答，请直接说明“没有找到足够依据”，不要编造。

                用户问题：
                %s

                检索资料：
                %s
                """.formatted(question, context);
    }
}
