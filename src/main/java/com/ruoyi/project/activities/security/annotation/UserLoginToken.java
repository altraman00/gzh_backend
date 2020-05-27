package com.ruoyi.project.activities.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Project : zhaopin-cloud
 * @Package Name : com.sunlands.zhaopin.framework.security.annotation
 * @Description : 需要登录才能进行操作的注解
 * @Author : xiekun
 * @Create Date : 2019年11月28日 21:06
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserLoginToken {
    boolean required() default true;
}
