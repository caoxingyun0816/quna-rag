package com.quna.rag.service;

import com.quna.rag.entity.ChatHistory;
import com.quna.rag.mapper.ChatMapper;
import com.quna.rag.util.AiUtil;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatService {
    private final ChatMapper chatMapper;
    private final RagService ragService;
    private final AiUtil aiUtil;

    public ChatService(ChatMapper chatMapper, RagService ragService, AiUtil aiUtil) {
        this.chatMapper = chatMapper;
        this.ragService = ragService;
        this.aiUtil = aiUtil;
    }

    public String ask(String question, Long userId) {
        String answer = ragService.search(question);
        ChatHistory history = new ChatHistory();
        history.setUserId(userId);
        history.setQuestion(question);
        history.setAnswer(answer);
        chatMapper.insert(history);
        return answer;
    }

    /**
     * 流式问答 - 返回完整答案用于保存历史
     */
    public void askStream(String question, Long userId, java.util.function.Consumer<String> onChunk) {
        // 先检索上下文
        String context = ragService.searchContext(question);
        
        // 使用原子引用累积完整答案
        AtomicReference<StringBuilder> fullAnswer = new AtomicReference<>(new StringBuilder());
        
        // 流式调用大模型
        aiUtil.chatStream(question, context, chunk -> {
            fullAnswer.get().append(chunk);
            onChunk.accept(chunk);
        });
        
        // 异步保存历史记录（不阻塞流式输出）
        String completeAnswer = fullAnswer.get().toString();
        if (!completeAnswer.isEmpty()) {
            ChatHistory history = new ChatHistory();
            history.setUserId(userId);
            history.setQuestion(question);
            history.setAnswer(completeAnswer);
            chatMapper.insert(history);
        }
    }

    public List<ChatHistory> history(Long userId) {
        return chatMapper.selectByUserId(userId);
    }
}
