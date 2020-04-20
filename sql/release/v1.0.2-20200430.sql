ALTER TABLE `wx_user`
ADD COLUMN `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户所在公号APPID' AFTER `open_id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;

ALTER TABLE `wx_activity_template`
ADD COLUMN `support_scene` varchar(255) NULL COMMENT '支持哪些场景 1. 订阅号 2. 服务号 3.小程序 ;多场景支持时 用逗号\",\"分隔  eg: 1,2,3' AFTER `template_name`;

ALTER TABLE `wx_menu`
ADD COLUMN `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '菜单所在公号APPID' AFTER `id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;

ALTER TABLE `wx_msg`
ADD COLUMN `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息所属公号APPID' AFTER `id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;

ALTER TABLE `wx_task_help_record`
ADD COLUMN `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '助力记录所属公号APPID' AFTER `id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;

ALTER TABLE `wx_auto_reply`
ADD COLUMN `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '自动回复所在公号APPID' AFTER `id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;

ALTER TABLE `sys_role`
ADD COLUMN `mp_scope` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '可见的公众号ID组成的数组字符串; eg:[1,2,3,5,8]' AFTER `data_scope`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`role_id`) USING BTREE;

-- 为超级管理员admin初始化所有的公众号可见(每次新增公众号之后 该值都要更新)
