package com.quna.rag.springrag.store;

import com.quna.rag.springrag.model.RagSplitConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

/**
 * 分段策略配置 Mapper，按集合读取最新生效的切片参数。
 */
@Mapper
public interface RagSplitConfigMapper {
    @Select("""
            SELECT id, collection_code, chunk_size, chunk_overlap, min_length,
                   markdown_soft_multiplier, markdown_hard_multiplier,
                   markdown_atomic_heading_level, markdown_max_merge_sections,
                   separators, protect_markdown_table, protect_code_fence,
                   protect_api_section, protect_json_block, protect_sql_block, update_time
            FROM rag_split_config
            WHERE collection_code = #{collectionCode} AND enabled = 1
            ORDER BY id DESC
            LIMIT 1
            """)
    @Results(id = "ragSplitConfigMap", value = {
            @Result(column = "collection_code", property = "collectionCode"),
            @Result(column = "chunk_size", property = "chunkSize"),
            @Result(column = "chunk_overlap", property = "chunkOverlap"),
            @Result(column = "min_length", property = "minLength"),
            @Result(column = "markdown_soft_multiplier", property = "markdownSoftMultiplier"),
            @Result(column = "markdown_hard_multiplier", property = "markdownHardMultiplier"),
            @Result(column = "markdown_atomic_heading_level", property = "markdownAtomicHeadingLevel"),
            @Result(column = "markdown_max_merge_sections", property = "markdownMaxMergeSections"),
            @Result(column = "protect_markdown_table", property = "protectMarkdownTable"),
            @Result(column = "protect_code_fence", property = "protectCodeFence"),
            @Result(column = "protect_api_section", property = "protectApiSection"),
            @Result(column = "protect_json_block", property = "protectJsonBlock"),
            @Result(column = "protect_sql_block", property = "protectSqlBlock"),
            @Result(column = "update_time", property = "updateTime")
    })
    RagSplitConfigEntity selectEnabled(@Param("collectionCode") String collectionCode);
}
