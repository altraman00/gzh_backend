
--
-- ALTER TABLE `wx_mp`
-- DROP COLUMN `template_id`;


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


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `wx_mp_activity_template`;
CREATE TABLE `wx_mp_activity_template` (
  `id` varchar(32) CHARACTER SET utf8 NOT NULL DEFAULT '' COMMENT '主键',
  `app_id` varchar(32) DEFAULT NULL,
  `app_name` varchar(255) DEFAULT NULL,
  `template_id` varchar(32) DEFAULT NULL COMMENT '模板id',
  `template_name` varchar(64) DEFAULT NULL COMMENT '模板名称',
  `template_class` varchar(64) DEFAULT NULL COMMENT '模板对应的服务类名',
  `activity_enable` tinyint(4) DEFAULT NULL COMMENT '活动是否启用',
  `support_scene` varchar(255) DEFAULT NULL COMMENT '支持哪些场景 1. 订阅号 2. 服务号 3.小程序 ;多场景支持时 用逗号","分隔  eg: 1,2,3',
  `need_num` int(11) DEFAULT NULL COMMENT '任务完成需要的个数',
  `reward_url` varchar(255) DEFAULT NULL COMMENT '活动奖励地址',
  `remark` varchar(1000) CHARACTER SET utf8 DEFAULT NULL COMMENT '备注',
  `start_time` datetime DEFAULT NULL COMMENT '活动启动时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(2) CHARACTER SET utf8 DEFAULT '0' COMMENT '逻辑删除标记（0：显示；1：隐藏）',
  `sort_no` int(2) DEFAULT NULL COMMENT '排序号',
  `create_id` varchar(32) CHARACTER SET utf8 DEFAULT NULL COMMENT '创建者',
  `update_id` varchar(32) CHARACTER SET utf8 DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;


ALTER TABLE `wx_mp_activity_template`
MODIFY COLUMN `activity_enable` tinyint(4) NOT NULL DEFAULT 0 COMMENT '活动是否启用' AFTER `template_class`;




rename table wx_mp_template_message to wx_mp_activity_template_message


-- by tanchang 20200525
ALTER TABLE `wx_activity_template`
ADD COLUMN `alias` varchar(100) NULL COMMENT '活动模板别名' AFTER `reward_url`;

ALTER TABLE `wx_activity_template`
ADD UNIQUE INDEX(`alias`);

ALTER TABLE `wx_mp_activity_template`
ADD COLUMN `master_template` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是默认的主要活动模板' AFTER `update_id`