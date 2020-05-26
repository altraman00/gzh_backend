package com.ruoyi.project.weixin.controller.yunchan.yunchan001;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.entity.yunchan.yunchan001.WxMpYunchan001UserStatus;
import com.ruoyi.project.weixin.service.WxUserService;
import com.ruoyi.project.weixin.service.yunchan.yunchan001.IWxMpYunchan001UserStatusService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xiekun
 * @since 2020-05-25
 */
@RestController
@RequestMapping("/open/mp/yunchan001/user")
public class WxMpYunchan001UserStatusController extends BaseController {

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private IWxMpYunchan001UserStatusService wxMpYunchan001UserStatusService;

    @ApiOperation("孕产001移动端微信授权")
    @ApiImplicitParam(name = "code", value = "微信授权code", dataType = "String", required = true)
    @GetMapping("/oauth2")
    public AjaxResult oauth2(
             @RequestParam(value = "code") String code
            ,@RequestParam(value = "appId") String appId
            ,@RequestParam(value = "parentOpenid",required = false,defaultValue = "") String parentOpenid) throws WxErrorException {

        //获取静默授权的access_token
        WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.switchoverTo(appId).oauth2getAccessToken(code);
        String openId = wxMpOAuth2AccessToken.getOpenId();

        WxUser byOpenIdAndAppId = wxUserService.getByOpenIdAndAppId(openId, appId);
        if(byOpenIdAndAppId == null){
            byOpenIdAndAppId = new WxUser();
            byOpenIdAndAppId.setAppId(appId);
            byOpenIdAndAppId.setOpenId(openId);
            byOpenIdAndAppId.setAppType(ConfigConstant.SUBSCRIBE_TYPE_WEBLICENS);
            byOpenIdAndAppId.setSubscribe(ConfigConstant.SUBSCRIBE_TYPE_NO);
            byOpenIdAndAppId.setSubscribeScene("ADD_SCENE_OTHERS");
            byOpenIdAndAppId.setUserSource(YunChan001Constant.ACTIVITY_ALIAS_NAME);
            byOpenIdAndAppId.setParentOpenid(parentOpenid);
            wxUserService.save(byOpenIdAndAppId);
        }

        return AjaxResult.success();
    }

    /**
     * 获取用户的阶段解锁状态
     * @param openId
     * @return
     */
    @GetMapping("/{openId}/status")
    public AjaxResult getUserStageStatus(@PathVariable(value = "openId") String openId){
        WxMpYunchan001UserStatus one = wxMpYunchan001UserStatusService.getOne(Wrappers.<WxMpYunchan001UserStatus>lambdaQuery()
                .eq(WxMpYunchan001UserStatus::getOpenId, openId), false);
        return AjaxResult.success(one);

    }


}
