package com.quna.rag.service;

import com.quna.rag.common.QunaRuntimeException;
import com.quna.rag.dto.request.RagKnowledgeBaseCreateRequest;
import com.quna.rag.mapper.RagKnowledgeBaseMapper;
import com.quna.rag.model.RagKnowledgeBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识库管理服务。
 * 当前版本沿用已有 business_doc / tech_doc 两个集合，后续接入 rag_knowledge_base 表后替换这里的内置映射。
 */
@Service
public class RagKnowledgeBaseService {
    private final RagKnowledgeBaseMapper mapper;

    public RagKnowledgeBaseService(RagKnowledgeBaseMapper mapper) {
        this.mapper = mapper;
    }

    public List<RagKnowledgeBase> list() {
        return mapper.selectList();
    }

    public RagKnowledgeBase create(RagKnowledgeBaseCreateRequest request) {
        if (request == null || StringUtils.isBlank(request.getKbCode()) || StringUtils.isBlank(request.getKbName())) {
            throw new QunaRuntimeException("kbCode 和 kbName 不能为空");
        }
        RagKnowledgeBase existing = mapper.selectByCode(request.getKbCode());
        if (existing != null) {
            return existing;
        }
        RagKnowledgeBase kb = new RagKnowledgeBase();
        kb.setKbCode(request.getKbCode());
        kb.setKbName(request.getKbName());
        kb.setKbType(request.getKbType() == null ? 1 : request.getKbType());
        kb.setDescription(request.getDescription());
        kb.setStatus(1);
        kb.setIsDeleted(0);
        mapper.insert(kb);
        return kb;
    }

    public RagKnowledgeBase require(Long kbId) {
        if (kbId == null) {
            throw new QunaRuntimeException("kbId 不能为空");
        }
        RagKnowledgeBase kb = mapper.selectById(kbId);
        if (kb == null || kb.getStatus() == null || kb.getStatus() != 1) {
            throw new QunaRuntimeException("知识库不存在或已禁用: " + kbId);
        }
        return kb;
    }
}
