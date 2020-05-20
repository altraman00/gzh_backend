package com.ruoyi.project.weixin.service;

import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplete;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;

public interface ActivityService {

    /**
     * 获取当前实现类的类名
     * @return
     */
    String getActivityServiceImplClassName();

    /**
     * 关注公众号事件
     * @param inMessage
     * @param wxMp
     * @param wxMpActivityTemplete
     * @param openId
     */
    void subscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplete wxMpActivityTemplete, String openId);

    /**
     * 取消关注公众号事件
     * @param inMessage
     * @param wxMp
     * @param wxMpActivityTemplete
     * @param openId
     */
    void unsubscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplete wxMpActivityTemplete, String openId);
}
