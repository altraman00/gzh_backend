package com.ruoyi.project.activities.yunchan.yunchan001;

import com.ruoyi.project.activities.yunchan.yunchan001.service.Yunchan001ActivityHelpHandleService;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.server.WxSendMsgServer;
import com.ruoyi.project.weixin.service.ActivityService;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateMessageService;
import com.ruoyi.project.weixin.service.WxUserService;
import com.ruoyi.project.weixin.utils.SpringContextUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant.SCENE_HELP_WELCOME;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.activities.yunchan
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月25日 16:17
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 * 活动流程：
 * 1.关注事件：
 * -> 欢迎关注公众号，一站式孕产课程免费看点击XXX去参加活动吧！ <活动首页>
 * -> 如果有助力动作，则推送助力消息：您帮助您的好友？获取免费课程出了一份力，您也可以分享海报参加活动，免费学习孕产知识哟！！，点击《》参加活动
 *
 *
 *
 */

@Component
@Service
@Slf4j
@AllArgsConstructor
public class Yunchan001ActivityServiceImpl implements ActivityService {

    private final IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;

    private final WxSendMsgServer wxSendMsgServer;

    private WxUserService wxUserService;


    private final Yunchan001ActivityHelpHandleService yunchan001ActivityHelpHandleServer;


    @Override
    public String getActivityServiceImplClassName() {
        String classFullName = this.getClass().getName();
        return SpringContextUtils.getCurrentClassName(classFullName);
    }

    @Override
    @Async
    public void subscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate template, String openId) {
        log.debug("yunchan001 subscrib : {}",openId);
        String appId = wxMp.getAppId();
        String templateId = template.getTemplateId();

        WxUser wxUser = wxUserService.findWxUserByOpenid(openId);


        //获取助力活动的所有配置项
        Map<String,WxMpActivityTemplateMessage> messages = wxMpActivityTemplateMessageService.findEnabledActivityTemplateMessages(appId,templateId);

        //发送欢迎语
        WxMpActivityTemplateMessage welcomeTemplate = messages.get(SCENE_HELP_WELCOME);
        log.debug("yunchan001 send welcome message:{}",welcomeTemplate);
        wxSendMsgServer.sendTextMessage(welcomeTemplate.getRepContent(),wxUser);

        //发送欢迎海报
        wxSendMsgServer.sendPosterMessage(messages.get(YunChan001Constant.SCENE_SUBSCRIB_POSTER),wxUser);

        //执行助力活动相关逻辑
        yunchan001ActivityHelpHandleServer.activityHelp(inMessage,openId,appId,templateId,messages);
    }


    @Override
    public void unsubscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate template, String openId) {

    }


}
