package com.quna.rag.service;

import com.quna.rag.dto.request.RagQueryRequest;
import com.quna.rag.dto.response.*;
import com.quna.rag.mapper.RagQueryLogMapper;
import com.quna.rag.model.RagQueryLog;
import com.quna.rag.util.AiUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 问答服务，基于检索结果拼接上下文并调用大模型生成答案。
 */
@Service
public class RagQueryService {
    private final RagVectorSearchService vectorSearchService;
    private final RagPromptService promptService;
    private final AiUtil aiUtil;
    private final RagQueryLogMapper queryLogMapper;

    public RagQueryService(RagVectorSearchService vectorSearchService,
                           RagPromptService promptService,
                           AiUtil aiUtil,
                           RagQueryLogMapper queryLogMapper) {
        this.vectorSearchService = vectorSearchService;
        this.promptService = promptService;
        this.aiUtil = aiUtil;
        this.queryLogMapper = queryLogMapper;
    }

    public RagQueryResponse ask(RagQueryRequest request) {
        long start = System.currentTimeMillis();
        RagSearchResponse search = vectorSearchService.search(request);
        String answer;
        if (search.getHits() == null || search.getHits().isEmpty()) {
            answer = "知识库中未找到足够信息。";
        } else {
            answer = aiUtil.chat(request.getQuestion(), promptService.build(request.getQuestion(), search.getHits()));
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
