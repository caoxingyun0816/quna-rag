CREATE DATABASE IF NOT EXISTS rag_enterprise DEFAULT CHARSET utf8mb4;
USE rag_enterprise;

CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `username` varchar(50) NOT NULL,
                        `password` varchar(100) NOT NULL,
                        `role` varchar(20) DEFAULT 'user',
                        `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `username` (`username`)
);

CREATE TABLE `document` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `filename` varchar(255) NOT NULL,
                            `user_id` bigint NOT NULL,
                            `source` varchar(30) DEFAULT '本地上传',
                            `status` varchar(30) DEFAULT '已入库',
                            `file_type` varchar(50) DEFAULT NULL,
                            `file_size` bigint DEFAULT NULL,
                            `chunk_count` int DEFAULT 0,
                            `permission_scope` varchar(30) DEFAULT '私有',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`)
);

CREATE TABLE `doc_chunk_config` (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `user_id` bigint NOT NULL,
                                    `chunk_size` int NOT NULL DEFAULT 500,
                                    `chunk_overlap` int NOT NULL DEFAULT 0,
                                    `separators` varchar(255) DEFAULT '段落,换行,句号',
                                    `enable_title_enhance` tinyint DEFAULT 1,
                                    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`)
);

CREATE TABLE `vector_model_config` (
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

CREATE TABLE `document_permission` (
                                       `id` bigint NOT NULL AUTO_INCREMENT,
                                       `document_id` bigint NOT NULL,
                                       `subject_type` varchar(30) NOT NULL,
                                       `subject_value` varchar(100) NOT NULL,
                                       `permission` varchar(30) DEFAULT 'READ',
                                       `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                       PRIMARY KEY (`id`),
                                       KEY `idx_document_permission_doc` (`document_id`)
);

CREATE TABLE `rag_document` (
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
                                `file_url` varchar(1000) DEFAULT NULL,
                                `content_hash` varchar(64) DEFAULT NULL,
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_rag_doc_collection_hash` (`collection_code`, `content_hash`),
                                KEY `idx_rag_doc_collection_project` (`collection_code`, `project`),
                                KEY `idx_rag_doc_module_type` (`module`, `doc_type`)
);

CREATE TABLE `rag_chunk` (
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

CREATE TABLE `rag_split_config` (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `collection_code` varchar(30) NOT NULL,
                                    `chunk_size` int NOT NULL DEFAULT 800,
                                    `chunk_overlap` int NOT NULL DEFAULT 100,
                                    `min_length` int NOT NULL DEFAULT 80,
                                    `markdown_soft_multiplier` int NOT NULL DEFAULT 2,
                                    `markdown_hard_multiplier` int NOT NULL DEFAULT 8,
                                    `markdown_atomic_heading_level` int NOT NULL DEFAULT 4,
                                    `markdown_max_merge_sections` int NOT NULL DEFAULT 3,
                                    `separators` varchar(500) DEFAULT NULL,
                                    `protect_markdown_table` tinyint(1) NOT NULL DEFAULT 1,
                                    `protect_code_fence` tinyint(1) NOT NULL DEFAULT 1,
                                    `protect_api_section` tinyint(1) NOT NULL DEFAULT 1,
                                    `protect_json_block` tinyint(1) NOT NULL DEFAULT 1,
                                    `protect_sql_block` tinyint(1) NOT NULL DEFAULT 1,
                                    `enabled` tinyint(1) NOT NULL DEFAULT 1,
                                    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`),
                                    KEY `idx_rag_split_collection` (`collection_code`, `enabled`)
);

CREATE TABLE `chat_history` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `user_id` bigint NOT NULL,
                                `question` text,
                                `answer` text,
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`)
);

INSERT INTO `user` (username,password,role)
VALUES ('admin','$2a$10$Zc1XJdH4A8mJzH2EbGaHbeJxGagqVJ1v1zJ9l1A2e3S4D5F6G7H8I9','admin');
