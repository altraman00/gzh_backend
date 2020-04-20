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

    private String appId;


}
