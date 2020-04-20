package com.ruoyi.project.weixin.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.system.mapper.SysUserRoleMapper;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.service.IWxMpService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 自定义菜单
 *
 * @author JL
 * @date 2019-03-27 16:52:10
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/wxmp")
public class WxMpController extends BaseController {

	private final IWxMpService wxMpService;
	private final SysUserRoleMapper sysUserRoleMapper;

	/**
	 * 查询所有的公众号(管理员查全量)
	 * 兼容其他用户查询(根据角色配置的可见范围查询)
	 *
	 * @return R
	 */
	@GetMapping("/list")
//	@PreAuthorize("@ss.hasPermi('wxmp:wxmp:get')")
	public AjaxResult getWxMp(String name) {
		logger.debug("查询全量公众号列表 name:{}", name	);
		List<WxMp> result = new ArrayList<>();
		//校验当前用户是不是超级管理员
		if(SecurityUtils.isAdmin()){
			if(StringUtils.isEmpty(name)){
				result = wxMpService.list(Wrappers.<WxMp>lambdaQuery().eq(WxMp::getDelFlag, "0").orderByDesc(WxMp::getCreateTime));
			}else {
				result = wxMpService.list(Wrappers.<WxMp>lambdaQuery().eq(WxMp::getDelFlag, "0").like(WxMp::getAppName, name).orderByDesc(WxMp::getCreateTime));
			}
		}else {
			logger.debug("当前用户不是admin 根据角色配置的可见范围查询");
			List<String> mpScopeList = sysUserRoleMapper.getMpScopeByUserId(SecurityUtils.getLoginUserId());
			logger.debug("查询当前用户角色配置的可见范围 mpScopeList:{}", mpScopeList);
			if(mpScopeList != null && mpScopeList.size() != 0){
				Set<String> set = new HashSet<>();
				for (String mpScope : mpScopeList) {
					//将数据库的json数组转换为对象
					List<String> list = JSON.parseArray(mpScope, String.class);
					//set去重
					set.addAll(list);
				}
				//再次判断set是否有值
				logger.debug("查询当前用户角色配置的可见范围 set:{}", set);
				if(set != null && set.size() != 0){
					if(StringUtils.isEmpty(name)){
						result = wxMpService.list(Wrappers.<WxMp>lambdaQuery().eq(WxMp::getDelFlag, "0").in(WxMp::getId, set).orderByDesc(WxMp::getCreateTime));
					}else {
						result = wxMpService.list(Wrappers.<WxMp>lambdaQuery().eq(WxMp::getDelFlag, "0").in(WxMp::getId, set).like(WxMp::getAppName, name).orderByDesc(WxMp::getCreateTime));
					}
				}
			}
		}
		logger.debug("查询全量公众号列表 name:{} result:{}", name, result);
		return AjaxResult.success(result);
	}

}
