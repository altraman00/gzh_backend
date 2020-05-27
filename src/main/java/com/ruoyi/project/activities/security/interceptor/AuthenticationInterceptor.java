package com.ruoyi.project.activities.security.interceptor;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ruoyi.project.activities.security.annotation.ApiH5;
import com.ruoyi.project.activities.security.annotation.ApiH5SkipToken;
import com.ruoyi.project.activities.security.service.ApiH5TokenService;
import com.ruoyi.project.activities.security.common.LoginUserContext;
import com.ruoyi.project.activities.security.entity.SysUserInfo;
import com.ruoyi.project.common.ResultCode;
import com.ruoyi.project.exception.GlobalException;
import com.ruoyi.project.weixin.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * @Project : zhaopin-cloud
 * @Package Name : com.sunlands.zhaopin.framework.security.interceptor.config
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2019年11月28日 21:11
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Autowired
    private ApiH5TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) {

        // 从 http 请求头中取出 token
        String token = httpServletRequest.getHeader("Authorization");

        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod();

        Class<?> clazz = handlerMethod.getBeanType();
        if (clazz == BasicErrorController.class) {
            //如果是spring的错误类 直接放行
            return true;
        }

        //检查是否有passtoken注释，有则跳过认证,其余的全部要走认证
        if (method.isAnnotationPresent(ApiH5.class) || method.isAnnotationPresent(ApiH5SkipToken.class)) {
            if (method.isAnnotationPresent(ApiH5SkipToken.class)) {
                ApiH5SkipToken skipToken = method.getAnnotation(ApiH5SkipToken.class);
                if (skipToken.required()) {
                    return true;
                }
            } else {
                // 解密token并获取token中的信息
                try {
                    DecodedJWT decode = tokenService.unSignToken(token);
                    String userInfo = decode.getClaim("sub").asString();
                    Date expiresAt = decode.getExpiresAt();
                    long currTimestamp = System.currentTimeMillis();
                    long expireTimestamp = expiresAt.getTime();
                    //token过期失效
                    if (expireTimestamp < currTimestamp) {
                        throw new GlobalException(ResultCode.FORBIDDEN);
                    }
                    //解析token中带的用户信息
                    SysUserInfo sysUserInfo = JSONUtils.fromJson(userInfo, SysUserInfo.class);
                    LoginUserContext.setUserInfo(sysUserInfo);

                } catch (JWTDecodeException j) {
                    throw new GlobalException(ResultCode.ERROR_TOKEN);
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) {
        LoginUserContext.removeUser();

    }
}
