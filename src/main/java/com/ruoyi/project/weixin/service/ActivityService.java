package com.ruoyi.project.weixin.service;

import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMp;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;

public interface ActivityService {

    /**
     * 关注公众号事件
     * @param inMessage
     * @param wxMp
     * @param template
     * @param openId
     */
    void subscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxActivityTemplate template, String openId);

    /**
     * 取消关注公众号事件
     * @param inMessage
     * @param wxMp
     * @param template
     * @param openId
     */
    void unsubscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxActivityTemplate template, String openId);
}
