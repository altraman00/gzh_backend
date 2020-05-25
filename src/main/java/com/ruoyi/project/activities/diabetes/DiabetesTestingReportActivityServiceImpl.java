package com.ruoyi.project.activities.diabetes;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpClient;
import com.ruoyi.project.weixin.constant.DiabetesConstant;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.service.ActivityService;
import com.ruoyi.project.weixin.service.WxUserService;
import com.ruoyi.project.weixin.utils.SpringContextUtils;
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

    /**
     * 用于微信带参二维码的活动标识
     */
    String SCENE_EVENT_KEY_DIABETES_TEST_H5 = "diabetes_test_h5";


    @Override
    public String getActivityServiceImplClassName() {
        String classFullName = this.getClass().getName();
        return SpringContextUtils.getCurrentClassName(classFullName);
    }

    @Override
    @Async
    public void subscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate template, String openId) {
        log.info("【DiabetesTestingSubscrib】subscrib event inMessage:[{}],wxMp:[{}],template[{}],openId[{}]", inMessage, wxMp, template, openId);

        String eventKey = inMessage.getEventKey();

        //如果公众号二维码中不带有场景参数，则执行默认的活动
        if(StringUtils.isEmpty(eventKey)){
            try {
                sendSubscribeState(wxMp, openId,DiabetesConstant.EVENT_SUBSCRIBE);
            } catch (Exception e) {
                log.error("【DiabetesTestingSubscrib】调取糖知家关注接口异常");
            }
        }

        //如果公众号二维码中带有场景参数，则执行对应的活动
        if(SCENE_EVENT_KEY_DIABETES_TEST_H5.equals(eventKey)){
            try {
                sendSubscribeState(wxMp, openId,DiabetesConstant.EVENT_SUBSCRIBE);
            } catch (Exception e) {
                log.error("【DiabetesTestingSubscrib】调取糖知家关注接口异常");
            }
        }

        try {
            sendSubscribeState(wxMp, openId,DiabetesConstant.EVENT_SUBSCRIBE);
        } catch (Exception e) {
            log.error("【DiabetesTestingSubscrib】调取糖知家关注接口异常");
        }
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
    public void unsubscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate template, String openId) {
        log.info("【DiabetesTestingSubscrib】unsubscrib event inMessage:[{}],wxMp:[{}],template[{}],openId[{}]", inMessage, wxMp, template, openId);
        try {
            sendSubscribeState(wxMp, openId,DiabetesConstant.EVENT_UNSUBSCRIBE);
        } catch (Exception e) {
            log.error("【DiabetesTestingSubscrib】调取糖知家取消关注接口异常");
        }
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
