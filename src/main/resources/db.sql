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
