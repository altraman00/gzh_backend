package com.ruoyi.framework.config.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.framework.config.swagger
 * @Description : swagger忽略的参数 在不需要递归展开的属性上加上IgnoreSwaggerParameter注解就行
 * @Author : xiekun
 * @Create Date : 2020年05月27日 16:54
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreSwaggerParameter {

}
