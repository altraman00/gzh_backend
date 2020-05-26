package com.ruoyi.project.weixin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.weixin.entity.WxUser;
import me.chanjar.weixin.common.error.WxErrorException;

/**
 * 微信用户
 *
 * @author JL
 * @date 2019-03-25 15:39:39
 */
public interface WxUserService extends IService<WxUser> {

	/**
	 * 同步微信用户
	 */
	void synchroWxUser(String appId) throws WxErrorException;

	/**
	 * 修改用户备注
	 * @param entity
	 * @return
	 */
	boolean updateRemark(WxUser entity) throws WxErrorException;

	/**
	 * 认识标签
	 * @param taggingType
	 * @param tagId
	 * @param openIds
	 * @throws WxErrorException
	 */
	void tagging(String taggingType, Long tagId, String[] openIds, String appId) throws WxErrorException;

	WxUser getByOpenIdAndAppId(String openId, String appId);

	/**
	 * 简单的创建一个只有openId的微信用户
	 * @param appId
	 * @param openId
	 * @param parentOpenid
	 * @return
	 */
	WxUser createSimpleWxUser(String appId,String openId,String parentOpenid);
}
