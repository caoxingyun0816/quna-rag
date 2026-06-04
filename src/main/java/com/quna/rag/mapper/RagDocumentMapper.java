package com.quna.rag.mapper;

import com.quna.rag.entity.RagDocument;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * RAG 文档 Mapper。
 */
@Mapper
public interface RagDocumentMapper {
    @Insert("""
            INSERT INTO rag_document(kb_id, doc_code, doc_name, doc_type, biz_module, project_code, source_type,
                                     source_url, file_url, file_size, file_md5, parse_status, vector_status,
                                     chunk_count, error_msg, version_no, status, is_deleted, create_user, update_user)
            VALUES(#{kbId}, #{docCode}, #{docName}, #{docType}, #{bizModule}, #{projectCode}, #{sourceType},
                   #{sourceUrl}, #{fileUrl}, #{fileSize}, #{fileMd5}, #{parseStatus}, #{vectorStatus},
                   #{chunkCount}, #{errorMsg}, #{versionNo}, #{status}, #{isDeleted}, #{createUser}, #{updateUser})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RagDocument entity);

    @Select("""
            SELECT id, kb_id, doc_code, doc_name, doc_type, biz_module, project_code, source_type, source_url,
                   file_url, file_size, file_md5, parse_status, vector_status, chunk_count, error_msg,
                   version_no, status, is_deleted, create_user, update_user, create_time, update_time
            FROM rag_document
            WHERE id = #{id} AND is_deleted = 0
            """)
    @Results(id = "ragDocumentMap", value = {
            @Result(column = "kb_id", property = "kbId"),
            @Result(column = "doc_code", property = "docCode"),
            @Result(column = "doc_name", property = "docName"),
            @Result(column = "doc_type", property = "docType"),
            @Result(column = "biz_module", property = "bizModule"),
            @Result(column = "project_code", property = "projectCode"),
            @Result(column = "source_type", property = "sourceType"),
            @Result(column = "source_url", property = "sourceUrl"),
            @Result(column = "file_url", property = "fileUrl"),
            @Result(column = "file_size", property = "fileSize"),
            @Result(column = "file_md5", property = "fileMd5"),
            @Result(column = "parse_status", property = "parseStatus"),
            @Result(column = "vector_status", property = "vectorStatus"),
            @Result(column = "chunk_count", property = "chunkCount"),
            @Result(column = "error_msg", property = "errorMsg"),
            @Result(column = "version_no", property = "versionNo"),
            @Result(column = "is_deleted", property = "isDeleted"),
            @Result(column = "create_user", property = "createUser"),
            @Result(column = "update_user", property = "updateUser"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime")
    })
    RagDocument selectById(@Param("id") Long id);

    @Select("""
            SELECT id, kb_id, doc_code, doc_name, doc_type, biz_module, project_code, source_type, source_url,
                   file_url, file_size, file_md5, parse_status, vector_status, chunk_count, error_msg,
                   version_no, status, is_deleted, create_user, update_user, create_time, update_time
            FROM rag_document
            WHERE kb_id = #{kbId} AND file_md5 = #{fileMd5} AND is_deleted = 0
            ORDER BY id DESC
            LIMIT 1
            """)
    @ResultMap("ragDocumentMap")
    RagDocument selectByKbAndFileMd5(@Param("kbId") Long kbId, @Param("fileMd5") String fileMd5);

    @Select("""
            <script>
            SELECT id, kb_id, doc_code, doc_name, doc_type, biz_module, project_code, source_type, source_url,
                   file_url, file_size, file_md5, parse_status, vector_status, chunk_count, error_msg,
                   version_no, status, is_deleted, create_user, update_user, create_time, update_time
            FROM rag_document
            WHERE is_deleted = 0
            <if test="kbId != null">AND kb_id = #{kbId}</if>
            <if test="projectCode != null and projectCode != ''">AND project_code = #{projectCode}</if>
            <if test="bizModule != null and bizModule != ''">AND biz_module = #{bizModule}</if>
            <if test="docType != null and docType != ''">AND doc_type = #{docType}</if>
            ORDER BY id DESC
            </script>
            """)
    @ResultMap("ragDocumentMap")
    List<RagDocument> selectList(@Param("kbId") Long kbId,
                                 @Param("projectCode") String projectCode,
                                 @Param("bizModule") String bizModule,
                                 @Param("docType") String docType);

    @Update("""
            UPDATE rag_document
            SET parse_status=#{parseStatus}, vector_status=#{vectorStatus}, error_msg=#{errorMsg}, update_time=CURRENT_TIMESTAMP
            WHERE id=#{id}
            """)
    int updateBuildStatus(@Param("id") Long id,
                          @Param("parseStatus") Integer parseStatus,
                          @Param("vectorStatus") Integer vectorStatus,
                          @Param("errorMsg") String errorMsg);

    @Update("""
            UPDATE rag_document
            SET parse_status=#{parseStatus}, vector_status=#{vectorStatus}, chunk_count=#{chunkCount},
                error_msg=#{errorMsg}, update_time=CURRENT_TIMESTAMP
            WHERE id=#{id}
            """)
    int updateIndexed(@Param("id") Long id,
                      @Param("parseStatus") Integer parseStatus,
                      @Param("vectorStatus") Integer vectorStatus,
                      @Param("chunkCount") Integer chunkCount,
                      @Param("errorMsg") String errorMsg);

    @Update("UPDATE rag_document SET is_deleted=1, update_time=CURRENT_TIMESTAMP WHERE id=#{id}")
    int logicalDelete(@Param("id") Long id);
}
