package com.quna.rag.mapper;

import com.quna.rag.entity.RagKnowledgeBase;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * RAG 知识库 Mapper。
 */
@Mapper
public interface RagKnowledgeBaseMapper {
    @Insert("""
            INSERT INTO rag_knowledge_base(kb_code, kb_name, kb_type, description, status, is_deleted, create_user, update_user)
            VALUES(#{kbCode}, #{kbName}, #{kbType}, #{description}, #{status}, #{isDeleted}, #{createUser}, #{updateUser})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RagKnowledgeBase entity);

    @Select("""
            <script>
            SELECT id, kb_code, kb_name, kb_type, description, status, is_deleted, create_user, update_user,
                   create_time, update_time
            FROM rag_knowledge_base
            WHERE is_deleted = 0
            <if test="kbCode != null and kbCode != ''">AND kb_code LIKE CONCAT('%', #{kbCode}, '%')</if>
            <if test="kbName != null and kbName != ''">AND kb_name LIKE CONCAT('%', #{kbName}, '%')</if>
            <if test="kbType != null">AND kb_type = #{kbType}</if>
            <if test="status != null">AND status = #{status}</if>
            ORDER BY id ASC
            </script>
            """)
    @Results(id = "ragKnowledgeBaseMap", value = {
            @Result(column = "kb_code", property = "kbCode"),
            @Result(column = "kb_name", property = "kbName"),
            @Result(column = "kb_type", property = "kbType"),
            @Result(column = "is_deleted", property = "isDeleted"),
            @Result(column = "create_user", property = "createUser"),
            @Result(column = "update_user", property = "updateUser"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime")
    })
    List<RagKnowledgeBase> selectList(@Param("kbCode") String kbCode,
                                      @Param("kbName") String kbName,
                                      @Param("kbType") Integer kbType,
                                      @Param("status") Integer status);

    @Select("""
            SELECT id, kb_code, kb_name, kb_type, description, status, is_deleted, create_user, update_user,
                   create_time, update_time
            FROM rag_knowledge_base
            WHERE id = #{id} AND is_deleted = 0
            """)
    @ResultMap("ragKnowledgeBaseMap")
    RagKnowledgeBase selectById(@Param("id") Long id);

    @Select("""
            SELECT id, kb_code, kb_name, kb_type, description, status, is_deleted, create_user, update_user,
                   create_time, update_time
            FROM rag_knowledge_base
            WHERE kb_code = #{kbCode} AND is_deleted = 0
            """)
    @ResultMap("ragKnowledgeBaseMap")
    RagKnowledgeBase selectByCode(@Param("kbCode") String kbCode);

    @Update("""
            UPDATE rag_knowledge_base
            SET kb_code=#{kbCode}, kb_name=#{kbName}, kb_type=#{kbType}, description=#{description},
                update_user=#{updateUser}, update_time=CURRENT_TIMESTAMP
            WHERE id=#{id} AND is_deleted = 0
            """)
    int update(RagKnowledgeBase entity);

    @Update("""
            UPDATE rag_knowledge_base
            SET status=#{status}, update_user=#{updateUser}, update_time=CURRENT_TIMESTAMP
            WHERE id=#{id} AND is_deleted = 0
            """)
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateUser") String updateUser);
}
