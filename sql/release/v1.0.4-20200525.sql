

ALTER TABLE `wx_activity_template_message`
DROP COLUMN `template_id`;


ALTER TABLE `wx_mp_template_message`
ADD COLUMN `activity_enable` tinyint(4) NULL COMMENT '1:启动;0:停止' AFTER `schedule_method`,
MODIFY COLUMN `remark` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注' AFTER `schedule_method`,
MODIFY COLUMN `sort_no` int(2) NULL DEFAULT NULL COMMENT '排序号' AFTER `remark`,
MODIFY COLUMN `create_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建者' AFTER `sort_no`,
MODIFY COLUMN `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间' AFTER `create_id`,
MODIFY COLUMN `update_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '更新者' AFTER `create_time`,
MODIFY COLUMN `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间' AFTER `update_id`;

ALTER TABLE `wx_mp_template_message`
MODIFY COLUMN `activity_enable` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1:启动;0:停止' AFTER `update_time`;


