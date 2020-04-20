package com.ruoyi.project.weixin.entity;

import com.ruoyi.project.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 活动模板表
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WxActivityTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    public static final String SUPPORT_SCENE_SUBSCRIPTION_NUMBER = "1";
    public static final String SUPPORT_SCENE_SERVICE_NUMBER = "2";
    public static final String SUPPORT_SCENE_APPLETS = "3";

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板对应的服务类名
     */
    private String templateClass;

    /**
     * 活动奖励地址
     */
    private String rewardUrl;

    /**
     * 任务完成需要的个数
     */
    private Integer needNum;
    /**
     * 支持哪些场景 1. 订阅号 2. 服务号 3.小程序
     * 多场景支持时 用逗号","分隔  eg: 1,2,3
     */
    private String supportScene;
}
