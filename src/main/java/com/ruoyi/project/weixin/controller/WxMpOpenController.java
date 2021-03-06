package com.ruoyi.project.weixin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.activities.diabetes.testing.DiabetesTestingReportActivityServiceImpl;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.dto.WxMsgDTO;
import com.ruoyi.project.weixin.dto.WxPosterMsgDTO;
import com.ruoyi.project.weixin.entity.*;
import com.ruoyi.project.weixin.server.WxSendMsgServer;
import com.ruoyi.project.weixin.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动模板表 前端控制器
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@Api("公众号相关")
@RestController
@RequestMapping("/open/wxmp")
@AllArgsConstructor
@Slf4j
public class WxMpOpenController extends BaseController {

    private final IWxMpService myWxMpService;

    private final WxSendMsgServer wxSendMsgServer;

    private final IWxActivityTemplateService wxActivityTemplateService;

    private final IWxMpActivityTemplateService wxMpActivityTemplateService;

    private final IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;

    private final WxMpService wxMpService;

    private final IWxMpActivityTemplateService IWxMpActivityTemplateService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private DiabetesTestingReportActivityServiceImpl activityService;


    @ApiOperation("获取access_token")
    @ApiImplicitParam(name = "appId", value = "获取access_token", dataType = "String",required = true)
    @GetMapping("/access_token")
    public AjaxResult getAccessToken(@RequestParam(value = "appId") String appId) throws WxErrorException {
        String accessToken = wxMpService.switchoverTo(appId).getAccessToken();
        return AjaxResult.success(accessToken);
    }


    @ApiOperation("获取access_toke by code")
    @ApiImplicitParam(name = "appId", value = "获取access_token", dataType = "String",required = true)
    @GetMapping("/access_token_by_code")
    /**
     * 返回格式
     * {
     *     "msg": "操作成功",
     *     "code": 200,
     *     "data": {
     *         "accessToken": "33_45mrxYfcsJ98uSBrVS4IG3S_TAqbIiouMmDsld97giV4vPeELOpkbCIckjaTr-anygxdKeRWjIRPwQ5OwwJYNwCIHdLpZ6xifGaMYPb2Lc8",
     *         "expiresIn": 7200,
     *         "refreshToken": "33_ZC2tsTEya2NmvrCVXSLtzIN_vCI8kFLVev6KR4YAhc7Tc9s94bWGrZCg1oqc-QRkAYI_cAC_SqVUjD0VxwZiuBJfr728Rpqspf2p0h6YsKs",
     *         "openId": "ogjgCj4s_f8p45PuAxe-Fx36pXlU",
     *         "scope": "snsapi_base",
     *         "unionId": null
     *     }
     * }
     */
    public AjaxResult getAccessTokenByCode(@RequestParam(value = "appId") String appId,@RequestParam(value = "code") String code) throws WxErrorException {

        logger.debug("get accesstoke by code : {}->{}",appId,code);
        WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.switchoverTo(appId).oauth2getAccessToken(code);
        logger.debug("get accesstoke by code : {}-{}->{}",appId,code,wxMpOAuth2AccessToken);

        return AjaxResult.success(wxMpOAuth2AccessToken);
    }


    @ApiOperation("测试用open接口")
    @GetMapping("/hello")
    public String openHello(@RequestParam(required = false, defaultValue = "gzh") String str) {
        String result = "hello_env_" + str;
        logger.debug("hello,env:{},str:{}", str);
        String actityServiceImplClassName = activityService.getActivityServiceImplClassName();
        return result + "_" + actityServiceImplClassName;
    }


    @ApiOperation("查询用户信息")
    @GetMapping("/userinfo")
    public AjaxResult checkUserSubscribeState(
            @RequestParam(value = "openId") String openId) {
        WxUser one = wxUserService.getOne(Wrappers.<WxUser>lambdaQuery()
                .eq(WxUser::getOpenId, openId).last("limit 0,1"), false);
        return AjaxResult.success(one);
    }

