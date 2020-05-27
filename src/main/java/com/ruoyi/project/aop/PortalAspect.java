package com.ruoyi.project.aop;

import com.ruoyi.common.utils.SpringBeanUtil;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.weixin.constant.WxEvenConstant;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.service.ActivityService;
import com.ruoyi.project.weixin.service.IWxMpService;
import com.ruoyi.project.weixin.service.impl.WxMpActivityTemplateServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangbin
 */
@Component
@Aspect
@Slf4j
@AllArgsConstructor
public class PortalAspect {

    private final IWxMpService iWxMpService;

    private final WxMpActivityTemplateServiceImpl wxMpActivityTemplateService;

    @Pointcut("execution(* com.ruoyi.project.weixin.controller.WxPortalController.post(..))")
    public void portal() {

    }

    @After("portal()")
    public void doAfter(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String requestBody = (String) args[1];
        String appId= (String) args[0];
        String openId= (String) args[5];
        WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);

        WxMp wxMp = iWxMpService.getByAppId(appId);
        if (wxMp == null) {
            throw new IllegalArgumentException(String.format("【{}】未找到对应的配置，请核实！", appId));
        }
        String eventKey = inMessage.getEventKey();

        if(StringUtils.isNotEmpty(eventKey)){
            //如果有场景值，根据场景值获取对应的实现类执行关注事件
            log.info("【{}】({})关注事件场景值：{}",appId,openId,eventKey);
            String alias = getActivityAlias(eventKey);

            if(StringUtils.isNotEmpty(alias)){
                log.info("【{}】({})执行指定别名模板：{}",appId,openId,alias);
                WxMpActivityTemplate template = wxMpActivityTemplateService.findActivityTemplateByAppIdAndAlias(appId,alias);
                doTemplate(appId, openId, inMessage, wxMp, template);
            }else{
                //执行默认的活动模板
                doDefaultActivityTemplate(appId, openId, inMessage, wxMp);
            }
        }else{
            //执行默认的活动模板
            doDefaultActivityTemplate(appId, openId, inMessage, wxMp);
        }


    }

    /**
     * 公众号场景值 格式：alias:helpActivity@help#thisisopenid
     * @param eventKey
     * @return
     */
    private String getActivityAlias(String eventKey) {
        String regex = "alias:(\\w+)@.*";
        Pattern pattern = Pattern.compile(regex);

        Matcher m = pattern.matcher(eventKey);
        String str = null;
        if (m.find()) {
            str = m.group(1);
        }
        return str;
    }

    /**
     * 执行默认的活动模板  优先为主活动模板，如果没有设置主活动模板，就是用排序号考前的第一个活动模板
     * @param appId
     * @param openId
     * @param inMessage
     * @param wxMp
     */
    private void doDefaultActivityTemplate(String appId, String openId, WxMpXmlMessage inMessage, WxMp wxMp) {


        //如果没有场景值，获取排序号为0的第一个活动模板作为主活动模板 执行关注事件
        WxMpActivityTemplate template = wxMpActivityTemplateService.getMasterActivityTemplate(appId);
        if(template == null){
            template = wxMpActivityTemplateService.getFirstAvalibleTemplate(appId);
        }

        if(template != null) {
            log.info("【{}】({})执行默认活动模板:{}",appId,openId,template.getTemplateName());
            doTemplate(appId, openId, inMessage, wxMp, template);
        }
    }

    /**
     * 执行活动模板
     * @param appId
     * @param openId
     * @param inMessage
     * @param wxMp
     * @param template
     */
    private void doTemplate(String appId, String openId, WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate template) {
        if (null != template) {
            log.info("【{}】({})开始执行模板:{}",appId,openId,template.getTemplateName());
            String templateClass = template.getTemplateClass();

            ActivityService activityService = (ActivityService) SpringBeanUtil.getBean(templateClass);
            if (activityService != null) {
                log.info("【{}】found execute impl class : {}",appId,activityService.getClass().getName());
                if (WxEvenConstant.EVENT_SUBSCRIBE.equals(inMessage.getEvent())) {
                    activityService.subscrib(inMessage, wxMp, template, openId);
                }

                if (WxEvenConstant.EVENT_UNSUBSCRIBE.equals(inMessage.getEvent())) {
                    activityService.unsubscrib(inMessage, wxMp, template, openId);
                }
            }

        }
    }
}
