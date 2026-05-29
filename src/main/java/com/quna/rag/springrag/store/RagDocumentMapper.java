package com.quna.rag.springrag.store;

import com.quna.rag.springrag.model.RagDocumentEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RagDocumentMapper {
    @Insert("""
            INSERT INTO rag_document(collection_code, filename, file_type, source, project, module, doc_type, tags,
                                     permission_scope, visible_roles, status, chunk_count, file_size, content_hash)
            VALUES(#{collectionCode}, #{filename}, #{fileType}, #{source}, #{project}, #{module}, #{docType}, #{tags},
                   #{permissionScope}, #{visibleRoles}, #{status}, #{chunkCount}, #{fileSize}, #{contentHash})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RagDocumentEntity entity);

    @Update("UPDATE rag_document SET chunk_count=#{chunkCount}, status=#{status}, update_time=CURRENT_TIMESTAMP WHERE id=#{id}")
    int updateStatusAndChunkCount(@Param("id") Long id, @Param("status") String status, @Param("chunkCount") int chunkCount);

    @Select("""
            SELECT id, collection_code, filename, file_type, source, project, module, doc_type, tags,
                   permission_scope, visible_roles, status, chunk_count, file_size, content_hash, create_time, update_time
            FROM rag_document
            WHERE collection_code = #{collectionCode}
              AND content_hash = #{contentHash}
            ORDER BY id DESC
            LIMIT 1
            """)
    @ResultMap("ragDocMap")
    RagDocumentEntity selectByCollectionAndContentHash(@Param("collectionCode") String collectionCode,
                                                       @Param("contentHash") String contentHash);

    @Select("""
            <script>
            SELECT id, collection_code, filename, file_type, source, project, module, doc_type, tags,
                   permission_scope, visible_roles, status, chunk_count, file_size, content_hash, create_time, update_time
            FROM rag_document
            WHERE 1=1
            <if test="collectionCode != null and collectionCode != ''">AND collection_code = #{collectionCode}</if>
            <if test="project != null and project != ''">AND project = #{project}</if>
            <if test="module != null and module != ''">AND module = #{module}</if>
            <if test="docType != null and docType != ''">AND doc_type = #{docType}</if>
            ORDER BY id DESC
            </script>
            """)
    @Results(id = "ragDocMap", value = {
            @Result(column = "collection_code", property = "collectionCode"),
            @Result(column = "file_type", property = "fileType"),
            @Result(column = "doc_type", property = "docType"),
            @Result(column = "permission_scope", property = "permissionScope"),
            @Result(column = "visible_roles", property = "visibleRoles"),
            @Result(column = "chunk_count", property = "chunkCount"),
            @Result(column = "file_size", property = "fileSize"),
            @Result(column = "content_hash", property = "contentHash"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime")
    })
    List<RagDocumentEntity> selectList(@Param("collectionCode") String collectionCode,
                                       @Param("project") String project,
                                       @Param("module") String module,
                                       @Param("docType") String docType);
}
