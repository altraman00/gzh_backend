package com.ruoyi.project.activities.security.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ruoyi.project.activities.security.entity.SysUserInfo;
import com.ruoyi.project.common.ResultCode;
import com.ruoyi.framework.web.exception.GlobalException;
import com.ruoyi.project.weixin.utils.JSONUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Project : zhaopin-cloud
 * @Package Name : com.sunlands.zhaopin.framework.security.service
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2019年11月28日 22:03
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
@Service("TokenService")
public class ApiH5TokenService {

    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";

    @Value("${h5.jwt.secret:gzh_activities}")
    private String secret;

    @Value("${h5.jwt.expiration:21600000}")
    private String expiration = "0";

    public String getToken(SysUserInfo user) {

        String userStr = JSONUtils.objectToJson(user);

        // 将 user id 保存到 token 里面
        String token= JWT.create().withAudience(user.getId())
                .withIssuer("auth0")
                //用户信息生成token
                .withClaim(CLAIM_KEY_USERNAME,userStr)
                //token创建时间
                .withClaim(CLAIM_KEY_CREATED,new Date())
                //过期时间
                .withExpiresAt(new Date(System.currentTimeMillis() + Long.parseLong(expiration) * 1000))
                //token 的密钥
                .sign(Algorithm.HMAC256(secret));

        return token;
    }


    /**
     * 先验证token是否被伪造，然后解码token。
     * @param token 字符串token
     * @return 解密后的DecodedJWT对象，可以读取token中的数据。
     */
    public DecodedJWT unSignToken(String token) {
        // 执行认证
        if (token == null) {
            throw new GlobalException(ResultCode.NO_TOKEN);
        }

        DecodedJWT jwt;
        try {
            // 使用了HMAC256加密算法。
            // secret是用来加密数字签名的密钥。
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("auth0")
                    .build(); //Reusable verifier instance
            jwt = verifier.verify(token);
        } catch (JWTVerificationException exception){
            throw new GlobalException(ResultCode.FORBIDDEN);
        } catch (Exception e) {
            throw new GlobalException(ResultCode.ERROR_TOKEN);
        }
        return jwt;
    }

    /**
     * 从token中获取userInfo
     * @param token
     * @param classz
     * @param <T>
     * @return
     */
    public <T> T unSignToken(String token,Class<T> classz){
        DecodedJWT decode = unSignToken(token);
        String userInfo = decode.getClaim("sub").asString();
        SysUserInfo sysUserInfo = (SysUserInfo) JSONUtils.fromJson(userInfo, classz);
        return (T) sysUserInfo;
    }


}
