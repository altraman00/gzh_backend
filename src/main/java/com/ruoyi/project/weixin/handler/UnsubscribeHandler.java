package com.ruoyi.project.weixin.handler;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.DiabetesConstant;
import com.ruoyi.project.weixin.dto.WxMpXmlMessageDTO;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.mapper.WxUserMapper;
import com.ruoyi.project.weixin.service.WxMsgService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author JL
 */
@Slf4j
@Component
@AllArgsConstructor
public class UnsubscribeHandler extends AbstractHandler {

    /**糖知家URL**/
    @Value("${sunlands.diabetes-testing.url}")
    private String DIABETES_TESTING_URL;

    @Value("${sunlands.diabetes-testing.appid}")
    private String DIABETES_TESTING_APPID;

    private final WxMsgService wxMsgService;
    private final WxUserMapper wxUserMapper;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService wxMpService,
                                    WxSessionManager sessionManager) {
        WxMpXmlMessageDTO wxMpXmlMessageDTO = (WxMpXmlMessageDTO) wxMessage;
        String openId = wxMessage.getFromUser();
        log.info("取消关注用户 OPENID: " + openId);
        WxUser wxUser = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
                .eq(WxUser::getOpenId,openId).eq(WxUser::getAppId,wxMpXmlMessageDTO.getAppId()));
        if(wxUser!=null){
            wxUser.setSubscribe(ConfigConstant.SUBSCRIBE_TYPE_NO);
            wxUser.setCancelSubscribeTime(LocalDateTime.now());
            wxUserMapper.updateById(wxUser);
            //消息记录
            MsgHandler.getWxMpXmlOutMessage(wxMpXmlMessageDTO,null,wxUser,wxMsgService);

            //如果是糖知家的用户关注，调取糖知家接口更新状态
            try {
                String appId = ((WxMpXmlMessageDTO) wxMessage).getAppId();
                if (appId.equals(DIABETES_TESTING_APPID)) {
                    userUnSubscribe(wxMessage);
                }
            } catch (Exception e) {
                log.error("【SubscribeHandler】更新糖知家用户取关公众状态出错：" + e.getMessage());
            }


        }
        return null;
    }


    /**
     * 更新糖知家用户关注公众号状态
     * @param wxMessage
     */
    private void userUnSubscribe(WxMpXmlMessage wxMessage) {
        String openId = wxMessage.getOpenId();
        SubscribeVO subVo = new SubscribeVO();
        subVo.setAppId(((WxMpXmlMessageDTO) wxMessage).getAppId());
        subVo.setOpenId(openId);
        subVo.setSubscribed(1);
        String url = DIABETES_TESTING_URL + DiabetesConstant.DIABETES_TESTING_USER_SUBCRIBE_API;
        String params = JSONUtil.toJsonStr(subVo);
        logger.info("【SubscribeHandler】更新糖知家用户关注公众状态，url:{},params:{}", url, params);
        HttpUtil.post(url, params);
    }

}
