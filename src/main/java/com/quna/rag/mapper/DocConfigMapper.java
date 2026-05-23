package com.quna.rag.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface DocConfigMapper {
    @Select("SELECT user_id userId, chunk_size chunkSize, chunk_overlap chunkOverlap, separators, enable_title_enhance enableTitleEnhance, update_time updateTime FROM doc_chunk_config WHERE user_id = #{userId} ORDER BY id DESC LIMIT 1")
    Map<String, Object> selectChunkConfig(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO doc_chunk_config(user_id, chunk_size, chunk_overlap, separators, enable_title_enhance)
            VALUES(#{userId}, #{chunkSize}, #{chunkOverlap}, #{separators}, #{enableTitleEnhance})
            """)
    int insertChunkConfig(Map<String, Object> config);

    @Select("SELECT user_id userId, provider, model, dimension, endpoint, enabled, update_time updateTime FROM vector_model_config WHERE user_id = #{userId} ORDER BY id DESC LIMIT 1")
    Map<String, Object> selectVectorConfig(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO vector_model_config(user_id, provider, model, dimension, endpoint, enabled)
            VALUES(#{userId}, #{provider}, #{model}, #{dimension}, #{endpoint}, #{enabled})
            """)
    int insertVectorConfig(Map<String, Object> config);

    @Select("""
            SELECT p.id, p.document_id documentId, d.filename documentName, p.subject_type subjectType,
                   p.subject_value subjectValue, p.permission, p.create_time createTime
            FROM document_permission p
            LEFT JOIN document d ON d.id = p.document_id
            WHERE d.user_id = #{userId}
            ORDER BY p.id DESC
            """)
    List<Map<String, Object>> selectPermissions(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO document_permission(document_id, subject_type, subject_value, permission)
            VALUES(#{documentId}, #{subjectType}, #{subjectValue}, #{permission})
            """)
    int insertPermission(Map<String, Object> permission);
}
