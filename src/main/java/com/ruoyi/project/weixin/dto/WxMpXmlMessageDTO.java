package com.ruoyi.project.weixin.dto;

import lombok.Data;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;

/**
 * @Classname WxMpXmlMessageDTO
 * @Description
 * @Date 2020-04-17 11:32
 * @Created by pjz
 */
@Data
public class WxMpXmlMessageDTO extends WxMpXmlMessage {
    private String appId;
}
