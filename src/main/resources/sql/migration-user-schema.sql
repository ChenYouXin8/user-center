-- 将已有 chenyouxin.user 表对齐到当前后端实体。
-- 执行唯一索引前，必须先确认下面两个查询均返回 0 行：
-- SELECT username, COUNT(*) FROM `user` GROUP BY username HAVING COUNT(*) > 1;
-- SELECT email, COUNT(*) FROM `user` GROUP BY email HAVING COUNT(*) > 1;

ALTER TABLE `user`
    MODIFY COLUMN `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    MODIFY COLUMN `avatarUrl` VARCHAR(255) NULL COMMENT '头像地址',
    MODIFY COLUMN `gender` TINYINT NULL COMMENT '性别',
    MODIFY COLUMN `password` VARCHAR(128) NOT NULL COMMENT 'PBKDF2 密码摘要',
    MODIFY COLUMN `phone` VARCHAR(20) NULL COMMENT '手机号',
    MODIFY COLUMN `email` VARCHAR(128) NOT NULL COMMENT '邮箱',
    MODIFY COLUMN `isValid` TINYINT NOT NULL DEFAULT 1 COMMENT '是否有效：0-否，1-是',
    MODIFY COLUMN `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    MODIFY COLUMN `isDelete` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    ADD UNIQUE KEY `uk_user_username` (`username`),
    ADD UNIQUE KEY `uk_user_email` (`email`),
    ADD INDEX `idx_user_isDelete_createTime` (`isDelete`, `createTime`);
