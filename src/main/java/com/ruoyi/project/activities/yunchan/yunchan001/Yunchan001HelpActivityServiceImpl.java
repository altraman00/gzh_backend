package com.ruoyi.project.activities.yunchan.yunchan001;

import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.activities.yunchan
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月25日 16:17
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Component
@Service
@Slf4j
public class Yunchan001HelpActivityServiceImpl implements ActivityService {

    @Override
    public String getActivityServiceImplClassName() {
        return ActivityService.ACTIVITY_HELP_YUNCHAN001;
    }

    @Override
    public void subscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate wxMpActivityTemplate, String openId) {
        String eventKey = inMessage.getEventKey();
        String appId = wxMp.getAppId();
        String templateId = wxMpActivityTemplate.getTemplateId();




    }

    @Override
    public void unsubscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate wxMpActivityTemplate, String openId) {

    }
}
