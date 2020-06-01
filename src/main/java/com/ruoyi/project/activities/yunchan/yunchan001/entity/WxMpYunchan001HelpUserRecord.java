package com.ruoyi.project.activities.yunchan.yunchan001.entity;

import com.ruoyi.project.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 助力记录表
 * </p>
 *
 * @author xiekun
 * @since 2020-05-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WxMpYunchan001HelpUserRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 邀请人微信userId
     */
    private String inviteWxUserId;

    /**
     * 助力人微信userId
     */
    private String helpWxUserId;

    /**
     * 对应的wx_mp_yunchan001_help_user_status_id主键
     */
    private String yunchan001HelpUserStatusId;


}
