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
}
