package com.ruoyi.project.weixin.entity;

import com.ruoyi.project.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.entity
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月19日 18:12
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WxMpActivityTemplete extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String appId;

    private String appName;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板对应的服务类名,对应具体的某个活动
     */
    private String templateClass;

    /**
     * 活动是否启用
     */
    private boolean activityEnable;

    private Integer needNum;

    private String rewardUrl;


    /**
     * 排序号
     */
    private Integer sortNo;

}
