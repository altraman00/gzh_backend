package com.ruoyi.project.weixin.handler;

import ch.qos.logback.core.net.SyslogOutputStream;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.http.HttpClient;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.DiabetesConstant;
import com.ruoyi.project.weixin.dto.WxMpXmlMessageDTO;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.mapper.WxUserMapper;
import com.ruoyi.project.weixin.service.WxMsgService;
import com.ruoyi.project.weixin.service.WxUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author JL
 */
@Slf4j
@Component
public class UnsubscribeHandler extends AbstractHandler {

    /**糖知家URL**/
    @Value("${sunlands.diabetes-testing.url}")
    private String DIABETES_TESTING_URL;

    @Value("${sunlands.diabetes-testing.appid}")
    private String DIABETES_TESTING_APPID;

    @Autowired
    private WxMsgService wxMsgService;

    @Autowired
    private WxUserMapper wxUserMapper;

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
        String openId = wxMessage.getFromUser();
        Map<String,Object> map = new HashMap<>();
        map.put("openId",openId);
        map.put("appId",((WxMpXmlMessageDTO) wxMessage).getAppId());
        map.put("subscribed",2);

        String url = DIABETES_TESTING_URL + DiabetesConstant.DIABETES_TESTING_USER_SUBCRIBE_API;
        String params = JSONUtil.toJsonStr(map);
        logger.info("【SubscribeHandler】更新糖知家用户关注公众状态，url:{},params:{}", url, params);
        String result = HttpClient.doPost(url, params);
        logger.info("【SubscribeHandler】userUnSubscribe result:{}",result);
    }


    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("openId","ok3-nwhW7u55FL856JGLTxreBshs");
        map.put("appId","wx2ae24112f4d17301");
        map.put("subscribed",1);
        String params = JSONUtil.toJsonStr(map);
        String url = "http://127.0.0.1:8081/user/gzh/subscribe";
        System.out.println("url"+ url+"\nparams"+ params);
        String result = HttpClient.doPost(url, params);
        System.out.println(result);
    }

}
