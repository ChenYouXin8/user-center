-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `userId` BIGINT NULL,
    `username` VARCHAR(64) NULL,
    `operation` VARCHAR(64) NOT NULL COMMENT '操作类型: LOGIN, LOGOUT, UPDATE_USER, DELETE_USER, CHANGE_PASSWORD',
    `detail` VARCHAR(255) NULL,
    `ip` VARCHAR(64) NULL,
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_userId` (`userId`),
    INDEX `idx_createTime` (`createTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
