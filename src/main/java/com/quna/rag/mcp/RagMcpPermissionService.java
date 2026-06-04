package com.quna.rag.mcp;

import com.quna.rag.mapper.RagKbPermissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * MCP 知识库权限服务。
 * 当前规则：知识库没有任何权限配置时默认全部登录用户可见；一旦配置权限，则按用户或角色匹配。
 */
@Service
public class RagMcpPermissionService {
    private static final int SUBJECT_USER = 1;
    private static final int SUBJECT_ROLE = 2;
    private static final int PERMISSION_VIEW = 1;

    private final RagKbPermissionMapper permissionMapper;

    public RagMcpPermissionService(RagKbPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public void requireKbView(Long kbId, RagMcpUser user) {
        if (kbId == null) {
            throw new IllegalArgumentException("kbId 不能为空");
        }
        if (user == null || user.isTestUser()) {
            return;
        }
        int total = permissionMapper.countByKbId(kbId);
        if (total == 0) {
            return;
        }
        if (StringUtils.hasText(user.getUserId())
                && permissionMapper.countPermission(kbId, SUBJECT_USER, user.getUserId(), PERMISSION_VIEW) > 0) {
            return;
        }
        if (StringUtils.hasText(user.getRole())
                && permissionMapper.countPermission(kbId, SUBJECT_ROLE, user.getRole(), PERMISSION_VIEW) > 0) {
            return;
        }
        throw new IllegalStateException("无权访问知识库: " + kbId);
    }
}
