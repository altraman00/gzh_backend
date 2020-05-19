package com.ruoyi.project.weixin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.dto
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月15日 18:07
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Data
public class WxMsgDTO {

    @ApiModelProperty( "openId")
    private String openId;

    @ApiModelProperty( "appId")
    private String appId;

    @ApiModelProperty("发送的消息内容")
    private String content;

    @ApiModelProperty("消息类型（text：文本；image：图片；voice：语音；video：视频；shortvideo：小视频；location：地理位置；music：音乐；news：图文；event：推送事件）")
    private String repType;

    @ApiModelProperty( "消息分类（1、用户发给公众号；2、公众号发给用户；")
    private String type;

    @ApiModelProperty( "消息id")
    private String wxMpTempleteMsgId;

}
