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
 * @author zhangbin
 * @since 2020-03-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WxMpTemplateMessage extends BaseEntity {

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
     * 回复类型文本保存文字
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
    private String mpAppId;


}
