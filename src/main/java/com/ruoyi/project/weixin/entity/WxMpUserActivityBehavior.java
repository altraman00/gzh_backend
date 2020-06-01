package com.ruoyi.project.weixin.entity;

import com.ruoyi.project.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author xiekun
 * @since 2020-05-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WxMpUserActivityBehavior extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String pageUrl;

    private String openId;

    /**
     * 场景：页面浏览:scan_page；分享:share；开始测评:strat_testing；提交测评:submit_testing；查看测评:check_testing；关注公众号:attention_vipcn；关注顾问:attention_adviser
     */
    private String scene;

    /**
     * 活动名称
     */
    private String activityName;

    private String appId;

    /**
     * 排序号
     */
    private Integer sortNo;


}
