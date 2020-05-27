/*
 Navicat Premium Data Transfer

 Source Server         : test_db
 Source Server Type    : MySQL
 Source Server Version : 50635
 Source Host           : 111.230.70.125:3389
 Source Schema         : db_feo_ruo

 Target Server Type    : MySQL
 Target Server Version : 50635
 File Encoding         : 65001

 Date: 25/05/2020 15:16:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for wx_mp_yunchan001_help_user_record
-- ----------------------------
DROP TABLE IF EXISTS `wx_mp_yunchan001_help_user_record`;
CREATE TABLE `wx_mp_yunchan001_help_user_record` (
  `id` varchar(32) CHARACTER SET utf8 NOT NULL DEFAULT '' COMMENT '主键',
  `invite_wx_user_id` varchar(32) DEFAULT NULL COMMENT '邀请人微信userId',
  `help_wx_user_id` varchar(32) DEFAULT NULL COMMENT '助力人微信userId',
  `yunchan001_help_user_status_id` varchar(32) DEFAULT NULL COMMENT '对应的wx_mp_yunchan001_help_user_status_id主键',
  `del_flag` char(2) CHARACTER SET utf8 DEFAULT '0' COMMENT '逻辑删除标记（0：显示；1：隐藏）',
  `create_id` varchar(32) CHARACTER SET utf8 DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_id` varchar(32) CHARACTER SET utf8 DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(100) CHARACTER SET utf8 DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='孕产001-助力记录表';

-- ----------------------------
-- Table structure for wx_mp_yunchan001_help_user_status
-- ----------------------------
DROP TABLE IF EXISTS `wx_mp_yunchan001_help_user_status`;
CREATE TABLE `wx_mp_yunchan001_help_user_status` (
  `id` varchar(32) CHARACTER SET utf8 NOT NULL DEFAULT '' COMMENT '主键',
  `app_id` varchar(32) DEFAULT NULL COMMENT 'appId',
  `template_id` varchar(32) DEFAULT NULL COMMENT '活动模板id',
  `wx_user_id` varchar(32) DEFAULT NULL COMMENT '微信用户id',
  `complete_num` int(2) DEFAULT NULL COMMENT '任务完成个数',
  `task_status` int(2) DEFAULT NULL COMMENT '状态 1-进行中 2-已完成',
  `del_flag` char(2) CHARACTER SET utf8 DEFAULT '0' COMMENT '逻辑删除标记（0：显示；1：隐藏）',
  `create_id` varchar(32) CHARACTER SET utf8 DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_id` varchar(32) CHARACTER SET utf8 DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(100) CHARACTER SET utf8 DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='孕产001-用户助力状态';

-- ----------------------------
-- Table structure for wx_mp_yunchan001_user_status
-- ----------------------------
DROP TABLE IF EXISTS `wx_mp_yunchan001_user_status`;
CREATE TABLE `wx_mp_yunchan001_user_status` (
  `id` varchar(32) CHARACTER SET utf8 NOT NULL DEFAULT '' COMMENT '主键',
  `app_id` varchar(32) DEFAULT NULL,
  `wxuser_id` varchar(32) DEFAULT NULL COMMENT 'wx_user的id',
  `open_id` varchar(64) DEFAULT NULL COMMENT 'openid',
  `first_stage_status` varchar(8) DEFAULT NULL COMMENT '第一阶段的解锁状态',
  `first_stage_unlock_time` datetime DEFAULT NULL COMMENT '第一阶段的解锁时间',
  `second_stage_status` varchar(8) DEFAULT NULL COMMENT '第二阶段的解锁状态',
  `second_stage_unlock_time` datetime DEFAULT NULL COMMENT '第二阶段的解锁时间',
  `aid_teacher_qrcode` varchar(500) DEFAULT NULL COMMENT '助教老师的微信二维码',
  `del_flag` char(2) CHARACTER SET utf8 DEFAULT '0' COMMENT '逻辑删除标记（0：未删除；1：删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `sort_no` int(2) DEFAULT NULL COMMENT '排序号',
  `create_id` varchar(32) CHARACTER SET utf8 DEFAULT NULL COMMENT '创建者',
  `update_id` varchar(32) CHARACTER SET utf8 DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='孕产001-用户活动状态';

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE `wx_mp_yunchan001_user_status`
ADD COLUMN `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注' AFTER `update_id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;


ALTER TABLE `wx_mp_yunchan001_user_status`
MODIFY COLUMN `first_stage_status` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 0 COMMENT '第一阶段的解锁状态，0:未解锁，1:已解锁' AFTER `open_id`,
MODIFY COLUMN `second_stage_status` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 0 COMMENT '第二阶段的解锁状态，0:未解锁，1:已解锁' AFTER `first_stage_unlock_time`;


ALTER TABLE `wx_mp_yunchan001_user_status`
MODIFY COLUMN `first_stage_status` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '第一阶段的解锁状态，0:未解锁，1:已解锁' AFTER `open_id`,
MODIFY COLUMN `second_stage_status` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '第二阶段的解锁状态，0:未解锁，1:已解锁' AFTER `first_stage_unlock_time`;


ALTER TABLE `wx_mp_yunchan001_user_status`
MODIFY COLUMN `first_stage_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '第一阶段的解锁状态，0:未解锁，1:已解锁' AFTER `open_id`,
MODIFY COLUMN `second_stage_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '第二阶段的解锁状态，0:未解锁，1:已解锁' AFTER `first_stage_unlock_time`



INSERT INTO `wx_activity_template`(`id`, `create_id`, `create_time`, `update_id`, `update_time`, `remark`, `del_flag`, `template_name`, `support_scene`, `template_class`, `need_num`, `reward_url`, `alias`) VALUES ('4', NULL, '2020-05-13 19:50:09', NULL, '2020-05-26 09:50:29', NULL, '0', '孕产训练营拉新活动', '2', 'yunchan001ActivityServiceImpl', NULL, NULL, 'yunchan001')
INSERT INTO `wx_activity_template_message`(`id`, `create_id`, `create_time`, `update_id`, `update_time`, `remark`, `del_flag`, `template_id`, `rep_type`, `rep_content`, `rep_media_id`, `rep_url`, `title`, `scene`, `avatar_coordinate`, `qrcode_coordinate`, `avatar_size`, `qrcode_size`, `schedule_cron`, `schedule_class`, `schedule_method`, `sort_no`) VALUES ('41', NULL, '2020-03-12 09:09:52', NULL, '2020-03-24 10:46:29', '${上级好友微信昵称}——为分享者微信昵称； 此条信息发放给被推荐者', '0', '4', 'text', '【任务完成】\r\n\r\n您已帮好友${上级好友微信昵称}，助力成功。', NULL, '', '关注消息1——给关注者推送任务完成消息', 'help_success', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1);
INSERT INTO `wx_activity_template_message`(`id`, `create_id`, `create_time`, `update_id`, `update_time`, `remark`, `del_flag`, `template_id`, `rep_type`, `rep_content`, `rep_media_id`, `rep_url`, `title`, `scene`, `avatar_coordinate`, `qrcode_coordinate`, `avatar_size`, `qrcode_size`, `schedule_cron`, `schedule_class`, `schedule_method`, `sort_no`) VALUES ('40', NULL, '2020-03-12 09:09:52', NULL, '2020-04-17 16:19:29', '奖品领取页面地址', '0', '4', 'text', 'http://qr61.cn/oKHkte/q08pvhH', NULL, '', '奖品领取页面地址', 'jp_url', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 10);
INSERT INTO `wx_activity_template_message`(`id`, `create_id`, `create_time`, `update_id`, `update_time`, `remark`, `del_flag`, `template_id`, `rep_type`, `rep_content`, `rep_media_id`, `rep_url`, `title`, `scene`, `avatar_coordinate`, `qrcode_coordinate`, `avatar_size`, `qrcode_size`, `schedule_cron`, `schedule_class`, `schedule_method`, `sort_no`) VALUES ('42', NULL, '2020-03-12 09:09:52', NULL, '2020-03-24 10:46:35', '此条信息发放给被推荐者', '0', '4', 'text', '【任务未完成】 您已助力过，分享给其他好友帮忙助力吧。', NULL, '', '关注消息1——给关注者推送任务完成消息', 'has_help', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `wx_activity_template_message`(`id`, `create_id`, `create_time`, `update_id`, `update_time`, `remark`, `del_flag`, `template_id`, `rep_type`, `rep_content`, `rep_media_id`, `rep_url`, `title`, `scene`, `avatar_coordinate`, `qrcode_coordinate`, `avatar_size`, `qrcode_size`, `schedule_cron`, `schedule_class`, `schedule_method`, `sort_no`) VALUES ('43', NULL, '2020-03-12 09:09:52', NULL, '2020-04-24 13:33:13', '给被推荐者发送活动规则', '0', '4', 'text', 'Hi ${关注者微信昵称}, 你好!\n\n我是小M，这里有份MBA必备资料包！领取规则:\n1、只要三位好友助力，就能领取。\n2、将下面带有你头像的图片转发给好友或好友群。\n3、邀请好友关注公众号，即可助力成功。\n<a href = \"https://open.weixin.qq.com/connect/oauth2/authorize?appid=&redirect_uri=http://gzh.supplus.cn&response_type=code&scope=snsapi_userinfo&state=#wechat_redirect\">点击查看活动详情</a>', NULL, '', '关注消息2——给关注人推送活动规则消息', 'activity_rule', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 4);
INSERT INTO `wx_activity_template_message`(`id`, `create_id`, `create_time`, `update_id`, `update_time`, `remark`, `del_flag`, `template_id`, `rep_type`, `rep_content`, `rep_media_id`, `rep_url`, `title`, `scene`, `avatar_coordinate`, `qrcode_coordinate`, `avatar_size`, `qrcode_size`, `schedule_cron`, `schedule_class`, `schedule_method`, `sort_no`) VALUES ('44', NULL, '2020-03-12 09:09:52', NULL, '2020-04-23 14:41:49', '活动海报', '0', '4', 'poster', 'http://mmbiz.qpic.cn/mmbiz_jpg/OaMbhMpeNpnaccOjAWLzzCK6aCrC5bZsUA1CP84apevibkkiaVGfPm9B5ocWQEuHYHMF0icBDVbZwtpVAtiacJsIrg/0?wx_fmt=jpeg', 'Q1ipCmTIBavTCEiOqjOaYGdQZJv_LMO4k8g0Qh0MYu4', '', '关注消息3——给关注人推送海报消息', 'activity_poster', '36,30', '530,986', 54, 160, NULL, NULL, NULL, 5);
INSERT INTO `wx_activity_template_message`(`id`, `create_id`, `create_time`, `update_id`, `update_time`, `remark`, `del_flag`, `template_id`, `rep_type`, `rep_content`, `rep_media_id`, `rep_url`, `title`, `scene`, `avatar_coordinate`, `qrcode_coordinate`, `avatar_size`, `qrcode_size`, `schedule_cron`, `schedule_class`, `schedule_method`, `sort_no`) VALUES ('45', NULL, '2020-03-12 09:09:52', NULL, '2020-04-24 13:33:13', '当被推荐者关注公众号后了，且推荐者的3人助力未完成时 ${被推荐人昵称}为被分享者', '0', '4', 'text', '【活动任务】\n您的好友${被推荐人昵称}帮你助力了，还差${缺少个数}个助力，<a href = \"https://open.weixin.qq.com/connect/oauth2/authorize?appid=&redirect_uri=http://gzh.supplus.cn&response_type=code&scope=snsapi_userinfo&state=#wechat_redirect\">查看任务详情</a>！', NULL, '', '关注消息4——给推荐人推送任务过程中的消息', 'be_helped', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 6);
INSERT INTO `wx_activity_template_message`(`id`, `create_id`, `create_time`, `update_id`, `update_time`, `remark`, `del_flag`, `template_id`, `rep_type`, `rep_content`, `rep_media_id`, `rep_url`, `title`, `scene`, `avatar_coordinate`, `qrcode_coordinate`, `avatar_size`, `qrcode_size`, `schedule_cron`, `schedule_class`, `schedule_method`, `sort_no`) VALUES ('46', NULL, '2020-03-12 09:09:52', NULL, '2020-04-24 13:33:13', '当被推荐者关注公众号后了，且推荐者的3人助力完成时', '0', '4', 'text', '【活动任务】\n恭喜，您的好友已经帮您完成了助力任务，点击领取资料包吧！<a href = \"https://open.weixin.qq.com/connect/oauth2/authorize?appid=&redirect_uri=http://gzh.supplus.cn&response_type=code&scope=snsapi_userinfo&state=#wechat_redirect\">领取奖励</a>', NULL, '', '关注消息5——给推荐人推送任务已完成的消息', 'task_complete', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 7);
INSERT INTO `wx_activity_template_message`(`id`, `create_id`, `create_time`, `update_id`, `update_time`, `remark`, `del_flag`, `template_id`, `rep_type`, `rep_content`, `rep_media_id`, `rep_url`, `title`, `scene`, `avatar_coordinate`, `qrcode_coordinate`, `avatar_size`, `qrcode_size`, `schedule_cron`, `schedule_class`, `schedule_method`, `sort_no`) VALUES ('47', NULL, '2020-03-12 09:09:52', NULL, '2020-04-24 13:33:13', '当关注者的助力活动未完成时，定时提醒', '0', '4', 'schedule', '只差一点点，您的助力活动就可以完成啦。赶紧去邀请好友助力吧。<a href = \"https://open.weixin.qq.com/connect/oauth2/authorize?appid=&redirect_uri=http://gzh.supplus.cn&response_type=code&scope=snsapi_userinfo&state=#wechat_redirect\">查看任务详情</a>', NULL, '', '定时消息1——给关注人推送消息	', 'schedule_invite', NULL, NULL, NULL, NULL, '0 0 21 * * ? ', 'helpActivityTask', 'sendInviteMessage', 8);
INSERT INTO `wx_activity_template_message`(`id`, `create_id`, `create_time`, `update_id`, `update_time`, `remark`, `del_flag`, `template_id`, `rep_type`, `rep_content`, `rep_media_id`, `rep_url`, `title`, `scene`, `avatar_coordinate`, `qrcode_coordinate`, `avatar_size`, `qrcode_size`, `schedule_cron`, `schedule_class`, `schedule_method`, `sort_no`) VALUES ('48', NULL, '2020-03-12 09:09:52', NULL, '2020-03-24 10:57:12', '此条信息发放给被推荐者', '0', '4', 'text', '助力人数已满，无法助力。', NULL, '', '关注消息1——给关注者推送任务完成消息', 'has_complete', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3);
INSERT INTO `wx_activity_template_message`(`id`, `create_id`, `create_time`, `update_id`, `update_time`, `remark`, `del_flag`, `template_id`, `rep_type`, `rep_content`, `rep_media_id`, `rep_url`, `title`, `scene`, `avatar_coordinate`, `qrcode_coordinate`, `avatar_size`, `qrcode_size`, `schedule_cron`, `schedule_class`, `schedule_method`, `sort_no`) VALUES ('49', NULL, '2020-03-12 09:09:52', NULL, '2020-04-17 14:57:56', '奖励资料标题', '0', '4', 'text', '2020考研必过宝典', NULL, '', '奖励资料标题', 'jp_title', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 9);