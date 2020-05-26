package com.ruoyi.project.weixin.controller.yunchan.yunchan001;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.entity.yunchan.yunchan001.WxMpYunchan001HelpUserRecord;
import com.ruoyi.project.weixin.service.WxUserService;
import com.ruoyi.project.weixin.service.yunchan.yunchan001.IWxMpYunchan001HelpUserRecordService;
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
@RestController
@RequestMapping("/open/mp/yunchan001/user/record")
public class WxMpYunchan001HelpUserRecordController extends BaseController {

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private IWxMpYunchan001HelpUserRecordService wxMpYunchan001HelpUserRecordService;

    @ApiOperation("获取用户的助力队友列表")
    @ApiImplicitParam(name = "openId", value = "openId", required = true, paramType = "String")
    @GetMapping("/")
    public AjaxResult getUserStageStatus(@RequestParam(value = "openId") String openId) {

        WxUser one = wxUserService.getOne(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getOpenId, openId));
        List<WxMpYunchan001HelpUserRecord> list = wxMpYunchan001HelpUserRecordService.list(Wrappers.<WxMpYunchan001HelpUserRecord>lambdaQuery()
                .eq(WxMpYunchan001HelpUserRecord::getInviteWxUserId, one.getId())
                .orderByAsc(WxMpYunchan001HelpUserRecord::getCreateTime)
                .last("limit 0,3"));
        List<String> collect = list.stream().map(t -> t.getHelpWxUserId()).collect(Collectors.toList());
        List<WxUser> helpUsers = wxUserService.lambdaQuery().in(true,WxUser::getId, collect).list();
        return AjaxResult.success(helpUsers);
    }

}
