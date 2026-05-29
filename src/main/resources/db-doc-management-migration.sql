USE rag_enterprise;

ALTER TABLE `document`
    ADD COLUMN `source` varchar(30) DEFAULT '本地上传',
    ADD COLUMN `status` varchar(30) DEFAULT '已入库',
    ADD COLUMN `file_type` varchar(50) DEFAULT NULL,
    ADD COLUMN `file_size` bigint DEFAULT NULL,
    ADD COLUMN `chunk_count` int DEFAULT 0,
    ADD COLUMN `permission_scope` varchar(30) DEFAULT '私有',
    ADD COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS `doc_chunk_config` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `chunk_size` int NOT NULL DEFAULT 500,
    `chunk_overlap` int NOT NULL DEFAULT 0,
    `separators` varchar(255) DEFAULT '段落,换行,句号',
    `enable_title_enhance` tinyint DEFAULT 1,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `vector_model_config` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `provider` varchar(50) DEFAULT 'DashScope',
    `model` varchar(100) DEFAULT 'text-embedding-v2',
    `dimension` int DEFAULT 1536,
    `endpoint` varchar(255) DEFAULT NULL,
    `enabled` tinyint DEFAULT 1,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `document_permission` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `document_id` bigint NOT NULL,
    `subject_type` varchar(30) NOT NULL,
    `subject_value` varchar(100) NOT NULL,
    `permission` varchar(30) DEFAULT 'READ',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_document_permission_doc` (`document_id`)
);

CREATE TABLE IF NOT EXISTS `rag_document` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `collection_code` varchar(30) NOT NULL,
    `filename` varchar(255) NOT NULL,
    `file_type` varchar(30) DEFAULT NULL,
    `source` varchar(30) DEFAULT 'UPLOAD',
    `project` varchar(100) DEFAULT NULL,
    `module` varchar(100) DEFAULT NULL,
    `doc_type` varchar(50) DEFAULT NULL,
    `tags` varchar(500) DEFAULT NULL,
    `permission_scope` varchar(100) DEFAULT 'ALL',
    `visible_roles` varchar(500) DEFAULT NULL,
    `status` varchar(30) DEFAULT 'INDEXED',
    `chunk_count` int DEFAULT 0,
    `file_size` bigint DEFAULT NULL,
    `content_hash` varchar(64) DEFAULT NULL,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rag_doc_collection_hash` (`collection_code`, `content_hash`),
    KEY `idx_rag_doc_collection_project` (`collection_code`, `project`),
    KEY `idx_rag_doc_module_type` (`module`, `doc_type`)
);

CREATE TABLE IF NOT EXISTS `rag_chunk` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `doc_id` bigint NOT NULL,
    `collection_code` varchar(30) NOT NULL,
    `chunk_index` int NOT NULL,
    `title_path` varchar(500) DEFAULT NULL,
    `content` mediumtext NOT NULL,
    `content_hash` varchar(64) DEFAULT NULL,
    `keywords` varchar(1000) DEFAULT NULL,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_rag_chunk_doc` (`doc_id`),
    KEY `idx_rag_chunk_collection` (`collection_code`),
    FULLTEXT KEY `ft_rag_chunk_content_keywords` (`content`, `keywords`)
);

-- 已存在 rag_document 表的环境需要单独执行：
-- ALTER TABLE `rag_document` ADD UNIQUE KEY `uk_rag_doc_collection_hash` (`collection_code`, `content_hash`);
-- 已存在 rag_chunk 表的环境需要单独执行：
-- ALTER TABLE `rag_chunk` DROP COLUMN `metadata_json`;
-- ALTER TABLE `rag_chunk` DROP INDEX `ft_rag_chunk_content`;
-- ALTER TABLE `rag_chunk` ADD FULLTEXT KEY `ft_rag_chunk_content_keywords` (`content`, `keywords`);
