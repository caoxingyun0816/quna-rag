package com.quna.rag.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * RAG 知识库权限 Mapper。
 * 当前版本只预留权限表，查询侧默认全部可见。
 */
@Mapper
public interface RagKbPermissionMapper {
    @Select("""
            SELECT COUNT(1)
            FROM rag_kb_permission
            WHERE kb_id=#{kbId} AND subject_type=#{subjectType} AND subject_id=#{subjectId}
            """)
    int countPermission(@Param("kbId") Long kbId,
                        @Param("subjectType") Integer subjectType,
                        @Param("subjectId") String subjectId);
}
