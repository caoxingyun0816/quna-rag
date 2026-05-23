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
