package com.quna.rag.dto.request;

import lombok.Data;

/**
 * 知识库权限保存请求。
 */
@Data
public class RagKbPermissionSaveRequest {
    private Long kbId;
    private String subjectType;
    private String subjectId;
    private String permissionType;
}
