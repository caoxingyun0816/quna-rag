package com.quna.rag.springrag.store;

import com.quna.rag.springrag.model.RagChunkEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;
/**
 * 切片表 MyBatis Mapper，负责切片写入、关键词检索和按文档 ID 查询。
 */

@Mapper
public interface RagChunkMapper {
    @Insert("""
            INSERT INTO rag_chunk(doc_id, collection_code, chunk_index, title_path, content, content_hash, keywords)
            VALUES(#{docId}, #{collectionCode}, #{chunkIndex}, #{titlePath}, #{content}, #{contentHash}, #{keywords})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RagChunkEntity entity);

    @Select("""
            <script>
            SELECT c.id, c.doc_id, c.collection_code, c.chunk_index, c.title_path, c.content,
                   c.content_hash, c.keywords, c.create_time,
                   d.filename, d.file_type, d.project, d.module, d.doc_type, d.tags,
                   MATCH(c.content, c.keywords) AGAINST(#{query} IN NATURAL LANGUAGE MODE) AS keyword_score
            FROM rag_chunk c
            JOIN rag_document d ON d.id = c.doc_id
            WHERE c.collection_code = #{collectionCode}
            <if test="project != null and project != ''">AND d.project = #{project}</if>
            <if test="module != null and module != ''">AND d.module = #{module}</if>
            <if test="docType != null and docType != ''">AND d.doc_type = #{docType}</if>
            AND MATCH(c.content, c.keywords) AGAINST(#{query} IN NATURAL LANGUAGE MODE)
            ORDER BY MATCH(c.content, c.keywords) AGAINST(#{query} IN NATURAL LANGUAGE MODE) DESC
            LIMIT #{limit}
            </script>
            """)
    @Results(id = "ragChunkMap", value = {
            @Result(column = "doc_id", property = "docId"),
            @Result(column = "collection_code", property = "collectionCode"),
            @Result(column = "chunk_index", property = "chunkIndex"),
            @Result(column = "title_path", property = "titlePath"),
            @Result(column = "content_hash", property = "contentHash"),
            @Result(column = "keyword_score", property = "keywordScore"),
            @Result(column = "file_type", property = "fileType"),
            @Result(column = "doc_type", property = "docType"),
            @Result(column = "create_time", property = "createTime")
    })
    List<RagChunkEntity> keywordSearch(@Param("collectionCode") String collectionCode,
                                       @Param("query") String query,
                                       @Param("project") String project,
                                       @Param("module") String module,
                                       @Param("docType") String docType,
                                       @Param("limit") int limit);

    @Select("""
            <script>
            SELECT c.id, c.doc_id, c.collection_code, c.chunk_index, c.title_path, c.content,
                   c.content_hash, c.keywords, c.create_time,
                   d.filename, d.file_type, d.project, d.module, d.doc_type, d.tags,
                   0.1 AS keyword_score
            FROM rag_chunk c
            JOIN rag_document d ON d.id = c.doc_id
            WHERE c.collection_code = #{collectionCode}
            <if test="project != null and project != ''">AND d.project = #{project}</if>
            <if test="module != null and module != ''">AND d.module = #{module}</if>
            <if test="docType != null and docType != ''">AND d.doc_type = #{docType}</if>
            AND (c.content LIKE CONCAT('%', #{query}, '%') OR c.keywords LIKE CONCAT('%', #{query}, '%'))
            ORDER BY c.id DESC
            LIMIT #{limit}
            </script>
            """)
    @ResultMap("ragChunkMap")
    List<RagChunkEntity> likeSearch(@Param("collectionCode") String collectionCode,
                                    @Param("query") String query,
                                    @Param("project") String project,
                                    @Param("module") String module,
                                    @Param("docType") String docType,
                                    @Param("limit") int limit);
}
