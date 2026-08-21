-- 用户中心 user 表结构（MySQL 8）。
-- 新环境可直接执行；已有环境请先执行 migration-user-schema.sql。
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户编号',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `avatarUrl` VARCHAR(255) NULL COMMENT '头像地址',
    `gender` TINYINT NULL COMMENT '性别',
    `password` VARCHAR(128) NOT NULL COMMENT 'PBKDF2 密码摘要',
    `phone` VARCHAR(20) NULL COMMENT '手机号',
    `email` VARCHAR(128) NOT NULL COMMENT '邮箱',
    `isValid` TINYINT NOT NULL DEFAULT 1 COMMENT '是否有效：0-否，1-是',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    UNIQUE KEY `uk_user_email` (`email`),
    INDEX `idx_user_isDelete_createTime` (`isDelete`, `createTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
