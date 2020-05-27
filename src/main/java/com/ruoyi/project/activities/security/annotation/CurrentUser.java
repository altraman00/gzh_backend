package com.ruoyi.project.activities.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Project : zhaopin-cloud
 * @Package Name : com.sunlands.zhaopin.framework.security.annotation
 * @Description :
 * @Author : xiekun
 * @Create Date : 2019年11月29日 18:11
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */


/**
 * <p>绑定当前登录的用户</p>
 * <p>不同于@ModelAttribute</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
