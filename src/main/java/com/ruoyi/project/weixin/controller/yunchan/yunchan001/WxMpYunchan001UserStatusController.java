package com.ruoyi.project.weixin.controller.yunchan.yunchan001;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.api.R;
import com.google.common.collect.Lists;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.activities.yunchan.yunchan001.Yunchan001ActivityServiceImpl;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.entity.yunchan.yunchan001.WxMpYunchan001UserStatus;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateMessageService;
import com.ruoyi.project.weixin.service.WxUserService;
import com.ruoyi.project.weixin.service.yunchan.yunchan001.IWxMpYunchan001UserStatusService;
import com.ruoyi.project.weixin.utils.JsonUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;

    @Autowired
    private Yunchan001ActivityServiceImpl yunchan001ActivityService;

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
        //创建用户
        wxUserService.createSimpleWxUser(appId,openId,parentOpenid);
        return AjaxResult.success();
    }


    /**
     * 获取助力老师的微信二维码
     * @param openId
     * @return
     */
    @GetMapping("/{openId}/teacher")
    public AjaxResult getUserAssistanceTeacher(
             @PathVariable(value = "openId") String openId
            ,@RequestParam(value = "appId") String appId){

        WxMpYunchan001UserStatus userStatus = wxMpYunchan001UserStatusService.getOne(Wrappers.<WxMpYunchan001UserStatus>lambdaQuery()
                .eq(WxMpYunchan001UserStatus::getOpenId, openId), false);

        if(userStatus == null){
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


    public static void main(String[] args) {
        List<Integer> list = Lists.newArrayList();
        for ( int i = 0; i < 10; i++ ) {
            int random = new Random().nextInt(3);
            list.add(random);
            System.out.println(random);
        }
        Map<Integer, Long> collect = list.stream().collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()));
        System.out.println(JsonUtils.toJson(collect));
    }



}
