package com.ruoyi.project.activities.yunchan.yunchan001;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.activities.security.annotation.ApiH5;
import com.ruoyi.project.activities.security.annotation.CurrentUser;
import com.ruoyi.project.activities.security.entity.SysUserInfo;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001HelpUserRecord;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001HelpUserRecordService;
import com.ruoyi.project.common.BaseResponse;
import com.ruoyi.project.common.ResultCode;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.service.WxUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 助力记录表 前端控制器
 * </p>
 *
 * @author xiekun
 * @since 2020-05-25
 */

@Api(value = "WxMpYunchan001HelpUserRecordController", tags = "孕产001助力详情 相关接口")
@ApiH5
@RestController
@RequestMapping("/open/mp/yunchan001/user/record")
public class Yunchan001HelpUserRecordController extends BaseController {

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private IWxMpYunchan001HelpUserRecordService wxMpYunchan001HelpUserRecordService;

    @ApiOperation("获取用户的助力队友列表")
    @GetMapping("")
    public BaseResponse<List<WxUser>> getUserStageStatus(@CurrentUser SysUserInfo sysUserInfo) {
        logger.info("【getUserStageStatus】sysUserInfo:{}",sysUserInfo);
        WxUser one = wxUserService.getOne(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getOpenId, sysUserInfo.getOpenId()));
        List<WxMpYunchan001HelpUserRecord> list = wxMpYunchan001HelpUserRecordService.list(Wrappers.<WxMpYunchan001HelpUserRecord>lambdaQuery()
                .eq(WxMpYunchan001HelpUserRecord::getInviteWxUserId, one.getId())
                .orderByAsc(WxMpYunchan001HelpUserRecord::getCreateTime)
                .last("limit 0,3"));
        List<String> collect = list.stream().map(t -> t.getHelpWxUserId()).collect(Collectors.toList());
        List<WxUser> helpUsers = wxUserService.lambdaQuery().in(true,WxUser::getId, collect).list();
        return new BaseResponse<>(ResultCode.SUCCESS, helpUsers);
    }

}
