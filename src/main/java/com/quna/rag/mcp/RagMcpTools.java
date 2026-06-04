package com.quna.rag.mcp;

import com.quna.rag.dto.request.RagDocumentListRequest;
import com.quna.rag.dto.request.RagQueryRequest;
import com.quna.rag.dto.response.RagDocumentResponse;
import com.quna.rag.dto.response.RagQueryResponse;
import com.quna.rag.dto.response.RagSearchResponse;
import com.quna.rag.mcp.dto.RagMcpAskResponse;
import com.quna.rag.mcp.dto.RagMcpDocumentListResponse;
import com.quna.rag.mcp.dto.RagMcpSearchResponse;
import com.quna.rag.service.RagDocumentService;
import com.quna.rag.service.RagQueryService;
import com.quna.rag.service.RagVectorSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标准 RAG MCP 工具服务。
 */
@Slf4j
@Service
public class RagMcpTools {
    private final RagMcpAuthService authService;
    private final RagMcpPermissionService permissionService;
    private final RagDocumentService documentService;
    private final RagVectorSearchService searchService;
    private final RagQueryService queryService;

    public RagMcpTools(RagMcpAuthService authService,
                       RagMcpPermissionService permissionService,
                       RagDocumentService documentService,
                       RagVectorSearchService searchService,
                       RagQueryService queryService) {
        this.authService = authService;
        this.permissionService = permissionService;
        this.documentService = documentService;
        this.searchService = searchService;
        this.queryService = queryService;
    }

    @Tool(description = "查询 RAG 知识库文档列表，支持按知识库、项目、模块、文档类型过滤")
    public RagMcpDocumentListResponse quna_rag_doc_list(
            @ToolParam(description = "知识库ID，例如 business_doc 对应的 kbId", required = false) Long kbId,
            @ToolParam(description = "项目编码", required = false) String projectCode,
            @ToolParam(description = "业务模块", required = false) String bizModule,
            @ToolParam(description = "文档类型，如 md/pdf/docx/xlsx/txt", required = false) String docType) {

        log.info("MCP 工具调用: quna_rag_doc_list, kbId={}, projectCode={}, bizModule={}, docType={}",
                kbId, projectCode, bizModule, docType);
        authService.requireToolPermission(RagMcpAuthService.PERMISSION_DOC_LIST);
        RagMcpUser user = authService.currentUser();
        if (kbId != null) {
            permissionService.requireKbView(kbId, user);
        }

        RagDocumentListRequest request = new RagDocumentListRequest();
        request.setKbId(kbId);
        request.setProjectCode(projectCode);
        request.setBizModule(bizModule);
        request.setDocType(docType);
        List<RagDocumentResponse> documents = documentService.list(request);
        return new RagMcpDocumentListResponse(true, documents.size(), documents);
    }

    @Tool(description = "按自然语言问题检索 RAG 知识库，返回命中的文档片段和引用来源")
    public RagMcpSearchResponse quna_rag_search(
            @ToolParam(description = "知识库ID") Long kbId,
            @ToolParam(description = "检索问题") String question,
            @ToolParam(description = "最终返回数量，默认 5", required = false) Integer topK,
            @ToolParam(description = "向量召回数量，默认 20", required = false) Integer vectorTopK,
            @ToolParam(description = "关键词召回数量，默认 20", required = false) Integer keywordTopK,
            @ToolParam(description = "项目编码", required = false) String projectCode,
            @ToolParam(description = "业务模块", required = false) String bizModule,
            @ToolParam(description = "文档类型", required = false) String docType) {

        log.info("MCP 工具调用: quna_rag_search, kbId={}, question={}", kbId, question);
        authService.requireToolPermission(RagMcpAuthService.PERMISSION_SEARCH);
        permissionService.requireKbView(kbId, authService.currentUser());

        RagSearchResponse search = searchService.search(queryRequest(kbId, question, topK, vectorTopK,
                keywordTopK, projectCode, bizModule, docType));
        RagMcpSearchResponse response = new RagMcpSearchResponse();
        response.setSuccess(true);
        response.setQuestion(search.getQuestion());
        response.setTotalCandidates(search.getTotalCandidates());
        response.setTotal(search.getTotal());
        response.setHits(search.getHits());
        return response;
    }

    @Tool(description = "基于 RAG 知识库回答问题，返回答案和引用来源")
    public RagMcpAskResponse quna_rag_ask(
            @ToolParam(description = "知识库ID") Long kbId,
            @ToolParam(description = "用户问题") String question,
            @ToolParam(description = "最终引用数量，默认 5", required = false) Integer topK,
            @ToolParam(description = "项目编码", required = false) String projectCode,
            @ToolParam(description = "业务模块", required = false) String bizModule,
            @ToolParam(description = "文档类型", required = false) String docType) {

        log.info("MCP 工具调用: quna_rag_ask, kbId={}, question={}", kbId, question);
        authService.requireToolPermission(RagMcpAuthService.PERMISSION_ASK);
        permissionService.requireKbView(kbId, authService.currentUser());

        RagQueryResponse ask = queryService.ask(queryRequest(kbId, question, topK, null,
                null, projectCode, bizModule, docType));
        RagMcpAskResponse response = new RagMcpAskResponse();
        response.setSuccess(true);
        response.setQuestion(ask.getQuestion());
        response.setAnswer(ask.getAnswer());
        response.setTotalCandidates(ask.getTotalCandidates());
        response.setTotal(ask.getTotal());
        response.setReferences(ask.getReferences());
        return response;
    }

    private RagQueryRequest queryRequest(Long kbId, String question, Integer topK, Integer vectorTopK,
                                         Integer keywordTopK, String projectCode, String bizModule, String docType) {
        RagQueryRequest request = new RagQueryRequest();
        request.setKbId(kbId);
        request.setQuestion(question);
        request.setTopK(topK);
        request.setVectorTopK(vectorTopK);
        request.setKeywordTopK(keywordTopK);
        request.setProjectCode(projectCode);
        request.setBizModule(bizModule);
        request.setDocType(docType);
        request.setIncludeSources(true);
        request.setEnableRerank(true);
        return request;
    }
}
