package com.ruoyi.project.activities.yunchan.yunchan001.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001UserStatus;
import com.ruoyi.project.weixin.entity.WxUser;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xiekun
 * @since 2020-05-25
 */
public interface IWxMpYunchan001UserStatusService extends IService<WxMpYunchan001UserStatus> {

    /**
     * 根据openid查到用户状态
     * @param openid
     * @return
     */
    WxMpYunchan001UserStatus findUserStatusByOpenId(String openid);

    /**
     * 解锁第一阶段
     * @param openid
     */
    void unlockFirstStage(String openid);

    WxMpYunchan001UserStatus initUserStatus(WxUser wxUser);
}
