package com.ruoyi.project.weixin.entity;

import com.ruoyi.project.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 助力记录表
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WxTaskHelpRecord extends BaseEntity {

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
     * 对应的wx_activity_task主键
     */
    private String wxUserTaskId;

    /**
     * wxUserTaskId对应的对象中已经包含了APPID
     */
//    private String appId;


}