    @ApiOperation("获取活动模版")
    @GetMapping("/template")
    public List<WxMpActivityTemplateMessage> getActivityTemplate(@RequestParam(value = "appId") String appId) {
        logger.debug("【getActivityTemplate】appId:{}",appId);

        List<WxMpActivityTemplate> activityTemplates = wxMpActivityTemplateService.list(Wrappers.<WxMpActivityTemplate>lambdaQuery()
                .eq(WxMpActivityTemplate::getAppId, appId)
                .eq(WxMpActivityTemplate::isActivityEnable, true));

        List<WxMpActivityTemplateMessage> list = Lists.newArrayList();
        for(WxMpActivityTemplate wxMpActivityTemplate : activityTemplates){
            String templateId = wxMpActivityTemplate.getTemplateId();
            List<WxMpActivityTemplateMessage> templateMessages = wxMpActivityTemplateMessageService.list(
                    Wrappers.<WxMpActivityTemplateMessage>lambdaQuery()
                            .eq(WxMpActivityTemplateMessage::getAppId, appId)
                            .eq(WxMpActivityTemplateMessage::getTemplateId,templateId)
//                            .eq(WxMpActivityTemplateMessage::getActivityEnable, true)
            );
            list.addAll(templateMessages);
        }

        return list;
    }


