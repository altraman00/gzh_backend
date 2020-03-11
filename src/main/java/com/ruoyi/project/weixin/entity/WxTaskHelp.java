package com.ruoyi.project.weixin.entity;

import com.ruoyi.project.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 助力任务表
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WxTaskHelp extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 微信用户id
     */
    private String wxUserId;

    /**
     * 被助力人数（任务完成个数）
     */
    private Integer helpNum;

    /**
     * 状态 1-进行中 2-已完成
     */
    private Integer taskStatus;


}
