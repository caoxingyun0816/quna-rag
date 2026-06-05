package com.quna.rag.service;

import com.quna.rag.common.QunaRuntimeException;
import com.quna.rag.dto.request.RagKnowledgeBaseCreateRequest;
import com.quna.rag.dto.request.RagKnowledgeBaseListRequest;
import com.quna.rag.dto.request.RagKbPermissionSaveRequest;
import com.quna.rag.mapper.RagKbPermissionMapper;
import com.quna.rag.mapper.RagKnowledgeBaseMapper;
import com.quna.rag.entity.RagKnowledgeBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识库管理服务。
 * 当前版本沿用已有 business_doc / tech_doc 两个集合，后续接入 rag_knowledge_base 表后替换这里的内置映射。
 */
@Service
public class RagKnowledgeBaseService {
    private final RagKnowledgeBaseMapper mapper;
    private final RagKbPermissionMapper permissionMapper;

    public RagKnowledgeBaseService(RagKnowledgeBaseMapper mapper, RagKbPermissionMapper permissionMapper) {
        this.mapper = mapper;
        this.permissionMapper = permissionMapper;
    }

    public List<RagKnowledgeBase> list(RagKnowledgeBaseListRequest request) {
        String kbCode = request == null ? null : request.getKbCode();
        String kbName = request == null ? null : request.getKbName();
        Integer kbType = request == null ? null : request.getKbType();
        Integer status = request == null ? null : request.getStatus();
        return mapper.selectList(StringUtils.trimToNull(kbCode), StringUtils.trimToNull(kbName), kbType, status);
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

    public RagKnowledgeBase update(Long id, RagKnowledgeBaseCreateRequest request) {
        if (id == null || request == null) {
            throw new QunaRuntimeException("参数错误");
        }
        if (StringUtils.isBlank(request.getKbCode()) || StringUtils.isBlank(request.getKbName())) {
            throw new QunaRuntimeException("kbCode 和 kbName 不能为空");
        }
        RagKnowledgeBase old = mapper.selectById(id);
        if (old == null) {
            throw new QunaRuntimeException("知识库不存在");
        }
        RagKnowledgeBase existing = mapper.selectByCode(request.getKbCode());
        if (existing != null && !existing.getId().equals(id)) {
            throw new QunaRuntimeException("知识库编码已存在");
        }
        old.setKbCode(request.getKbCode());
        old.setKbName(request.getKbName());
        old.setKbType(request.getKbType() == null ? 1 : request.getKbType());
        old.setDescription(request.getDescription());
        mapper.update(old);
        return mapper.selectById(id);
    }

    public boolean updateStatus(Long id, Integer status) {
        if (id == null || status == null || (status != 0 && status != 1)) {
            throw new QunaRuntimeException("状态参数错误");
        }
        mapper.updateStatus(id, status, null);
        return true;
    }

    @Transactional
    public boolean savePermission(RagKbPermissionSaveRequest request) {
        if (request == null || request.getKbId() == null || StringUtils.isBlank(request.getSubjectId())) {
            throw new QunaRuntimeException("权限参数不完整");
        }
        require(request.getKbId());
        Integer subjectType = subjectType(request.getSubjectType());
        Integer permissionType = permissionType(request.getPermissionType());
        permissionMapper.deleteSubjectPermission(request.getKbId(), subjectType, request.getSubjectId());
        permissionMapper.insertPermission(request.getKbId(), subjectType, request.getSubjectId(), permissionType);
        return true;
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

    private Integer subjectType(String subjectType) {
        if ("ROLE".equalsIgnoreCase(subjectType)) {
            return 2;
        }
        if ("DEPT".equalsIgnoreCase(subjectType) || "DEPARTMENT".equalsIgnoreCase(subjectType)) {
            return 3;
        }
        return 1;
    }

    private Integer permissionType(String permissionType) {
        if ("WRITE".equalsIgnoreCase(permissionType) || "UPLOAD".equalsIgnoreCase(permissionType)) {
            return 2;
        }
        if ("ADMIN".equalsIgnoreCase(permissionType) || "MANAGE".equalsIgnoreCase(permissionType)) {
            return 3;
        }
        return 1;
    }
}
