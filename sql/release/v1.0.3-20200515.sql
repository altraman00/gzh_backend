ALTER TABLE `db_feo_ruo`.`wx_user`
ADD COLUMN `parent_openid` varchar(64) NULL COMMENT '上一级用户openid' AFTER `mall_user_id`,
ADD COLUMN `user_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '用户来源 diabetes_h5:糖知家' AFTER `parent_openid`;
