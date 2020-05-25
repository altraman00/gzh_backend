package com.ruoyi.project.weixin.service;

import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;

public interface ActivityService {

    /**最开始做的助理活动活动**/
    String ACTIVITY_HELP = "help000";

    /**糖知家的评测小工具活动**/
    String ACTIVITY_DIABETIS = "diabetesTesting";

    /**孕产001的助力活动**/
    String ACTIVITY_YUNCHAN001_HELP = "help";


    /**
     * 获取当前实现类的类名
     * @return
     */
    String getActivityServiceImplClassName();

    /**
     * 关注公众号事件
     * @param inMessage
     * @param wxMp
     * @param wxMpActivityTemplate
     * @param openId
     */
    void subscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate wxMpActivityTemplate, String openId);

    /**
     * 取消关注公众号事件
     * @param inMessage
     * @param wxMp
     * @param wxMpActivityTemplate
     * @param openId
     */
    void unsubscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate wxMpActivityTemplate, String openId);
}
