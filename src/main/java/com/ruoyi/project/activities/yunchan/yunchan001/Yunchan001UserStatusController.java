package com.ruoyi.project.activities.yunchan.yunchan001;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.weixin.WeixinUtil;
import com.ruoyi.project.activities.security.annotation.ApiH5;
import com.ruoyi.project.activities.security.annotation.ApiH5SkipToken;
import com.ruoyi.project.activities.security.annotation.CurrentUser;
import com.ruoyi.project.activities.security.entity.SysUserInfo;
import com.ruoyi.project.activities.security.service.ApiH5TokenService;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001UserStatus;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001UserStatusService;
import com.ruoyi.project.common.BaseResponse;
import com.ruoyi.project.common.ResultCode;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateMessageService;
import com.ruoyi.project.weixin.service.WxUserService;
import com.ruoyi.project.weixin.utils.JSONUtils;
import io.swagger.annotations.*;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author xiekun
 * @since 2020-05-25
 */
@Api(value = "WxMpYunchan001UserStatusController", tags = "孕产001用户授权，获取助教二维码等 相关接口")
@ApiH5
@RestController
@RequestMapping("/open/mp/yunchan001/user")
public class Yunchan001UserStatusController extends BaseController {

    @Autowired
    private ApiH5TokenService tokenService;

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private IWxMpYunchan001UserStatusService wxMpYunchan001UserStatusService;

    @Autowired
    private IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;

    @Autowired
    private Yunchan001ActivityServiceImpl yunchan001ActivityService;

    @ApiH5SkipToken
    @ApiOperation("孕产001移动端微信授权")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "微信授权code", required = true, paramType = "String"),
            @ApiImplicitParam(name = "appId", value = "appId", required = true, paramType = "String"),
            @ApiImplicitParam(name = "parentOpenid", value = "分享人的openid", required = false, paramType = "String")
    })
    @GetMapping("/oauth2")
    public BaseResponse oauth2(
            @RequestParam(value = "code") String code
            , @RequestParam(value = "appId") String appId
            , @RequestParam(value = "parentOpenid", required = false, defaultValue = "") String parentOpenid) throws WxErrorException {

        logger.debug("【yunchan001-oauth2】get accesstoke by code : {}->{}",appId,code);
        //获取静默授权的access_token
        WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.switchoverTo(appId).oauth2getAccessToken(code);
        logger.info("【oauth2】wxMpOAuth2AccessToken:{}", JSONUtils.toJson(wxMpOAuth2AccessToken));
        WxMpUser wxMpUser = wxMpService.switchoverTo(appId).oauth2getUserInfo(wxMpOAuth2AccessToken, null);
        logger.info("【oauth2】wxMpUser:{}", JSONUtils.toJson(wxMpUser));
        String openId = wxMpOAuth2AccessToken.getOpenId();

        //创建用户
        WxUser simpleWxUser = wxUserService.createWxUser(wxMpUser);

        //第一次登录时初始化用户状态
        wxMpYunchan001UserStatusService.initUserStatus(simpleWxUser);

        //生成token
        SysUserInfo userInfo = SysUserInfo.builder()
                .id(simpleWxUser.getId())
                .openId(simpleWxUser.getOpenId())
                .build();
        String token = tokenService.getToken(userInfo);
        Map<String, String> resMap = new HashMap<String, String>() {{
            put("token", token);
            put("openId", openId);
        }};
        return new BaseResponse<>(ResultCode.SUCCESS, resMap);
    }


    @ApiOperation("获取助力老师的微信二维码")
    @GetMapping("/teacher")
    public BaseResponse<Map<String,String>> getUserAssistanceTeacher(
             @CurrentUser SysUserInfo sysUserInfo
    ) {
        String openId = sysUserInfo.getOpenId();
        WxMpYunchan001UserStatus userStatus =wxMpYunchan001UserStatusService.findUserStatusByOpenId(openId);
        Map<String,String> resMap = new HashMap<>();
        resMap.put("aidTeacherQrcode",userStatus.getAidTeacherQrcode());
        return new BaseResponse<>(ResultCode.SUCCESS, resMap);
    }


    @ApiOperation("获取用户的阶段解锁状态")
    @GetMapping("/status")
    public BaseResponse<WxMpYunchan001UserStatus> getUserStageStatus(@CurrentUser SysUserInfo sysUserInfo) {
        String openId = sysUserInfo.getOpenId();
        WxMpYunchan001UserStatus one = wxMpYunchan001UserStatusService.getOne(Wrappers.<WxMpYunchan001UserStatus>lambdaQuery()
                .eq(WxMpYunchan001UserStatus::getOpenId, openId), false);
        return new BaseResponse<>(ResultCode.SUCCESS, one);
    }

    @ApiOperation("移动端微信分享时获取签名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "url", value = "url", dataType = "String", required = true),
            @ApiImplicitParam(name = "appId", value = "appId", required = true, paramType = "String")
    })
    @GetMapping("/share/sign")
    public BaseResponse<Map<String, String>> wxShare(
             @ApiParam(name="appId",value="appId",required=true) String appId
            ,@ApiParam(name="url",value="分享链接",required=true) String url
    ) throws WxErrorException, IOException {
        String accessToken = wxMpService.switchoverTo(appId).getAccessToken();
        Map<String, String> ret = WeixinUtil.getShareSignByAccessToken(accessToken, url);
        logger.info("【wxShare】signature:{}",ret);
        return new BaseResponse<>(ResultCode.SUCCESS,ret);
    }


}
