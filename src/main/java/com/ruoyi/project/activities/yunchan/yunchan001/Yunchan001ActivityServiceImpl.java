package com.ruoyi.project.activities.yunchan.yunchan001;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.mapper.WxUserMapper;
import com.ruoyi.project.weixin.server.WxSendMsgServer;
import com.ruoyi.project.weixin.service.ActivityService;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateMessageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

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
@AllArgsConstructor
public class Yunchan001ActivityServiceImpl implements ActivityService {

    private final IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;

    private final WxUserMapper wxUserMapper;

    private final WxSendMsgServer wxSendMsgServer;

    private final Yunchan001ActivityHelp yunchan001ActivityHelp;

    @Override
    public String getActivityServiceImplClassName() {
        return ActivityService.ACTIVITY_YUNCHAN001_HELP;
    }

    @Override
    @Async
    public void subscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate template, String openId) {
        log.info("【yunchan001Subscrib】subscrib event inMessage:[{}],wxMp:[{}],template[{}],openId[{}]", inMessage, wxMp, template, openId);
        String eventKey = inMessage.getEventKey();
        String appId = wxMp.getAppId();
        String templateId = template.getTemplateId();
        QueryWrapper<WxMpActivityTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(WxMpActivityTemplateMessage::getAppId, appId)
                .eq(WxMpActivityTemplateMessage::getTemplateId,templateId)
                .eq(WxMpActivityTemplateMessage::getActivityEnable,true);

        List<WxMpActivityTemplateMessage> messages = wxMpActivityTemplateMessageService.list(queryWrapper);
        WxUser wxUser = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
                .eq(WxUser::getOpenId,openId).eq(WxUser::getAppId,appId));

        String wxUserId = wxUser.getId();
        Integer needNum = template.getNeedNum();
        log.info("【yunchan001Subscrib】event key:[{}],openId:[{}],appId[{}]",eventKey,openId,appId);

        // 首先判断是不是扫活动中的公众号二维码进入的 {prefix}_class_yunchan001@help#{inviter_openid}
        if (StringUtils.isNotBlank(eventKey) && eventKey.contains(YunChan001Constant.MP_QRCODE_SCENE_EVENT_KEY)) {
            String value = eventKey.substring(eventKey.lastIndexOf("@") + 1);

            //活动的标示
            String activityTag = value.split("#")[0];

            //孕产的助力活动
            if(StringUtils.isNotEmpty(activityTag) && ActivityService.ACTIVITY_YUNCHAN001_HELP.equals(activityTag)){
                log.info("【yunchan001Subscrib】启动yunchan001的助力活动");
                //邀请人的公众号
                String inviterOpenId = value.split("#")[1];
                //启动助力活动
                yunchan001ActivityHelp.activityHelp(inviterOpenId,openId, appId, templateId, messages, wxUser, wxUserId, needNum);
                // 推送活动规则消息
                yunchan001ActivityHelp.executeActivityRule(messages,wxUser,templateId,appId);
                // 推送活动海报
                WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(YunChan001Constant.SCENE_ACTIVITY_POSTER)).findFirst().orElse(null);
                wxSendMsgServer.sendPosterMessage(message,wxUser);
            }else{
                log.info("【yunchan001Subscrib】执行默认活动");
            }

        }

    }


    @Override
    public void unsubscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate template, String openId) {

    }


}
