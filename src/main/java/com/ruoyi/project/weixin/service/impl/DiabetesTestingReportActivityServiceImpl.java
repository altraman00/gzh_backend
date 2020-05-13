package com.ruoyi.project.weixin.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.HelpActivityConstant;
import com.ruoyi.project.weixin.entity.*;
import com.ruoyi.project.weixin.mapper.WxUserMapper;
import com.ruoyi.project.weixin.service.ActivityService;
import com.ruoyi.project.weixin.service.IWxMpTemplateMessageService;
import com.ruoyi.project.weixin.service.WxMsgService;
import com.sun.media.jfxmedia.logging.Logger;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.service.impl
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月13日 14:14
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Service
@Slf4j
@AllArgsConstructor
public class DiabetesTestingReportActivityServiceImpl implements ActivityService {

    /**糖知家，场景，已经关注公众号**/
    private final static String DIABETES_SCENE_FOLLOWED_VIPCN_YES = "has_followed_vipcn";

    /**糖知家，场景，未关注公众号**/
    private final static String DIABETES_SCENE_FOLLOWED_VIPCN_NO = "hasnot_followed_vipcn";

    /**糖知家URL**/
    private final static String DIABETES_URL = "http://gzh.supplus.cn/diabetes/api/";

    /**查看报告的地址**/
    private final static String URL_TESTING_REPORT = DIABETES_URL + "/diabetes-testing-report.html";

    /**进入测评的地址**/
    private final static String URL_ENTRY_TESTING = DIABETES_URL + "/diabetes-testing-entry.html";

    @Autowired
    private WxUserMapper wxUserMapper;

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private WxMsgService wxMsgService;

    @Autowired
    private IWxMpTemplateMessageService wxMpTemplateMessageService;

    @Override
    public void execute(WxMpXmlMessage inMessage, WxMp wxMp, WxActivityTemplate template, String openId) {

        log.info("【DiabetesTestingReport】event inMessage:[{}],wxMp:[{}],template[{}],openId[{}]", inMessage, wxMp, template,openId);

        String eventKey = inMessage.getEventKey();
        String appId = wxMp.getAppId();
        String templateId = template.getId();

        QueryWrapper<WxMpTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WxMpTemplateMessage::getAppId, appId).eq(WxMpTemplateMessage::getTemplateId, templateId);
        List<WxMpTemplateMessage> messages = wxMpTemplateMessageService.list(queryWrapper);

        WxUser wxUser = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
                .eq(WxUser::getOpenId, openId).eq(WxUser::getAppId, appId));
        String wxUserId = wxUser.getId();
        log.info("【DiabetesTestingReport】event key:[{}],openId:[{}],appId[{}]", eventKey, openId, appId);

        //查询糖知家，看该openId有没有做过测评
        try {
            String url = "http://gzh.supplus.cn/diabetes/api/report/"+openId;
            String result = HttpUtils.sendGet(url, openId);
            if(StringUtils.isNotEmpty(result)){
                JSONObject jsonObject = JSONUtil.parseObj(result);
                String code = jsonObject.get("code").toString();

                Boolean hasTested = StringUtils.isNotEmpty(code) && code.equals("200") ? true : false;
                if (hasTested) {
                    //发送查看报告的消息
                    executeHasTested(messages, wxUser);
                } else {
                    //发送推送用户使用测评工具的消息
                    executeHasNotTested(messages, wxUser);
                }
            }
        } catch (Exception e) {
            log.error("【DiabetesTestingReport】diabetes testing request error",e);
        }

    }

    /**
     * 发送查看报告的消息
     *
     * @param wxUser
     */
    private void executeHasTested(List<WxMpTemplateMessage> messages, WxUser wxUser) {
        WxMpTemplateMessage templateMessage = messages.stream()
                .filter(t -> DIABETES_SCENE_FOLLOWED_VIPCN_YES.equals(t.getScene()))
                .findFirst().orElse(null);
        boolean hasAvailableMessage = templateMessage != null && StringUtils.isNotBlank(templateMessage.getRepContent());
        if(hasAvailableMessage){
            String content = templateMessage.getRepContent();
            content = content.replace(HelpActivityConstant.PLACEHOLDER_SUBSCRIBE_NICKNAME,wxUser.getNickName());
            sendTextMessage(content,wxUser);
        }
    }

    /**
     * 发送推送用户使用测评工具的消息
     *
     * @param wxUser
     */
    private void executeHasNotTested(List<WxMpTemplateMessage> messages, WxUser wxUser) {

        WxMpTemplateMessage templateMessage = messages.stream()
                .filter(t -> DIABETES_SCENE_FOLLOWED_VIPCN_NO.equals(t.getScene()))
                .findFirst().orElse(null);
        boolean hasAvailableMessage = templateMessage != null && StringUtils.isNotBlank(templateMessage.getRepContent());
        if(hasAvailableMessage){
            String content = templateMessage.getRepContent();
            content = content.replace(HelpActivityConstant.PLACEHOLDER_SUBSCRIBE_NICKNAME,wxUser.getNickName());
            sendTextMessage(content,wxUser);
        }

    }

    /**
     * 发送查看报告的文本消息
     *
     * @param content
     * @param wxUser
     */
    private void sendTextMessage(String content, WxUser wxUser) {
        try {
            WxMpKefuMessage wxMpKefuMessage = WxMpKefuMessage
                    .TEXT()
                    .toUser(wxUser.getOpenId())
                    .content(content)
                    .build();
            wxMpService.switchoverTo(wxUser.getAppId()).getKefuService().sendKefuMessage(wxMpKefuMessage);
        } catch (Exception e) {
            log.error("【DiabetesTestingReport】发送客服消息失败，appId:{} openId：{}", wxUser.getAppId(), wxUser.getOpenId());
        }
        // 记录数据库
        WxMsg wxMsg = new WxMsg();
        wxMsg.setAppId(wxUser.getAppId());
        wxMsg.setNickName(wxUser.getNickName());
        wxMsg.setHeadimgUrl(wxUser.getHeadimgUrl());
        wxMsg.setType(ConfigConstant.WX_MSG_TYPE_2);
        wxMsg.setRepContent(content);
        wxMsg.setWxUserId(wxUser.getId());
        wxMsg.setRepType(ConfigConstant.MESSAGE_REP_TYPE_TEXT);
        wxMsgService.save(wxMsg);
    }


}
