package com.ruoyi.project.weixin.entity;

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
public class WxMp extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 公众号appId
     */
    private String appId;

    /**
     * 活动模板id
     */
    private String templateId;

    /**
     * 公众号名称
     */
    private String appName;

    /**
     * 公众号标识
     */
    private String appIdentify;


}
