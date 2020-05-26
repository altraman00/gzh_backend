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

ALTER TABLE `db_feo_ruo`.`wx_mp_yunchan001_user_status`
ADD COLUMN `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注' AFTER `update_id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;
