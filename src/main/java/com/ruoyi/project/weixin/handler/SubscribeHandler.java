package com.ruoyi.project.weixin.handler;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.http.HttpClient;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.DiabetesConstant;
import com.ruoyi.project.weixin.dto.WxMpXmlMessageDTO;
import com.ruoyi.project.weixin.entity.WxAutoReply;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.mapper.WxUserMapper;
import com.ruoyi.project.weixin.service.WxAutoReplyService;
import com.ruoyi.project.weixin.service.WxMsgService;
import com.ruoyi.project.weixin.utils.LocalDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JL
 */
@Slf4j
@Component
//@AllArgsConstructor
public class SubscribeHandler extends AbstractHandler {

    /**
     * 糖知家URL
     **/
    @Value("${sunlands.diabetes-testing.url}")
    private String DIABETES_TESTING_URL;

    @Value("${sunlands.diabetes-testing.appid}")
    private String DIABETES_TESTING_APPID;

    @Autowired
    private WxAutoReplyService wxAutoReplyService;

    @Autowired
    private WxUserMapper wxUserMapper;

    @Autowired
    private WxMsgService wxMsgService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) {
        log.info("新关注用户 OPENID: " + wxMessage.getFromUser());
        log.info("新关注用户 wxMessage: {}", wxMessage);
        // 获取微信用户基本信息
        try {
            WxMpXmlMessageDTO wxMpXmlMessageDTO = (WxMpXmlMessageDTO) wxMessage;
            WxMpUser userWxInfo = weixinService.getUserService()
                    .userInfo(wxMessage.getFromUser(), null);
            if (userWxInfo != null) {
                // TODO 添加关注用户到本地数据库
                WxUser wxUser = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
                        .eq(WxUser::getOpenId, userWxInfo.getOpenId()).eq(WxUser::getAppId, wxMpXmlMessageDTO.getAppId()));
                if (wxUser == null) {//第一次关注
                    wxUser = new WxUser();
                    wxUser.setAppId(wxMpXmlMessageDTO.getAppId());
                    wxUser.setSubscribeNum(1);
                    this.setWxUserValue(wxUser, userWxInfo);
//						wxUser.setTenantId(wxApp.getTenantId());
                    wxUserMapper.insert(wxUser);
                } else {//曾经关注过
                    wxUser.setSubscribeNum(wxUser.getSubscribeNum() + 1);
                    this.setWxUserValue(wxUser, userWxInfo);
//						wxUser.setTenantId(wxApp.getTenantId());
                    wxUserMapper.updateById(wxUser);
                }
                //发送关注消息
                List<WxAutoReply> listWxAutoReply = wxAutoReplyService.list(Wrappers.<WxAutoReply>query()
                        .lambda().eq(WxAutoReply::getType, ConfigConstant.WX_AUTO_REPLY_TYPE_1).eq(WxAutoReply::getAppId, wxMpXmlMessageDTO.getAppId()));
                WxMpXmlOutMessage wxMpXmlOutMessage = MsgHandler.getWxMpXmlOutMessage(wxMpXmlMessageDTO, listWxAutoReply, wxUser, wxMsgService);

                //如果是糖知家的用户关注，调取糖知家接口更新状态
                try {
                    String appId = ((WxMpXmlMessageDTO) wxMessage).getAppId();
                    if (appId.equals(DIABETES_TESTING_APPID)) {
                        newUserSubscribe(wxMessage);
                    }
                } catch (Exception e) {
                    log.error("【SubscribeHandler】更新糖知家用户关注公众状态出错：" + e.getMessage());
                }

                return wxMpXmlOutMessage;
            }
        } catch (Exception e) {
            log.error("用户关注出错：" + e.getMessage());
        }

        return null;
    }


    /**
     * 更新糖知家用户关注公众号状态
     * @param wxMessage
     */
    private void newUserSubscribe(WxMpXmlMessage wxMessage) {
        String openId = wxMessage.getFromUser();
        Map<String,Object> map = new HashMap<>();
        map.put("openId",openId);
        map.put("appId",((WxMpXmlMessageDTO) wxMessage).getAppId());
        map.put("subscribed",1);
        String url = DIABETES_TESTING_URL + DiabetesConstant.DIABETES_TESTING_USER_SUBCRIBE_API;
        String params = JSONUtil.toJsonStr(map);
        logger.info("【SubscribeHandler】更新糖知家用户关注公众状态，url:{},params:{}", url, params);
        String result = HttpClient.doPost(url, params);
        logger.info("【SubscribeHandler】newUserSubscribe result:{}",result);
    }


    public static void setWxUserValue(WxUser wxUser, WxMpUser userWxInfo) {
        wxUser.setAppType(ConfigConstant.WX_APP_TYPE_2);
        wxUser.setSubscribe(ConfigConstant.SUBSCRIBE_TYPE_YES);
        wxUser.setSubscribeScene(userWxInfo.getSubscribeScene());
        wxUser.setSubscribeTime(LocalDateTimeUtils.timestamToDatetime(userWxInfo.getSubscribeTime() * 1000));
        wxUser.setOpenId(userWxInfo.getOpenId());
        wxUser.setNickName(userWxInfo.getNickname());
        wxUser.setSex(String.valueOf(userWxInfo.getSex()));
        wxUser.setCity(userWxInfo.getCity());
        wxUser.setCountry(userWxInfo.getCountry());
        wxUser.setProvince(userWxInfo.getProvince());
        wxUser.setLanguage(userWxInfo.getLanguage());
        wxUser.setRemark(userWxInfo.getRemark());
        wxUser.setHeadimgUrl(userWxInfo.getHeadImgUrl());
        wxUser.setUnionId(userWxInfo.getUnionId());
        wxUser.setGroupId(JSONUtil.toJsonStr(userWxInfo.getGroupId()));
        wxUser.setTagidList(userWxInfo.getTagIds());
        wxUser.setQrSceneStr(userWxInfo.getQrSceneStr());
        wxUser.setRemark(userWxInfo.getRemark());
    }


}
