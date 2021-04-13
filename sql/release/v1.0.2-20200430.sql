-- 备份表wx_activity_template_message wx_mp_template_message

ALTER TABLE `wx_user`
ADD COLUMN `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户所在公号APPID' AFTER `open_id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;

ALTER TABLE `wx_activity_template`
ADD COLUMN `support_scene` varchar(255) NULL COMMENT '支持哪些场景 1. 订阅号 2. 服务号 3.小程序 ;多场景支持时 用逗号\",\"分隔  eg: 1,2,3' AFTER `template_name`;

ALTER TABLE `wx_mp`
ADD COLUMN `type` int(2) NULL COMMENT '账号主体类型(1. 订阅号 2. 服务号 3.小程序)' AFTER `del_flag`;

ALTER TABLE `wx_menu`
ADD COLUMN `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '菜单所在公号APPID' AFTER `id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;

ALTER TABLE `wx_msg`
ADD COLUMN `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息所属公号APPID' AFTER `id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;

ALTER TABLE `wx_auto_reply`
ADD COLUMN `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '自动回复所在公号APPID' AFTER `id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;

ALTER TABLE `sys_role`
ADD COLUMN `mp_scope` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '[]' COMMENT '可见的公众号ID组成的数组字符串; eg:[1,2,3,5,8]' AFTER `data_scope`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`role_id`) USING BTREE;

ALTER TABLE `wx_task_help_record`
ADD COLUMN `wx_user_task_id` varchar(32) DEFAULT NULL COMMENT '对应的wx_activity_task主键' AFTER `help_wx_user_id`;

-- 初始化wx_user_task_id
UPDATE wx_task_help_record a, wx_activity_task b SET a.wx_user_task_id = b.id WHERE a.invite_wx_user_id = b.wx_user_id AND b.app_id = 'wx66fcb1f854cdab95'AND b.template_id = 1;

ALTER TABLE `wx_mp`
ADD COLUMN `secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '公众号secret' AFTER `app_id`,
ADD COLUMN `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '公众号token' AFTER `secret`,
ADD COLUMN `aes_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '公众号aesKey' AFTER `token`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;
-- wx_mp新增字段需要根据实际情况初始化
UPDATE wx_mp SET secret = '9239cce70b0e7028a2d29ae3e8247e74', token = 'smallprogram', aes_key = 'FR14R0SGgKUygrZ9pIrsarQoWuup8ujgzzrTt1Cqqwf' WHERE app_id = 'wx66fcb1f854cdab95';
-- 设置角色可见公众号为空数组(超级管理员默认全部可见,不用特殊设置, 其他角色需要去后台手动配置公众号可见权限)
UPDATE sys_role SET mp_scope = '[]';
-- 以上所有需要补充APPID字段的数据 默认都设置为现在生产正在使用的公众号(柠檬在线学堂 APPID:wx66fcb1f854cdab95)
UPDATE wx_user SET app_id = 'wx66fcb1f854cdab95';
UPDATE wx_menu SET app_id = 'wx66fcb1f854cdab95';
UPDATE wx_msg SET app_id = 'wx66fcb1f854cdab95';
UPDATE wx_auto_reply SET app_id = 'wx66fcb1f854cdab95';
UPDATE wx_activity_template SET support_scene = '2';
UPDATE wx_mp SET type = '2';
-- wx_activity_template_message中rep_content包含重定向到gzh-h5页面的URL 都要加上'state=', 然后现有公众号的模板消息中也要加上'state='
UPDATE wx_activity_template_message SET rep_content = REPLACE(rep_content, '#wechat_redirect' ,'&state=#wechat_redirect');
UPDATE wx_mp_template_message SET rep_content = REPLACE(rep_content, '#wechat_redirect' ,CONCAT('&state=',app_id,'#wechat_redirect'));
-- 另外注意: 每个公众号的海报不能共用, 切换公众号之后需要在公众号配置中重新上传海报, 验证预览海报正常即可
