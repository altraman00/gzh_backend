package com.ruoyi.project.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author zhangbin
 */
@Component
@Aspect
@Slf4j
public class PortalAspect {

    @Pointcut("execution(* com.ruoyi.project.weixin.controller.WxPortalController.post(..))")
    public void portal() {

    }

    @AfterReturning("portal()")
    public void doAfterReturning(JoinPoint joinPoint) {
        log.info("成功切向事件：{}",Arrays.toString(joinPoint.getArgs()));
    }
}
