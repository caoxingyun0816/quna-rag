package com.quna.rag.mapper;

import org.apache.ibatis.annotations.*;

/**
 * RAG 知识库权限 Mapper。
 * 当前版本只预留权限表，查询侧默认全部可见。
 */
@Mapper
public interface RagKbPermissionMapper {
    @Select("SELECT COUNT(1) FROM rag_kb_permission WHERE kb_id=#{kbId}")
    int countByKbId(@Param("kbId") Long kbId);

    @Select("""
            SELECT COUNT(1)
            FROM rag_kb_permission
            WHERE kb_id=#{kbId}
              AND subject_type=#{subjectType}
              AND subject_id=#{subjectId}
              AND permission_type >= #{permissionType}
            """)
    int countPermission(@Param("kbId") Long kbId,
                        @Param("subjectType") Integer subjectType,
                        @Param("subjectId") String subjectId,
                        @Param("permissionType") Integer permissionType);

    @Delete("""
            DELETE FROM rag_kb_permission
            WHERE kb_id=#{kbId} AND subject_type=#{subjectType} AND subject_id=#{subjectId}
            """)
    int deleteSubjectPermission(@Param("kbId") Long kbId,
                                @Param("subjectType") Integer subjectType,
                                @Param("subjectId") String subjectId);

    @Insert("""
            INSERT INTO rag_kb_permission(kb_id, subject_type, subject_id, permission_type)
            VALUES(#{kbId}, #{subjectType}, #{subjectId}, #{permissionType})
            """)
    int insertPermission(@Param("kbId") Long kbId,
                         @Param("subjectType") Integer subjectType,
                         @Param("subjectId") String subjectId,
                         @Param("permissionType") Integer permissionType);
}
