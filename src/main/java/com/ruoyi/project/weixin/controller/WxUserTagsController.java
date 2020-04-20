package com.ruoyi.project.weixin.controller;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.weixin.service.WxUserService;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.entity.WxUserTagsDict;
import com.ruoyi.project.weixin.utils.ThreadLocalUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpUserTagService;
import me.chanjar.weixin.mp.bean.tag.WxUserTag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信用户标签
 *
 * @author JL
 * @date 2019-03-25 15:39:39
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/wxusertags")
public class WxUserTagsController extends BaseController {

	private final WxMpService wxService;
	private final WxUserService wxUserService;

	/**
	* 获取微信用户标签
	* @return
	*/
	@PreAuthorize("@ss.hasPermi('wxmp:wxusertags:list')")
	@GetMapping("/list")
	public AjaxResult getWxUserList(String appId) {
		appId = ThreadLocalUtil.getAppId();
		logger.debug("getWxUserTagList 当前操作的APPID:{}", appId);
		WxMpUserTagService wxMpUserTagService = wxService.switchoverTo(appId).getUserTagService();
		try {
			List<WxUserTag> listWxUserTag =  wxMpUserTagService.tagGet();
			return AjaxResult.success(listWxUserTag);
		} catch (WxErrorException e) {
			e.printStackTrace();
			log.error("获取微信用户标签失败", e);
			return AjaxResult.error(e.getMessage());
		}
	}

	/**
	 * 获取微信用户标签字典
	 * @param appId
	 * @return
	 */
	@PreAuthorize("@ss.hasPermi('wxmp:wxusertags:list')")
	@GetMapping("/dict")
	public AjaxResult getWxUserTagsDict(String appId) {
		appId = ThreadLocalUtil.getAppId();
		logger.debug("getWxUserTagsDict 当前操作的APPID:{}", appId);
		WxMpUserTagService wxMpUserTagService = wxService.switchoverTo(appId).getUserTagService();
		try {
			List<WxUserTag> listWxUserTag =  wxMpUserTagService.tagGet();
			List<WxUserTagsDict> listWxUserTagsDict = new ArrayList<>();
			WxUserTagsDict wxUserTagsDict;
			for(WxUserTag wxUserTag : listWxUserTag){
				wxUserTagsDict = new WxUserTagsDict();
				wxUserTagsDict.setName(wxUserTag.getName());
				wxUserTagsDict.setValue(wxUserTag.getId());
				listWxUserTagsDict.add(wxUserTagsDict);
			}
			return AjaxResult.success(listWxUserTagsDict);
		} catch (WxErrorException e) {
			e.printStackTrace();
			log.error("获取微信用户标签字典失败", e);
			return AjaxResult.error(e.getMessage());
		}
	}

	/**
	 * 新增微信用户标签
	 * @return
	 */
	@PreAuthorize("@ss.hasPermi('wxmp:wxusertags:add')")
	@PostMapping
	public AjaxResult save(@RequestBody JSONObject data){
		String appId = ThreadLocalUtil.getAppId();
		logger.debug("saveUserTag 当前操作的APPID:{}", appId);
		WxMpUserTagService wxMpUserTagService = wxService.switchoverTo(appId).getUserTagService();
		String name = data.getStr("name");
//		WxMpUserTagService wxMpUserTagService = wxService.getUserTagService();
		try {
			return AjaxResult.success(wxMpUserTagService.tagCreate(name));
		} catch (WxErrorException e) {
			e.printStackTrace();
			log.error("新增微信用户标签失败", e);
			return AjaxResult.error(e.getMessage());
		}
	}

	/**
	 * 修改微信用户标签
	 * @return
	 */
	@PreAuthorize("@ss.hasPermi('wxmp:wxusertags:edit')")
	@PutMapping
	public AjaxResult updateById(@RequestBody JSONObject data){
		String appId = ThreadLocalUtil.getAppId();
		logger.debug("updateUserTagUserTagById 当前操作的APPID:{}", appId);
		WxMpUserTagService wxMpUserTagService = wxService.switchoverTo(appId).getUserTagService();
		Long id = data.getLong("id");
		String name = data.getStr("name");
//		WxMpUserTagService wxMpUserTagService = wxService.getUserTagService();
		try {
			return AjaxResult.success(wxMpUserTagService.tagUpdate(id,name));
		} catch (WxErrorException e) {
			e.printStackTrace();
			log.error("修改微信用户标签失败", e);
			return AjaxResult.error(e.getMessage());
		}
	}

	/**
	 * 删除微信用户标签
	 * @param id
	 * @param appId
	 * @return
	 */
	@PreAuthorize("@ss.hasPermi('wxmp:wxusertags:del')")
	@DeleteMapping
	public AjaxResult removeById(Long id,String appId){
		appId = ThreadLocalUtil.getAppId();
		logger.debug("removeUserTagById 当前操作的APPID:{}", appId);
		String finalAppId = appId;
		int count = wxUserService.count(Wrappers.<WxUser>lambdaQuery().and(wrapper -> wrapper.eq(WxUser::getAppId, finalAppId))
				.and(wrapper -> wrapper
						.eq(WxUser::getTagidList,"["+id+"]")
						.or()
						.like(WxUser::getTagidList,","+id+",")
						.or()
						.likeRight(WxUser::getTagidList,"["+id+",")
						.or()
						.likeLeft(WxUser::getTagidList,","+id+"]")));
		if(count>0){
			return AjaxResult.error("该标签下有用户存在，无法删除");
		}
		WxMpUserTagService wxMpUserTagService = wxService.switchoverTo(appId).getUserTagService();
		try {
			return  AjaxResult.success(wxMpUserTagService.tagDelete(id));
		} catch (WxErrorException e) {
			e.printStackTrace();
			log.error("删除微信用户标签失败", e);
			return AjaxResult.error(e.getMessage());
		}
	}
}
