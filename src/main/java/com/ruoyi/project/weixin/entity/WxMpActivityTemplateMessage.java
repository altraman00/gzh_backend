package com.ruoyi.project.weixin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.project.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName
public class WxMpActivityTemplateMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 回复消息类型（text：文本；image：图片；poster：海报）
     */
    private String repType;

    /**
     * 消息内容
     */
    private String repContent;

    /**
     * 回复类型imge、voice、news、video的mediaID或音乐缩略图的媒体id
     */
    private String repMediaId;

    /**
     * 链接
     */
    private String repUrl;

    /**
     * 标题
     */
    private String title;

    /**
     * 场景字段
     */
    private String scene;

    /**
     * 公众号id
     */
    private String appId;

    /**
     * 头像坐标
     */
    private String avatarCoordinate;

    /**
     * 二维码坐标
     */
    private String qrcodeCoordinate;

    /**
     * 头像大小
     */
    private Integer avatarSize;

    /**
     * 二维码大小
     */
    private Integer qrcodeSize;

    /**
     * 类型为定时消息时的cron表达式
     */
    private String scheduleCron;

    /**
     * 类型为定时消息时的类
     */
    private String scheduleClass;

    /**
     * 类型为定时消息时的cron方法名
     */
    private String scheduleMethod;

    /**
     * 排序
     */
    private Integer sortNo;

    /**
     * 活动是否启用
     */
    private Boolean activityEnable;


}
