package com.ruoyi.project.weixin.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.http.HttpClient;
import com.ruoyi.project.weixin.constant.DiabetesConstant;
import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.service.ActivityService;
import com.ruoyi.project.weixin.service.WxUserService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.service.impl
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月13日 14:14
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
@Component
@Service
@Slf4j
public class DiabetesTestingReportActivityServiceImpl implements ActivityService {

    @Autowired
    private WxUserService wxUserService;

    @Value("${sunlands.diabetes-testing.url}")
    private String sunlandsDiabetesUrl;

    @Override
    @Async
    public void subscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxActivityTemplate template, String openId) {
        log.info("【DiabetesTestingSubscrib】subscrib event inMessage:[{}],wxMp:[{}],template[{}],openId[{}]", inMessage, wxMp, template, openId);
        sendSubscribeState(wxMp, openId,DiabetesConstant.EVENT_SUBSCRIBE);
    }

    /**
     * 取消关注糖知家用户关注公众号
     *
     * @param inMessage
     * @param wxMp
     * @param template
     * @param openId
     */
    @Override
    @Async
    public void unsubscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxActivityTemplate template, String openId) {
        log.info("【DiabetesTestingSubscrib】unsubscrib event inMessage:[{}],wxMp:[{}],template[{}],openId[{}]", inMessage, wxMp, template, openId);
        sendSubscribeState(wxMp, openId,DiabetesConstant.EVENT_UNSUBSCRIBE);
    }


    /**
     * 给糖知家发送关注事件
     * @param wxMp
     * @param openId
     * @param event
     */
    private void sendSubscribeState(WxMp wxMp, String openId,String event) {
        String appId = wxMp.getAppId();
        WxUser wxUser = wxUserService.getOne(Wrappers.<WxUser>lambdaQuery()
                .eq(WxUser::getOpenId,openId).eq(WxUser::getAppId,appId));

        String nickName = wxUser.getNickName();
        Map<String,String> paramsMap = new HashMap<String,String>(){{
            put("appId",appId);
            put("openId",openId);
            put("nickName",nickName);
            put("event", event);
        }};

        String url = sunlandsDiabetesUrl + DiabetesConstant.DIABETES_TESTING_PORTAL_API;
        String params = JSONUtil.toJsonStr(paramsMap);
        log.info("【DiabetesTestingReportActivity】,url:{},params:{}",url,params);
        String result = HttpClient.doPost(url, params);
        log.info("【DiabetesTestingReportActivity】,result:{}",result);
    }


}
