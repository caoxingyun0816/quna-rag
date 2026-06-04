package com.quna.rag.mapper;

import com.quna.rag.model.RagQueryLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * RAG 查询日志 Mapper。
 */
@Mapper
public interface RagQueryLogMapper {
    @Insert("""
            INSERT INTO rag_query_log(user_id, user_name, kb_id, question, rewritten_question, answer,
                                      hit_chunk_ids, model_name, embedding_model, cost_time_ms, success, error_msg)
            VALUES(#{userId}, #{userName}, #{kbId}, #{question}, #{rewrittenQuestion}, #{answer},
                   #{hitChunkIds}, #{modelName}, #{embeddingModel}, #{costTimeMs}, #{success}, #{errorMsg})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RagQueryLog entity);

    @Select("""
            SELECT id, user_id, user_name, kb_id, question, rewritten_question, answer, hit_chunk_ids,
                   model_name, embedding_model, cost_time_ms, success, error_msg, create_time
            FROM rag_query_log
            WHERE (#{kbId} IS NULL OR kb_id = #{kbId})
            ORDER BY id DESC
            LIMIT #{limit}
            """)
    @Results(id = "ragQueryLogMap", value = {
            @Result(column = "user_id", property = "userId"),
            @Result(column = "user_name", property = "userName"),
            @Result(column = "kb_id", property = "kbId"),
            @Result(column = "rewritten_question", property = "rewrittenQuestion"),
            @Result(column = "hit_chunk_ids", property = "hitChunkIds"),
            @Result(column = "model_name", property = "modelName"),
            @Result(column = "embedding_model", property = "embeddingModel"),
            @Result(column = "cost_time_ms", property = "costTimeMs"),
            @Result(column = "error_msg", property = "errorMsg"),
            @Result(column = "create_time", property = "createTime")
    })
    List<RagQueryLog> selectRecent(@Param("kbId") Long kbId, @Param("limit") int limit);
}
