package com.ruoyi.project.activities.yunchan.yunchan001.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001UserStatus;
import com.ruoyi.project.activities.yunchan.yunchan001.mapper.WxMpYunchan001UserStatusMapper;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001UserStatusService;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xiekun
 * @since 2020-05-25
 */
@Service
public class WxMpYunchan001UserStatusServiceImpl extends ServiceImpl<WxMpYunchan001UserStatusMapper, WxMpYunchan001UserStatus> implements IWxMpYunchan001UserStatusService {


    @Override
    public WxMpYunchan001UserStatus findUserStatusByOpenId(String openid) {
        WxMpYunchan001UserStatus result = this.getOne(Wrappers.<WxMpYunchan001UserStatus>lambdaQuery().eq(WxMpYunchan001UserStatus::getOpenId,openid),false);
        return result;
    }

    @Override
    public void unlockFirstStage(String openid) {

        this.update(Wrappers.<WxMpYunchan001UserStatus>lambdaUpdate()
                .eq(WxMpYunchan001UserStatus::getOpenId,openid)
                .set(WxMpYunchan001UserStatus::getFirstStageStatus,WxMpYunchan001UserStatus.LOCK_STATUS_UNLOCK)
                .set(WxMpYunchan001UserStatus::getFirstStageUnlockTime, LocalDateTime.now()));

    }
}
