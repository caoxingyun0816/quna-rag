package com.quna.rag.service;

import com.quna.rag.common.QunaRuntimeException;
import com.quna.rag.dto.request.RagKnowledgeBaseCreateRequest;
import com.quna.rag.model.RagKnowledgeBase;
import com.quna.rag.springrag.model.RagCollectionType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库管理服务。
 * 当前版本沿用已有 business_doc / tech_doc 两个集合，后续接入 rag_knowledge_base 表后替换这里的内置映射。
 */
@Service
public class RagKnowledgeBaseService {
    private static final Long BUSINESS_KB_ID = 1L;
    private static final Long TECH_KB_ID = 2L;

    public List<RagKnowledgeBase> list() {
        return List.of(
                knowledgeBase(BUSINESS_KB_ID, RagCollectionType.BUSINESS_DOC, 1),
                knowledgeBase(TECH_KB_ID, RagCollectionType.TECH_DOC, 2)
        );
    }

    public RagKnowledgeBase create(RagKnowledgeBaseCreateRequest request) {
        throw new QunaRuntimeException("知识库创建接口已预留，当前版本请使用内置 business_doc / tech_doc");
    }

    public String collectionCode(Long kbId) {
        if (kbId == null || BUSINESS_KB_ID.equals(kbId)) {
            return RagCollectionType.BUSINESS_DOC.getCode();
        }
        if (TECH_KB_ID.equals(kbId)) {
            return RagCollectionType.TECH_DOC.getCode();
        }
        throw new QunaRuntimeException("不支持的知识库ID: " + kbId);
    }

    public Long kbId(String collectionCode) {
        if (StringUtils.equalsIgnoreCase(collectionCode, RagCollectionType.TECH_DOC.getCode())) {
            return TECH_KB_ID;
        }
        return BUSINESS_KB_ID;
    }

    private RagKnowledgeBase knowledgeBase(Long id, RagCollectionType type, Integer kbType) {
        RagKnowledgeBase kb = new RagKnowledgeBase();
        kb.setId(id);
        kb.setKbCode(type.getCode());
        kb.setKbName(type.getLabel());
        kb.setKbType(kbType);
        kb.setDescription(type.getLabel());
        kb.setStatus(1);
        kb.setIsDeleted(0);
        kb.setCreateTime(LocalDateTime.now());
        kb.setUpdateTime(LocalDateTime.now());
        return kb;
    }
}
