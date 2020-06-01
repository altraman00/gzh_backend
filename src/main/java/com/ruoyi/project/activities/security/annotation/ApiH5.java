package com.ruoyi.project.activities.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.activities.security.annotation
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月27日 13:51
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiH5 {
    boolean required() default true;
}
