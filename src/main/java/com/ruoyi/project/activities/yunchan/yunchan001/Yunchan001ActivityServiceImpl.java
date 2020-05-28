package com.ruoyi.project.activities.yunchan.yunchan001;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.activities.yunchan.yunchan001.service.Yunchan001ActivityHelpHandleService;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.mapper.WxUserMapper;
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

import java.util.List;
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
    public String getActivityAliasName() {
        return YunChan001Constant.ACTIVITY_ALIAS_NAME;
    }

    @Override
    public String getActivityServiceImplClassName() {
        String classFullName = this.getClass().getName();
        return SpringContextUtils.getCurrentClassName(classFullName);
    }

    @Override
    @Async
    public void subscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate template, String openId) {
//        log.info("【yunchan001Subscrib】subscrib event inMessage:[{}],wxMp:[{}],template[{}],openId[{}]", inMessage, wxMp, template, openId);
//        String eventKey = inMessage.getEventKey();

//        QueryWrapper<WxMpActivityTemplateMessage> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda()
//                .eq(WxMpActivityTemplateMessage::getAppId, appId)
//                .eq(WxMpActivityTemplateMessage::getTemplateId,templateId)
//                .eq(WxMpActivityTemplateMessage::getActivityEnable,true);
//
//        List<WxMpActivityTemplateMessage> messages = wxMpActivityTemplateMessageService.list(queryWrapper);

//
//        String wxUserId = wxUser.getId();
//        Integer needNum = template.getNeedNum();
//        log.info("【yunchan001Subscrib】event key:[{}],openId:[{}],appId[{}]",eventKey,openId,appId);
//
//        // 首先判断是不是扫活动中的公众号二维码进入的 {prefix}_class_yunchan001@help_{inviter_openid}
//        if (StringUtils.isNotBlank(eventKey) && eventKey.contains(YunChan001Constant.MP_QRCODE_ACTIVITY_SCENE_EVENT_KEY)) {
//            String value = eventKey.substring(eventKey.lastIndexOf("@") + 1);
//
//            //活动的标示
//            String activityTag = value.split("_")[0];
//            //邀请人的公众号
//            String inviterOpenId = value.split("_")[1];
//
//            //孕产的助力活动
//            if(StringUtils.isNotEmpty(activityTag) && YunChan001Constant.MP_QRCODE_ACTIVITY_TYPE.equals(activityTag)){
//                log.info("【yunchan001Subscrib】启动yunchan001的助力活动");
//                //启动助力活动
//                yunchan001ActivityHelpHandleServer.activityHelp(inviterOpenId,openId, appId, templateId, messages, wxUser, wxUserId, needNum);
//            }else{
//                log.info("【yunchan001Subscrib】执行默认活动");
//            }
//
//        }

        String appId = wxMp.getAppId();
        String templateId = template.getTemplateId();

        WxUser wxUser = wxUserService.findWxUserByOpenid(openId);

        //获取助力活动的所有配置项
        Map<String,WxMpActivityTemplateMessage> messages = wxMpActivityTemplateMessageService.findActivityTemplateMessages(appId,templateId);

        //发送欢迎语
        WxMpActivityTemplateMessage welcomeTemplate = messages.get(SCENE_HELP_WELCOME);
        wxSendMsgServer.sendTextMessage(welcomeTemplate.getRepContent(),wxUser);

        //执行助力活动相关逻辑
        yunchan001ActivityHelpHandleServer.activityHelp(inMessage,openId,appId,templateId,messages);
    }


    @Override
    public void unsubscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate template, String openId) {

    }


}
