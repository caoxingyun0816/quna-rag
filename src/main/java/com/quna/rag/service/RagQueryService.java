package com.quna.rag.service;

import com.quna.rag.dto.request.RagQueryRequest;
import com.quna.rag.dto.response.RagQueryResponse;
import com.quna.rag.dto.response.RagReferenceResponse;
import com.quna.rag.dto.response.RagSearchHitResponse;
import com.quna.rag.dto.response.RagSearchResponse;
import com.quna.rag.entity.RagQueryLog;
import com.quna.rag.mapper.RagQueryLogMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 问答服务，基于检索结果拼接上下文并调用大模型生成答案。
 */
@Service
public class RagQueryService {
    private final RagVectorSearchService vectorSearchService;
    private final RagPromptService promptService;
    private final ChatClient chatClient;
    private final RagQueryLogMapper queryLogMapper;

    public RagQueryService(RagVectorSearchService vectorSearchService,
                           RagPromptService promptService,
                           @Qualifier("ragChatClient") ChatClient chatClient,
                           RagQueryLogMapper queryLogMapper) {
        this.vectorSearchService = vectorSearchService;
        this.promptService = promptService;
        this.chatClient = chatClient;
        this.queryLogMapper = queryLogMapper;
    }

    public RagQueryResponse ask(RagQueryRequest request) {
        long start = System.currentTimeMillis();
        RagSearchResponse search = vectorSearchService.search(request);
        String answer;
        if (search.getHits() == null || search.getHits().isEmpty()) {
            answer = "知识库中未找到足够信息。";
        } else {
            answer = chatClient.prompt()
                    .user(promptService.build(request.getQuestion(), search.getHits()))
                    .call()
                    .content();
        }
        RagQueryResponse response = new RagQueryResponse();
        response.setQuestion(request.getQuestion());
        response.setAnswer(answer);
        response.setTotalCandidates(search.getTotalCandidates());
        response.setTotal(search.getTotal());
        response.setReferences(search.getHits().stream().map(this::toReference).toList());
        saveLog(request, response, System.currentTimeMillis() - start, true, null);
        return response;
    }

    public SseEmitter askStream(RagQueryRequest request) {
        SseEmitter emitter = new SseEmitter(60000L);
        long start = System.currentTimeMillis();
        RagSearchResponse search = vectorSearchService.search(request);
        StringBuilder answer = new StringBuilder();
        try {
            emitter.send(SseEmitter.event().name("sources").data(search));
            if (search.getHits() == null || search.getHits().isEmpty()) {
                String empty = "知识库中未找到足够信息。";
                answer.append(empty);
                emitter.send(SseEmitter.event().name("message").data(empty));
                emitter.complete();
                saveLog(request, toResponse(request, answer.toString(), search), System.currentTimeMillis() - start, true, null);
                return emitter;
            }
            chatClient.prompt()
                    .user(promptService.build(request.getQuestion(), search.getHits()))
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        try {
                            answer.append(chunk);
                            emitter.send(SseEmitter.event().name("message").data(chunk));
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .doOnError(error -> {
                        saveLog(request, toResponse(request, answer.toString(), search), System.currentTimeMillis() - start, false, error.getMessage());
                        emitter.completeWithError(error);
                    })
                    .doOnComplete(() -> {
                        saveLog(request, toResponse(request, answer.toString(), search), System.currentTimeMillis() - start, true, null);
                        emitter.complete();
                    })
                    .subscribe();
        } catch (Exception e) {
            saveLog(request, toResponse(request, answer.toString(), search), System.currentTimeMillis() - start, false, e.getMessage());
            emitter.completeWithError(e);
        }
        return emitter;
    }

    private RagReferenceResponse toReference(RagSearchHitResponse hit) {
        RagReferenceResponse response = new RagReferenceResponse();
        response.setDocId(hit.getDocId());
        response.setChunkId(hit.getChunkId());
        response.setDocName(hit.getDocName());
        response.setTitle(hit.getTitle());
        response.setScore(hit.getScore());
        response.setVectorScore(hit.getVectorScore());
        response.setKeywordScore(hit.getKeywordScore());
        response.setVectorHit(hit.getVectorHit());
        response.setKeywordHit(hit.getKeywordHit());
        return response;
    }

    private RagQueryResponse toResponse(RagQueryRequest request, String answer, RagSearchResponse search) {
        RagQueryResponse response = new RagQueryResponse();
        response.setQuestion(request.getQuestion());
        response.setAnswer(answer);
        response.setTotalCandidates(search.getTotalCandidates());
        response.setTotal(search.getTotal());
        response.setReferences(search.getHits() == null ? List.of() : search.getHits().stream().map(this::toReference).toList());
        return response;
    }

    private void saveLog(RagQueryRequest request, RagQueryResponse response, long costTimeMs, boolean success, String errorMsg) {
        RagQueryLog log = new RagQueryLog();
        log.setKbId(request.getKbId());
        log.setQuestion(request.getQuestion());
        log.setAnswer(response.getAnswer());
        log.setHitChunkIds(response.getReferences().stream()
                .map(RagReferenceResponse::getChunkId)
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
        log.setCostTimeMs(costTimeMs);
        log.setSuccess(success ? 1 : 0);
        log.setErrorMsg(errorMsg);
        queryLogMapper.insert(log);
    }
}
