package com.ruoyi.project.activities.security.annotation;

import com.ruoyi.framework.web.exception.GlobalException;
import com.ruoyi.project.activities.security.entity.SysUserInfo;
import com.ruoyi.project.activities.security.service.ApiH5TokenService;
import com.ruoyi.project.common.ResultCode;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.service.WxUserService;
import com.ruoyi.project.weixin.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.servlet.http.HttpServletRequest;

/**
 * @Project : zhaopin-cloud
 * @Package Name : com.sunlands.zhaopin.framework.security.annotation
 * @Description : 增加方法注入，将含有 @CurrentUser 注解的方法参数注入当前登录用户
 * @Author : xiekun
 * @Create Date : 2019年11月29日 18:12
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
@Slf4j
public class CurrentUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private ApiH5TokenService tokenService;

    @Autowired
    private WxUserService userService;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {

        return methodParameter.getParameterType().isAssignableFrom(SysUserInfo.class)
                && methodParameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        Object loginUserObj = request.getAttribute("loginUser");

        String userStr = JSONUtils.objectToJson(loginUserObj);
        SysUserInfo loginUser = JSONUtils.fromJson(userStr, SysUserInfo.class);

        if(loginUser == null){
            //拦截器为空时，尝试从请求头中获取token并解析token的值
            String token = request.getHeader("token");
            loginUser = tokenService.unSignToken(token, SysUserInfo.class);
        }



        if(loginUser == null){
            throw new MissingServletRequestPartException("token");
        }else{
            String openid = loginUser.getOpenId();
            WxUser user = userService.findWxUserByOpenid(openid);
            if(user == null){
                log.debug("user openid is not exist : {}",loginUser.getOpenId());
                throw new GlobalException(ResultCode.FORBIDDEN);
            }
        }

        return loginUser;
    }
}
