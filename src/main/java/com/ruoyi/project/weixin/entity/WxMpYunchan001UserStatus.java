package com.ruoyi.project.weixin.entity;

import com.ruoyi.project.common.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author xiekun
 * @since 2020-05-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WxMpYunchan001UserStatus extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String appId;

    /**
     * wx_user的id
     */
    private String wxuserId;

    /**
     * openid
     */
    private String openId;

    /**
     * 第一阶段的解锁状态
     */
    private String firstStageStatus;

    /**
     * 第一阶段的解锁时间
     */
    private LocalDateTime firstStageUnlockTime;

    /**
     * 第二阶段的解锁状态
     */
    private String secondStageStatus;

    /**
     * 第二阶段的解锁时间
     */
    private LocalDateTime secondStageUnlockTime;

    private String aidTeacherQrcode;

    /**
     * 排序号
     */
    private Integer sortNo;


}