    @ApiOperation("发送文本消息")
    @PostMapping("/send/text_msg")
    public void sendGzhTextMsg(@RequestBody List<WxMsgDTO> wxMsgDTOs) {
        wxMsgDTOs.forEach(t -> {
            String openId = t.getOpenId();
            //查询用户
            WxUser wxUser = wxUserService.getOne(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getOpenId, openId).last("limit 0,1"), false);
            String content = t.getContent();
            wxSendMsgServer.sendTextMessage(content,wxUser);
        });
    }

    @ApiOperation("发送海报消息")
    @PostMapping("/send/poster_msg")
    public void sendGzhPosterMsg(@RequestBody WxPosterMsgDTO posterMsgDTO) {

        log.info("【sendGzhPosterMsg】,posterMsgDTO:{}",posterMsgDTO);
        WxMpActivityTemplateMessage posterMsgTemplate = posterMsgDTO.getWxMpTemplateMessage();
        String openId = posterMsgDTO.getOpenId();
        WxUser wxUser = wxUserService.getOne(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getOpenId, openId).last("limit 0,1"), false);

        wxSendMsgServer.sendPosterMessage(posterMsgTemplate,wxUser);
    }



    @ApiOperation("根据appId生成公众号二维码")
    @ApiImplicitParams({
            @ApiImplicitParam(name="openId",value="openId",required=true,paramType="String"),
            @ApiImplicitParam(name="wxMpQrParams",value="公众号场景值，采用如下公式 alias:{活动别名}@{openid} 如：alias:diabetesActivity@xxxxx",required=true,paramType="String")
    })
    @GetMapping("/create/qr_code")
    public AjaxResult create_mp_qrcode(@RequestParam(value = "appId") String appId,
                                   @RequestParam(value = "wxMpQrParams", required = false) String wxMpQrParams
    ) {
        try {
            String s = wxSendMsgServer.generatorPosterMpQrcode(appId, wxMpQrParams);
            return AjaxResult.success("OK",s);
        }catch (Exception ex){
            return AjaxResult.error("failed:"+ex.getMessage());
        }
    }




    @ApiOperation("根据openId和appId创建微信用户，默认是没有关注公众号的")
    @PostMapping("/create_wxuser")
    public AjaxResult createWxuser(@RequestBody WxUser wxUser) {
        logger.info("【createWxuser】wxUser:{}", wxUser);

        String appId = wxUser.getAppId();
        String openId = wxUser.getOpenId();
        if(wxUser == null || StringUtils.isEmpty(appId) || StringUtils.isEmpty(openId)){
            return AjaxResult.error("wxUser缺少openId或者appId");
        }
        WxUser byOpenIdAndAppId = wxUserService.getByOpenId(openId);
        if(byOpenIdAndAppId == null){
            byOpenIdAndAppId = new WxUser();
            byOpenIdAndAppId.setAppId(appId);
            byOpenIdAndAppId.setOpenId(openId);
            byOpenIdAndAppId.setAppType(ConfigConstant.SUBSCRIBE_TYPE_WEBLICENS);
            byOpenIdAndAppId.setSubscribe(ConfigConstant.SUBSCRIBE_TYPE_NO);
            byOpenIdAndAppId.setSubscribeScene("ADD_SCENE_OTHERS");
            byOpenIdAndAppId.setUserSource(wxUser.getUserSource());
            byOpenIdAndAppId.setParentOpenid(wxUser.getParentOpenid());
            wxUserService.save(byOpenIdAndAppId);
        }

        return AjaxResult.success();
    }


    @ApiOperation("获取公众号基本信息")
    @ApiImplicitParam(name = "appIdentify", value = "公众号身份标识，目前单公众号，固定为online_study", dataType = "String",required = true)
    @GetMapping("/info")
    public AjaxResult getMpInfo(@RequestParam(value = "appIdentify") String appIdentify){
        Map<String,Object> map = new HashMap<>(16);
        // 根据定义好的公众号标识查找公众号
        QueryWrapper<WxMp> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_identify",appIdentify);
        WxMp wxMp = myWxMpService.getOne(queryWrapper);
        map.put("wxMp", wxMp);
        List<WxMpActivityTemplate> activityTemplates = IWxMpActivityTemplateService.getActivityTemplatesByAppIdentify(appIdentify);
        List<WxActivityTemplate> templates = Lists.newArrayList();
        for(WxMpActivityTemplate template : activityTemplates){
            String templateId = template.getTemplateId();
            // 查询当前公众号配置的活动模板
            WxActivityTemplate wxActivityTemplate;
            if (StringUtils.isNotEmpty(templateId)) {
                wxActivityTemplate = wxActivityTemplateService.getById(templateId);
                templates.add(wxActivityTemplate);
            }
        }
        map.put("template",templates);
        return AjaxResult.success(activityTemplates);
    }

    @ApiOperation("移动端微信授权")
    @ApiImplicitParam(name = "code", value = "微信授权code", dataType = "String",required = true)
    @GetMapping("/oauth2")
    public AjaxResult oauth2(@RequestParam(value = "code") String code, @RequestParam(value = "appId") String appId){
        // 目前只支持单公众号，返回默认公众号,若改造多公众号需根据appId查询
        Map<String,Object> map = new HashMap<>(16);
        try {
            //根据APPID获取WxMp对象
            WxMp wxMp = myWxMpService.getByAppId(appId);
            if(wxMp != null){
                WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.switchoverTo(appId).oauth2getAccessToken(code);
                WxMpUser wxMpUser = wxMpService.switchoverTo(appId).oauth2getUserInfo(wxMpOAuth2AccessToken, null);
                //根据openId获取对应的APPID
                map.put("accessToken",wxMpOAuth2AccessToken);
                map.put("wxMpUser",wxMpUser);
                map.put("wxMp",wxMp);
            }else {
                logger.debug("参数appId未匹配到对应实体对象 :{}", appId);
                return AjaxResult.success("参数appId未匹配到对应实体对象 APPID:" + appId);
            }
        } catch (Exception e) {
            log.error("调用微信授权异常",e);
        }
        return AjaxResult.success(map);
    }



    @ApiOperation("根据场景值获取指定appid的对应活动的配置项")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sceneKeys", value = "配置场景值,可从控制台查看获取，支持逗号分隔多个一次获取", dataType = "String",required = true),
            @ApiImplicitParam(name = "templateAlias", value = "每个活动模板会固定一个别名,可找后端开发老师获取", required = true, paramType = "String"),
            @ApiImplicitParam(name = "appid", value = "当前应用的appid", required = false, paramType = "String")
    })
    @GetMapping("/activity/template/msg/custom")
    public AjaxResult getMpActivityMessageByCustom(String sceneKeys,String templateAlias,String appid){
        log.debug("get activity template message by custom : {},{},{}",appid,templateAlias,sceneKeys);
        WxActivityTemplate template = wxActivityTemplateService.findActivityTemplateByAlias(templateAlias);
        if(template != null){
            log.debug("found template : {} with alias : {}",template.getTemplateName(),templateAlias);
            String[] keys = sceneKeys.split(",");
            Map<String,WxMpActivityTemplateMessage> result = wxMpActivityTemplateMessageService.findEnabledActivityTemplateMessages(appid,template.getId(),keys);
            return AjaxResult.success("OK",result);
        }
        return AjaxResult.success();
    }

}
