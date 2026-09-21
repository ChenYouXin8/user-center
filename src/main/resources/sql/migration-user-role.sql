-- 为 chenyouxin.user 表增加角色列，供前端 access.canAdmin 权限判断使用。
-- 该变更是纯新增列且带默认值，对已有数据无破坏性：存量用户全部落为普通用户（0）。

ALTER TABLE `user`
    ADD COLUMN `userRole` TINYINT NOT NULL DEFAULT 0 COMMENT '用户角色：0-普通用户，1-管理员';

-- 按需将指定账号提升为管理员，例如：
-- UPDATE `user` SET `userRole` = 1 WHERE `username` = 'admin';
