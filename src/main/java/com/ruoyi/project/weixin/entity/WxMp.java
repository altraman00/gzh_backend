package com.ruoyi.project.weixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ruoyi.project.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 公众号-模板关联表
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
//返回前端时忽略敏感属性
@JsonIgnoreProperties({"secret","token","aesKey"})
public class WxMp extends BaseEntity {

    public static final Integer TYPE_SUBSCRIPTION_NUMBER = 1;
    public static final Integer TYPE_SERVICE_NUMBER = 2;
    public static final Integer TYPE_APPLETS = 3;

    private static final long serialVersionUID = 1L;

    /**
     * 公众号appId
     */
    private String appId;
    private String secret;
    private String token;
    private String aesKey;

    /**
     * 账号主体类型(1. 订阅号 2. 服务号 3.小程序)
     */
    private Integer type;

    /**
     * 活动模板id
     */
    private String templateId_xxx;

    /**
     * 公众号名称
     */
    private String appName;

    /**
     * 公众号标识
     */
    private String appIdentify;

    /**
     * 活动是否启用
     */
    private boolean activityEnable;

}
