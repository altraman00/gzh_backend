package com.ruoyi.project.weixin.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.system.mapper.SysUserRoleMapper;
import com.ruoyi.project.weixin.config.WxMpProperties;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.service.IWxMpService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

	private final IWxMpService iWxMpService;
	private final WxMpService wxMpService;
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
				result = iWxMpService.list(Wrappers.<WxMp>lambdaQuery().eq(WxMp::getDelFlag, "0").orderByDesc(WxMp::getCreateTime));
			}else {
				result = iWxMpService.list(Wrappers.<WxMp>lambdaQuery().eq(WxMp::getDelFlag, "0").like(WxMp::getAppName, name).orderByDesc(WxMp::getCreateTime));
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
						result = iWxMpService.list(Wrappers.<WxMp>lambdaQuery().eq(WxMp::getDelFlag, "0").in(WxMp::getId, set).orderByDesc(WxMp::getCreateTime));
					}else {
						result = iWxMpService.list(Wrappers.<WxMp>lambdaQuery().eq(WxMp::getDelFlag, "0").in(WxMp::getId, set).like(WxMp::getAppName, name).orderByDesc(WxMp::getCreateTime));
					}
				}
			}
		}
		logger.debug("查询全量公众号列表 name:{} result:{}", name, result);
		return AjaxResult.success(result);
	}

	/**
	 * 重新加载所有公众号信息到WxMpService对象中
	 *
	 * @return R
	 */
	@GetMapping("/reload")
	public AjaxResult loadWxMp() {
		try {
			logger.debug("重新加载所有公众号信息到WxMpService对象中");
			final List<WxMpProperties.MpConfig> configs = new ArrayList<>();
			QueryWrapper<WxMp> queryWrapper = new QueryWrapper<>();
			queryWrapper.lambda().eq(WxMp::getDelFlag, "0");
			List<WxMp> list = iWxMpService.list(queryWrapper);
			log.debug("从数据库获取所有有效的公众号 list:{}", list);
			for (WxMp wxMp : list) {
				WxMpProperties.MpConfig mpConfig = new WxMpProperties.MpConfig();
				mpConfig.setAppId(wxMp.getAppId());
				mpConfig.setSecret(wxMp.getSecret());
				mpConfig.setToken(wxMp.getToken());
				mpConfig.setAesKey(wxMp.getAesKey());
				configs.add(mpConfig);
			}
			if (configs == null) {
				throw new RuntimeException("大哥，拜托先看下项目首页的说明（readme文件），添加下相关配置，注意别配错了！");
			}

			wxMpService.setMultiConfigStorages(configs
					.stream().map(a -> {
						WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
						configStorage.setAppId(a.getAppId());
						configStorage.setSecret(a.getSecret());
						configStorage.setToken(a.getToken());
						configStorage.setAesKey(a.getAesKey());
						return configStorage;
					}).collect(Collectors.toMap(WxMpDefaultConfigImpl::getAppId, a -> a, (o, n) -> o)));
			return AjaxResult.success();
		}catch (Exception e){
			logger.error("重新加载所有公众号信息到WxMpService对象中时异常", e);
			return AjaxResult.error();
		}
	}

}
