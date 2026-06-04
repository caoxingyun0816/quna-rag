CREATE DATABASE IF NOT EXISTS rag_enterprise DEFAULT CHARSET utf8mb4;
USE rag_enterprise;

CREATE TABLE IF NOT EXISTS rag_knowledge_base (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    kb_code VARCHAR(64) NOT NULL COMMENT '知识库编码',
    kb_name VARCHAR(128) NOT NULL COMMENT '知识库名称',
    kb_type TINYINT NOT NULL DEFAULT 1 COMMENT '知识库类型：1-业务知识库 2-技术知识库 3-接口知识库 4-代码知识库',
    description VARCHAR(512) DEFAULT NULL COMMENT '描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
    create_user VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    update_user VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_rag_kb_code (kb_code),
    KEY idx_rag_kb_status (status, is_deleted)
) COMMENT='RAG知识库表';

CREATE TABLE IF NOT EXISTS rag_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    kb_id BIGINT NOT NULL COMMENT '知识库ID',
    doc_code VARCHAR(64) NOT NULL COMMENT '文档编码',
    doc_name VARCHAR(255) NOT NULL COMMENT '文档名称',
    doc_type VARCHAR(32) NOT NULL COMMENT '文档类型：md/pdf/docx/xlsx/txt/html/code',
    biz_module VARCHAR(128) DEFAULT NULL COMMENT '业务模块',
    project_code VARCHAR(128) DEFAULT NULL COMMENT '项目编码',
    source_type TINYINT NOT NULL DEFAULT 1 COMMENT '来源类型：1-手动上传 2-Git同步 3-接口同步 4-数据库同步',
    source_url VARCHAR(512) DEFAULT NULL COMMENT '来源地址',
    file_url VARCHAR(1000) DEFAULT NULL COMMENT '文件OSS地址',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小',
    file_md5 VARCHAR(64) DEFAULT NULL COMMENT '文件MD5/SHA256',
    parse_status TINYINT NOT NULL DEFAULT 0 COMMENT '解析状态：0-待解析 1-解析中 2-成功 3-失败',
    vector_status TINYINT NOT NULL DEFAULT 0 COMMENT '向量状态：0-待向量化 1-向量化中 2-成功 3-失败',
    chunk_count INT DEFAULT 0 COMMENT '切片数量',
    error_msg TEXT DEFAULT NULL COMMENT '失败原因',
    version_no INT NOT NULL DEFAULT 1 COMMENT '文档版本号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
    create_user VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    update_user VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_rag_doc_code (doc_code),
    UNIQUE KEY uk_rag_doc_kb_hash (kb_id, file_md5),
    KEY idx_rag_doc_kb_id (kb_id),
    KEY idx_rag_doc_project_module (project_code, biz_module),
    KEY idx_rag_doc_parse_status (parse_status),
    KEY idx_rag_doc_vector_status (vector_status),
    KEY idx_rag_doc_deleted (is_deleted)
) COMMENT='RAG文档表';

CREATE TABLE IF NOT EXISTS rag_document_chunk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    kb_id BIGINT NOT NULL COMMENT '知识库ID',
    doc_id BIGINT NOT NULL COMMENT '文档ID',
    chunk_code VARCHAR(64) NOT NULL COMMENT '切片编码',
    chunk_index INT NOT NULL COMMENT '切片序号',
    title VARCHAR(500) DEFAULT NULL COMMENT '标题路径',
    content MEDIUMTEXT NOT NULL COMMENT '切片内容',
    summary VARCHAR(1024) DEFAULT NULL COMMENT '切片摘要',
    token_count INT DEFAULT 0 COMMENT 'token数量',
    vector_id VARCHAR(128) DEFAULT NULL COMMENT '向量库ID',
    content_hash VARCHAR(64) DEFAULT NULL COMMENT '内容hash',
    metadata JSON DEFAULT NULL COMMENT '元数据',
    keywords VARCHAR(1000) DEFAULT NULL COMMENT '关键词',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_rag_chunk_code (chunk_code),
    KEY idx_rag_chunk_doc_id (doc_id),
    KEY idx_rag_chunk_kb_id (kb_id),
    KEY idx_rag_chunk_vector_id (vector_id),
    KEY idx_rag_chunk_deleted (is_deleted),
    FULLTEXT KEY ft_rag_chunk_content_keywords (title, content, keywords)
) COMMENT='RAG文档切片表';

CREATE TABLE IF NOT EXISTS rag_query_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id VARCHAR(64) DEFAULT NULL COMMENT '用户ID',
    user_name VARCHAR(64) DEFAULT NULL COMMENT '用户名称',
    kb_id BIGINT DEFAULT NULL COMMENT '知识库ID',
    question TEXT NOT NULL COMMENT '用户问题',
    rewritten_question TEXT DEFAULT NULL COMMENT '改写后问题',
    answer MEDIUMTEXT DEFAULT NULL COMMENT '模型回答',
    hit_chunk_ids TEXT DEFAULT NULL COMMENT '命中的切片ID',
    model_name VARCHAR(128) DEFAULT NULL COMMENT '模型名称',
    embedding_model VARCHAR(128) DEFAULT NULL COMMENT '向量模型',
    cost_time_ms BIGINT DEFAULT NULL COMMENT '耗时毫秒',
    success TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功：0-失败 1-成功',
    error_msg TEXT DEFAULT NULL COMMENT '失败原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_rag_query_user_id (user_id),
    KEY idx_rag_query_kb_id (kb_id),
    KEY idx_rag_query_create_time (create_time)
) COMMENT='RAG查询日志表';

CREATE TABLE IF NOT EXISTS rag_kb_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    kb_id BIGINT NOT NULL COMMENT '知识库ID',
    subject_type TINYINT NOT NULL COMMENT '授权对象：1-用户 2-角色 3-部门',
    subject_id VARCHAR(64) NOT NULL COMMENT '授权对象ID',
    permission_type TINYINT NOT NULL DEFAULT 1 COMMENT '权限：1-查看 2-上传 3-管理',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_rag_kb_subject (kb_id, subject_type, subject_id)
) COMMENT='RAG知识库权限表';

INSERT INTO rag_knowledge_base(kb_code, kb_name, kb_type, description)
VALUES ('business_doc', '业务知识库', 1, '业务文档、产品方案、流程说明')
ON DUPLICATE KEY UPDATE kb_name = VALUES(kb_name), kb_type = VALUES(kb_type), description = VALUES(description);

INSERT INTO rag_knowledge_base(kb_code, kb_name, kb_type, description)
VALUES ('tech_doc', '技术知识库', 2, '技术方案、接口文档、代码说明')
ON DUPLICATE KEY UPDATE kb_name = VALUES(kb_name), kb_type = VALUES(kb_type), description = VALUES(description);
