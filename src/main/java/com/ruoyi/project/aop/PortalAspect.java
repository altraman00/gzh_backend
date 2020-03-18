package com.ruoyi.project.aop;

import com.ruoyi.common.utils.SpringBeanUtil;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.weixin.constant.WxEvenConstant;
import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.service.ActivityService;
import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxMpService;
import com.ruoyi.project.weixin.service.impl.HelpActivityServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;

import java.util.Arrays;

/**
 * @author zhangbin
 */
@Component
@Aspect
@Slf4j
@AllArgsConstructor
public class PortalAspect {

    private final IWxMpService iWxMpService;

    private final IWxActivityTemplateService iWxActivityTemplateService;

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
        if (WxEvenConstant.EVENT_SUBSCRIBE.equals(inMessage.getEvent())) {
            log.info("此事件为关注事件，开始执行活动流程");
            WxMp wxMp = iWxMpService.getByAppId(appId);
            if (wxMp == null) {
                throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appId));
            }
            String templateId = wxMp.getTemplateId();
            if (StringUtils.isBlank(templateId)) {
                log.info("appId:[{}]无绑定活动模板，流程结束",appId);
                return;
            }
            if (!wxMp.isActivityEnable()) {
                log.info("appId:[{}]已暂停活动，流程结束",appId);
                return;
            }
            String openId= (String) args[5];
            WxActivityTemplate template = iWxActivityTemplateService.getById(templateId);
            String templateClass = template.getTemplateClass();
            ActivityService activityService  = (ActivityService) SpringBeanUtil.getBean(templateClass);
            log.info("appId:{}所绑定活动为：{}，开始执行活动流程",appId,template.getTemplateName());
            activityService.execute(inMessage,wxMp,template,openId);
        }
    }
}
