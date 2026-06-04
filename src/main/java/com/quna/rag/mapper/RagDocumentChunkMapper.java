package com.quna.rag.mapper;

import com.quna.rag.model.RagDocumentChunk;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * RAG 文档切片 Mapper。
 */
@Mapper
public interface RagDocumentChunkMapper {
    @Insert("""
            INSERT INTO rag_document_chunk(kb_id, doc_id, chunk_code, chunk_index, title, content, summary,
                                           token_count, vector_id, content_hash, metadata, keywords, status, is_deleted)
            VALUES(#{kbId}, #{docId}, #{chunkCode}, #{chunkIndex}, #{title}, #{content}, #{summary},
                   #{tokenCount}, #{vectorId}, #{contentHash}, #{metadata}, #{keywords}, #{status}, #{isDeleted})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RagDocumentChunk entity);

    @Update("UPDATE rag_document_chunk SET vector_id=#{vectorId}, update_time=CURRENT_TIMESTAMP WHERE id=#{id}")
    int updateVectorId(@Param("id") Long id, @Param("vectorId") String vectorId);

    @Select("""
            SELECT id, kb_id, doc_id, chunk_code, chunk_index, title, content, summary, token_count, vector_id,
                   content_hash, metadata, keywords, status, is_deleted, create_time, update_time
            FROM rag_document_chunk
            WHERE doc_id = #{docId} AND is_deleted = 0
            ORDER BY chunk_index ASC
            """)
    @Results(id = "ragDocumentChunkMap", value = {
            @Result(column = "kb_id", property = "kbId"),
            @Result(column = "doc_id", property = "docId"),
            @Result(column = "chunk_code", property = "chunkCode"),
            @Result(column = "chunk_index", property = "chunkIndex"),
            @Result(column = "token_count", property = "tokenCount"),
            @Result(column = "vector_id", property = "vectorId"),
            @Result(column = "content_hash", property = "contentHash"),
            @Result(column = "keyword_score", property = "keywordScore"),
            @Result(column = "doc_name", property = "docName"),
            @Result(column = "doc_type", property = "docType"),
            @Result(column = "project_code", property = "projectCode"),
            @Result(column = "biz_module", property = "bizModule"),
            @Result(column = "is_deleted", property = "isDeleted"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime")
    })
    List<RagDocumentChunk> selectByDocId(@Param("docId") Long docId);

    @Select("""
            <script>
            SELECT c.id, c.kb_id, c.doc_id, c.chunk_code, c.chunk_index, c.title, c.content, c.summary,
                   c.token_count, c.vector_id, c.content_hash, c.metadata, c.keywords, c.status, c.is_deleted,
                   c.create_time, c.update_time,
                   d.doc_name, d.doc_type, d.project_code, d.biz_module,
                   MATCH(c.title, c.content, c.keywords) AGAINST(#{query} IN NATURAL LANGUAGE MODE) AS keyword_score
            FROM rag_document_chunk c
            JOIN rag_document d ON d.id = c.doc_id AND d.is_deleted = 0
            WHERE c.is_deleted = 0 AND c.status = 1 AND c.kb_id = #{kbId}
            <if test="projectCode != null and projectCode != ''">AND d.project_code = #{projectCode}</if>
            <if test="bizModule != null and bizModule != ''">AND d.biz_module = #{bizModule}</if>
            <if test="docType != null and docType != ''">AND d.doc_type = #{docType}</if>
            AND MATCH(c.title, c.content, c.keywords) AGAINST(#{query} IN NATURAL LANGUAGE MODE)
            ORDER BY keyword_score DESC
            LIMIT #{limit}
            </script>
            """)
    @ResultMap("ragDocumentChunkMap")
    List<RagDocumentChunk> keywordSearch(@Param("kbId") Long kbId,
                                         @Param("query") String query,
                                         @Param("projectCode") String projectCode,
                                         @Param("bizModule") String bizModule,
                                         @Param("docType") String docType,
                                         @Param("limit") int limit);

    @Select("""
            <script>
            SELECT c.id, c.kb_id, c.doc_id, c.chunk_code, c.chunk_index, c.title, c.content, c.summary,
                   c.token_count, c.vector_id, c.content_hash, c.metadata, c.keywords, c.status, c.is_deleted,
                   c.create_time, c.update_time,
                   d.doc_name, d.doc_type, d.project_code, d.biz_module,
                   2.0 AS keyword_score
            FROM rag_document_chunk c
            JOIN rag_document d ON d.id = c.doc_id AND d.is_deleted = 0
            WHERE c.is_deleted = 0 AND c.status = 1 AND c.kb_id = #{kbId}
            <if test="projectCode != null and projectCode != ''">AND d.project_code = #{projectCode}</if>
            <if test="bizModule != null and bizModule != ''">AND d.biz_module = #{bizModule}</if>
            <if test="docType != null and docType != ''">AND d.doc_type = #{docType}</if>
            AND (c.title LIKE CONCAT('%', #{query}, '%')
                 OR c.content LIKE CONCAT('%', #{query}, '%')
                 OR c.keywords LIKE CONCAT('%', #{query}, '%'))
            ORDER BY c.id DESC
            LIMIT #{limit}
            </script>
            """)
    @ResultMap("ragDocumentChunkMap")
    List<RagDocumentChunk> likeSearch(@Param("kbId") Long kbId,
                                      @Param("query") String query,
                                      @Param("projectCode") String projectCode,
                                      @Param("bizModule") String bizModule,
                                      @Param("docType") String docType,
                                      @Param("limit") int limit);

    @Update("UPDATE rag_document_chunk SET is_deleted=1, update_time=CURRENT_TIMESTAMP WHERE doc_id=#{docId}")
    int logicalDeleteByDocId(@Param("docId") Long docId);
}
