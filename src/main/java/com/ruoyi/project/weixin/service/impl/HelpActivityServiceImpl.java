package com.ruoyi.project.weixin.service.impl;

import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HelpActivityServiceImpl implements ActivityService {
    @Override
    public void execute(WxMpXmlMessage inMessage, WxMp wxMp, WxActivityTemplate template, String openId) {
        // 先判断是不是对应的扫码带参进入
        log.info("成功执行助理活动方法");
    }
}
