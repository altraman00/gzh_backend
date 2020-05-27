package com.ruoyi.project.activities.yunchan.yunchan001;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001UserStatus;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001UserStatusService;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateMessageService;
import com.ruoyi.project.weixin.service.WxUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author xiekun
 * @since 2020-05-25
 */
@Api(value = "WxMpYunchan001UserStatusController", tags = "孕产001用户关注 相关接口")
@RestController
@RequestMapping("/open/mp/yunchan001/user")
public class WxMpYunchan001UserStatusController extends BaseController {

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

    @ApiOperation("孕产001移动端微信授权")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "微信授权code", required = true, paramType = "String"),
            @ApiImplicitParam(name = "appId", value = "appId", required = true, paramType = "String"),
            @ApiImplicitParam(name = "parentOpenid", value = "分享人的openid", required = false, paramType = "String")
    })
    @GetMapping("/oauth2")
    public AjaxResult oauth2(
            @RequestParam(value = "code") String code
            , @RequestParam(value = "appId") String appId
            , @RequestParam(value = "parentOpenid", required = false, defaultValue = "") String parentOpenid) throws WxErrorException {

        //获取静默授权的access_token
        WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.switchoverTo(appId).oauth2getAccessToken(code);
        String openId = wxMpOAuth2AccessToken.getOpenId();
        //创建用户
        wxUserService.createSimpleWxUser(appId, openId, parentOpenid);
        return AjaxResult.success();
    }


    @ApiOperation("获取助力老师的微信二维码")
    @GetMapping("/{openId}/teacher")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "openId", value = "openId", required = true, paramType = "String"),
            @ApiImplicitParam(name = "appId", value = "appId", required = true, paramType = "String")
    })
    public AjaxResult getUserAssistanceTeacher(
            @PathVariable(value = "openId") String openId
            , @RequestParam(value = "appId") String appId) {

        WxMpYunchan001UserStatus userStatus = wxMpYunchan001UserStatusService.getOne(Wrappers.<WxMpYunchan001UserStatus>lambdaQuery()
                .eq(WxMpYunchan001UserStatus::getOpenId, openId), false);

        if (userStatus == null) {
            //判断用户是否存在，不存在则创建用户
            WxUser simpleWxUser = wxUserService.createSimpleWxUser(appId, openId, "");

            //查询老师二维码的的list
            WxMpActivityTemplateMessage mpTemplateMessage = wxMpActivityTemplateMessageService.findMpTemplateMessage(appId
                    , yunchan001ActivityService.getActivityServiceImplClassName()
                    , YunChan001Constant.SCENE_AIDE_TEACHER_QRCODE);

            List<String> strings = Arrays.asList(mpTemplateMessage.getRepContent().split(","));
            int random = new Random().nextInt(strings.size());
            String aideTeacherQrcode = strings.get(random);
            userStatus = new WxMpYunchan001UserStatus();
            userStatus.setAidTeacherQrcode(aideTeacherQrcode);
            userStatus.setAppId(appId);
            userStatus.setOpenId(openId);
            userStatus.setWxuserId(simpleWxUser.getId());
            wxMpYunchan001UserStatusService.save(userStatus);
        }

        return AjaxResult.success(userStatus.getAidTeacherQrcode());
    }


    @ApiOperation("获取用户的阶段解锁状态")
    @ApiImplicitParam(name = "openId", value = "openId", required = true, paramType = "String")
    @GetMapping("/{openId}/status")
    public AjaxResult getUserStageStatus(@PathVariable(value = "openId") String openId) {
        WxMpYunchan001UserStatus one = wxMpYunchan001UserStatusService.getOne(Wrappers.<WxMpYunchan001UserStatus>lambdaQuery()
                .eq(WxMpYunchan001UserStatus::getOpenId, openId), false);
        return AjaxResult.success(one);
    }

}
