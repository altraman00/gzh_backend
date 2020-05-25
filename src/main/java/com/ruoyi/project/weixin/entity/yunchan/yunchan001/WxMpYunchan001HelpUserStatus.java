package com.ruoyi.project.weixin.entity.yunchan.yunchan001;

import com.ruoyi.project.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 活动任务表
 * </p>
 *
 * @author xiekun
 * @since 2020-05-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WxMpYunchan001HelpUserStatus extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * appId
     */
    private String appId;

    /**
     * 活动模板id
     */
    private String templateId;

    /**
     * 微信用户id
     */
    private String wxUserId;

    /**
     * 任务完成个数
     */
    private Integer completeNum;

    /**
     * 状态 1-进行中 2-已完成
     */
    private Integer taskStatus;


}
