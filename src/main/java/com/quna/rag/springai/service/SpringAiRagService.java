package com.quna.rag.springai.service;

import com.quna.rag.springai.model.SpringAiRagAskRequest;
import com.quna.rag.springai.model.SpringAiRagSearchRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpringAiRagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final int defaultTopK;
    private final double defaultSimilarityThreshold;

    public SpringAiRagService(@Qualifier("springAiRagChatClient") ChatClient chatClient,
                              @Qualifier("springAiVectorStore") VectorStore vectorStore,
                              @Value("${rag.spring-ai.top-k:3}") int defaultTopK,
                              @Value("${rag.spring-ai.similarity-threshold:0.3}") double defaultSimilarityThreshold) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.defaultTopK = defaultTopK;
        this.defaultSimilarityThreshold = defaultSimilarityThreshold;
    }

    public Map<String, Object> ask(SpringAiRagAskRequest request) {
        String question = requireQuestion(request.getQuestion());
        int topK = topK(request.getTopK());
        double threshold = threshold(request.getSimilarityThreshold());
        String answer = chatClient.prompt()
                .advisors(questionAnswerAdvisor(topK, threshold))
                .user(question)
                .call()
                .content();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", question);
        result.put("answer", answer);
        result.put("topK", topK);
        result.put("similarityThreshold", threshold);
        return result;
    }

    public Map<String, Object> search(SpringAiRagSearchRequest request) {
        String question = requireQuestion(request.getQuestion());
        List<Map<String, Object>> hits = vectorStore.similaritySearch(SearchRequest.builder()
                        .query(question)
                        .topK(topK(request.getTopK()))
                        .similarityThreshold(threshold(request.getSimilarityThreshold()))
                        .build())
                .stream()
                .map(this::toHit)
                .toList();
        return Map.of(
                "question", question,
                "hits", hits,
                "total", hits.size()
        );
    }

    private Map<String, Object> toHit(Document document) {
        Map<String, Object> hit = new LinkedHashMap<>();
        hit.put("content", document.getText());
        hit.put("score", document.getScore());
        hit.put("metadata", document.getMetadata());
        return hit;
    }

    private String requireQuestion(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question 不能为空");
        }
        return question.trim();
    }

    private int topK(Integer topK) {
        return topK == null ? defaultTopK : Math.max(1, Math.min(20, topK));
    }

    private double threshold(Double threshold) {
        return threshold == null ? defaultSimilarityThreshold : Math.max(0d, Math.min(1d, threshold));
    }

    private QuestionAnswerAdvisor questionAnswerAdvisor(int topK, double threshold) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(PromptTemplate.builder()
                        .template("""
                                你是趣拿企业知识库助手。

                                用户问题：
                                {query}

                                可参考的知识库内容：
                                ---------------------
                                {question_answer_context}
                                ---------------------

                                回答规则：
                                1. 优先依据知识库内容回答。
                                2. 如果知识库内容无法回答，请直接说明不知道。
                                3. 不要编造不存在的信息。
                                """)
                        .build())
                .searchRequest(SearchRequest.builder()
                        .topK(topK)
                        .similarityThreshold(threshold)
                        .build())
                .build();
    }
}
