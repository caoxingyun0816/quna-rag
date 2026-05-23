package com.quna.rag.mapper;

import com.quna.rag.entity.Document;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DocMapper {
    @Insert("""
            INSERT INTO document(filename,user_id,source,status,file_type,file_size,chunk_count,permission_scope)
            VALUES(#{filename},#{userId},#{source},#{status},#{fileType},#{fileSize},#{chunkCount},#{permissionScope})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Document doc);

    @Select("""
            <script>
            SELECT id, filename, user_id, source, status, file_type, file_size, chunk_count, permission_scope, create_time, update_time
            FROM document
            WHERE user_id = #{userId}
            <if test="keyword != null and keyword != ''">
                AND filename LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test="source != null and source != ''">
                AND source = #{source}
            </if>
            <if test="status != null and status != ''">
                AND status = #{status}
            </if>
            ORDER BY COALESCE(update_time, create_time) DESC, id DESC
            </script>
            """)
    @Results(id = "docMap", value = {
            @Result(column = "user_id", property = "userId"),
            @Result(column = "file_type", property = "fileType"),
            @Result(column = "file_size", property = "fileSize"),
            @Result(column = "chunk_count", property = "chunkCount"),
            @Result(column = "permission_scope", property = "permissionScope"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime")
    })
    List<Document> selectByUser(@Param("userId") Long userId,
                                @Param("keyword") String keyword,
                                @Param("source") String source,
                                @Param("status") String status);

    @Select("SELECT id, filename, user_id, source, status, file_type, file_size, chunk_count, permission_scope, create_time, update_time FROM document WHERE id = #{id} AND user_id = #{userId}")
    @ResultMap("docMap")
    Document selectByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE document SET status = #{status}, update_time = CURRENT_TIMESTAMP WHERE id = #{id} AND user_id = #{userId}")
    int updateStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("status") String status);

    @Update("UPDATE document SET permission_scope = #{permissionScope}, update_time = CURRENT_TIMESTAMP WHERE id = #{id} AND user_id = #{userId}")
    int updatePermissionScope(@Param("id") Long id, @Param("userId") Long userId, @Param("permissionScope") String permissionScope);
}
