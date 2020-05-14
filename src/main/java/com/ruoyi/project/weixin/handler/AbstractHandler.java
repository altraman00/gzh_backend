package com.ruoyi.project.weixin.handler;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.project.weixin.constant.DiabetesConstant;
import com.ruoyi.project.weixin.dto.WxMpXmlMessageDTO;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Binary Wang(https://github.com/binarywang)
 */
public abstract class AbstractHandler implements WxMpMessageHandler {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    class SubscribeVO{
        private String appId;
        private String openId;
        private Integer subscribed;

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public void setOpenId(String openId) {
            this.openId = openId;
        }

        public void setSubscribed(Integer subscribed) {
            this.subscribed = subscribed;
        }
    }

}
