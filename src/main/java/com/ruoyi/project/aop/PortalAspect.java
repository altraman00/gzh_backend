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

/**
 * @author zhangbin
 */
@Component
@Aspect
@Slf4j
@AllArgsConstructor
public class PortalAspect {

    private final IWxMpService iWxMpService;

    private final WxMpActivityTemplateServiceImpl IWxMpActivityTemplateService;

    @Pointcut("execution(* com.ruoyi.project.weixin.controller.WxPortalController.post(..))")
    public void portal() {

    }

    @After("portal()")
    public void doAfter(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        log.info("成功切向事件：{}",Arrays.toString(args));
        String requestBody = (String) args[1];
        String appId= (String) args[0];
        WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);

        WxMp wxMp = iWxMpService.getByAppId(appId);
        if (wxMp == null) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appId));
        }



        String eventKey = inMessage.getEventKey();
        if(StringUtils.isEmpty(eventKey)){
            //如果没有场景值，获取排序号为0的第一个活动模板作为主活动模板 执行关注事件

        }else{
            //如果有场景值，根据场景值获取对应的实现类执行关注事件

        }


        //遍历当前公众号所关联的所有活动模板
        List<WxMpActivityTemplate> templateList =  IWxMpActivityTemplateService.getActivityTemplatesByAppId(wxMp.getAppId());

        for(WxMpActivityTemplate wxMpActivityTemplate : templateList){
            log.info("发现活动模板:{},状态为：{},template id is : {},实现类：{}",wxMpActivityTemplate.getTemplateName(),wxMpActivityTemplate.isActivityEnable(),wxMpActivityTemplate.getTemplateId(),wxMpActivityTemplate.getTemplateClass());

            if(!wxMpActivityTemplate.isActivityEnable()){
                log.info("appId:[{}]已暂停活动，流程结束",appId);
                continue;
            }

            String openId= (String) args[5];

            WxMpActivityTemplate template = wxMpActivityTemplate;

            if(null != template){
                String templateClass = template.getTemplateClass();
                log.info("appId:{}所绑定活动为：{}，开始执行活动流程",appId,template.getTemplateName());
                ActivityService activityService  = (ActivityService) SpringBeanUtil.getBean(templateClass);
                if(activityService != null){
                    if (WxEvenConstant.EVENT_SUBSCRIBE.equals(inMessage.getEvent())) {
                        log.info("此事件为关注事件，开始执行活动流程:{}",activityService.getActivityServiceImplClassName());
                        activityService.subscrib(inMessage,wxMp,template,openId);
                    }

                    if(WxEvenConstant.EVENT_UNSUBSCRIBE.equals(inMessage.getEvent())){
                        activityService.unsubscrib(inMessage,wxMp,template,openId);
                    }
                }


            }

        }


    }
}
