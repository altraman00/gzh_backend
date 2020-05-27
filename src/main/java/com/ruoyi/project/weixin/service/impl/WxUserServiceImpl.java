package com.ruoyi.project.weixin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.handler.SubscribeHandler;
import com.ruoyi.project.weixin.mapper.WxUserMapper;
import com.ruoyi.project.weixin.service.WxUserService;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.entity.WxUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpUserService;
import me.chanjar.weixin.mp.api.WxMpUserTagService;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.chanjar.weixin.mp.bean.result.WxMpUserList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 微信用户
 *
 * @author JL
 * @date 2019-03-25 15:39:39
 */
@Slf4j
@Service
@AllArgsConstructor
public class WxUserServiceImpl extends ServiceImpl<WxUserMapper, WxUser> implements WxUserService {

	private final WxMpService wxService;

	private final WxUserMapper wxUserMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateRemark(WxUser entity) throws WxErrorException {
		String id = entity.getId();
		String remark = entity.getRemark();
		String openId = entity.getOpenId();
		entity = new WxUser();
		entity.setId(id);
		entity.setRemark(remark);
		super.updateById(entity);
		WxUser wxUser = getById(id);
		WxMpUserService wxMpUserService = wxService.switchoverTo(wxUser.getAppId()).getUserService();
		wxMpUserService.userUpdateRemark(openId,remark);
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void tagging(String taggingType,Long tagId,String[] openIds, String appId) throws WxErrorException {
		WxMpUserTagService wxMpUserTagService = wxService.switchoverTo(appId).getUserTagService();
		WxUser wxUser;
		if("tagging".equals(taggingType)){
			for(String openId : openIds){
				wxUser = baseMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
						.eq(WxUser::getOpenId,openId));
				Long[] tagidList = wxUser.getTagidList();
				List<Long> list = Arrays.asList(tagidList);
				list = new ArrayList<>(list);
				if(!list.contains(tagId)){
					list.add(tagId);
					tagidList = list.toArray(new Long[list.size()]);
					wxUser.setTagidList(tagidList);
					this.updateById(wxUser);
				}
			}
			wxMpUserTagService.batchTagging(tagId,openIds);
		}
		if("unTagging".equals(taggingType)){
			for(String openId : openIds){
				wxUser = baseMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
						.eq(WxUser::getOpenId,openId));
				Long[] tagidList = wxUser.getTagidList();
				List<Long> list = Arrays.asList(tagidList);
				list = new ArrayList<>(list);
				if(list.contains(tagId)){
					list.remove(tagId);
					tagidList = list.toArray(new Long[list.size()]);
					wxUser.setTagidList(tagidList);
					this.updateById(wxUser);
				}
			}
			wxMpUserTagService.batchUntagging(tagId,openIds);
		}
	}

	@Override
	public WxUser getByOpenIdAndAppId(String openId, String appId) {
		return this.getOne(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getAppId, appId)
				.eq(WxUser::getOpenId,openId));
	}

	/**
	 * 简单的创建一个只有openId的微信用户
	 * @param appId
	 * @param openId
	 * @param parentOpenid
	 * @return
	 */
	@Override
	public WxUser createSimpleWxUser(String appId, String openId, String parentOpenid) {
		WxUser byOpenIdAndAppId = this.getByOpenIdAndAppId(openId, appId);
		if(byOpenIdAndAppId == null){
			byOpenIdAndAppId = new WxUser();
			byOpenIdAndAppId.setAppId(appId);
			byOpenIdAndAppId.setOpenId(openId);
			byOpenIdAndAppId.setAppType(ConfigConstant.SUBSCRIBE_TYPE_WEBLICENS);
			byOpenIdAndAppId.setSubscribe(ConfigConstant.SUBSCRIBE_TYPE_NO);
			byOpenIdAndAppId.setSubscribeScene("ADD_SCENE_OTHERS");
			byOpenIdAndAppId.setUserSource(YunChan001Constant.ACTIVITY_ALIAS_NAME);
			byOpenIdAndAppId.setParentOpenid(parentOpenid);
			this.save(byOpenIdAndAppId);
		}
		return byOpenIdAndAppId;
	}

	@Override
	public WxUser findWxUserByOpenid(String openId) {
		return wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
				.eq(WxUser::getOpenId,openId));
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void synchroWxUser(String appId) throws WxErrorException {
		//先将已关注的用户取关
		WxUser wxUser = new WxUser();
		wxUser.setSubscribe(ConfigConstant.SUBSCRIBE_TYPE_NO);
		this.baseMapper.update(wxUser, Wrappers.<WxUser>lambdaQuery().eq(WxUser::getAppId, appId)
				.eq(WxUser::getSubscribe, ConfigConstant.SUBSCRIBE_TYPE_YES));
		WxMpUserService wxMpUserService = wxService.switchoverTo(appId).getUserService();
		this.recursionGet(wxMpUserService,null, appId);
	}

	/**
	 * 递归获取
	 * @param nextOpenid
	 */
	void recursionGet(WxMpUserService wxMpUserService,String nextOpenid, String appId) throws WxErrorException {
		WxMpUserList userList = wxMpUserService.userList(nextOpenid);
		List<WxUser> listWxUser = new ArrayList<>();
		List<WxMpUser> listWxMpUser = getWxMpUserList(wxMpUserService,userList.getOpenids());
		listWxMpUser.forEach(wxMpUser -> {
			WxUser wxUser = baseMapper.selectOne(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getAppId, appId)
					.eq(WxUser::getOpenId,wxMpUser.getOpenId()));
			if(wxUser == null){//用户未存在
				wxUser = new WxUser();
				wxUser.setAppId(appId);
				wxUser.setSubscribeNum(1);
			}
			SubscribeHandler.setWxUserValue(wxUser,wxMpUser);
			listWxUser.add(wxUser);
		});
		this.saveOrUpdateBatch(listWxUser);
		if(userList.getCount() >= 10000){
			this.recursionGet(wxMpUserService,userList.getNextOpenid(),appId);
		}
	}

	/**
	 * 分批次获取微信粉丝信息 每批100条
	 * @param wxMpUserService
	 * @param openidsList
	 * @return
	 * @throws WxErrorException
	 * @author
	 */
	private List<WxMpUser> getWxMpUserList(WxMpUserService wxMpUserService, List<String> openidsList) throws WxErrorException {
		// 粉丝openid数量
		int count = openidsList.size();
		if (count <= 0) {
			return new ArrayList<>();
		}
		List<WxMpUser> list = Lists.newArrayList();
		List<WxMpUser> followersInfoList;
		int a = count % 100 > 0 ? count / 100 + 1 : count / 100;
		for (int i = 0; i < a; i++) {
			if (i + 1 < a) {
				log.debug("i:{},from:{},to:{}", i, i * 100, (i + 1) * 100);
				followersInfoList = wxMpUserService.userInfoList(openidsList.subList(i * 100, ((i + 1) * 100)));
				if (null != followersInfoList && !followersInfoList.isEmpty()) {
					list.addAll(followersInfoList);
				}
			}
			else {
				log.debug("i:{},from:{},to:{}", i, i * 100, count - i * 100);
				followersInfoList = wxMpUserService.userInfoList(openidsList.subList(i * 100, count));
				if (null != followersInfoList && !followersInfoList.isEmpty()) {
					list.addAll(followersInfoList);
				}
			}
		}
		log.debug("本批次获取微信粉丝数：",list.size());
		return list;
	}
}
