package com.ruoyi.project.weixin.service;

import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMp;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;

public interface ActivityService {
    void execute(WxMpXmlMessage inMessage, WxMp wxMp, WxActivityTemplate template, String openId);
}
