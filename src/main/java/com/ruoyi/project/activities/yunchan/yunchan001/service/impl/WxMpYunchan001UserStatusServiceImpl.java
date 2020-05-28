package com.ruoyi.project.activities.yunchan.yunchan001.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001UserStatus;
import com.ruoyi.project.activities.yunchan.yunchan001.mapper.WxMpYunchan001UserStatusMapper;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001UserStatusService;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant.SCENE_AIDE_TEACHER_QRCODE;

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

    @Autowired
private IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;
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

    @Override
    public WxMpYunchan001UserStatus initUserStatus(WxUser wxUser) {
        WxMpYunchan001UserStatus userStatus = this.findUserStatusByOpenId(wxUser.getOpenId());
        if (userStatus == null) {
            //判断用户是否存在，不存在则创建用户

            //查询老师二维码的的list
            Map<String,WxMpActivityTemplateMessage> mpTemplateMessageMap = wxMpActivityTemplateMessageService.findActivityTemplateMessages(wxUser.getAppId(),YunChan001Constant.ACTIVITY_ALIAS_NAME,new String[]{SCENE_AIDE_TEACHER_QRCODE});
            WxMpActivityTemplateMessage mpTemplateMessage = mpTemplateMessageMap.get(YunChan001Constant.ACTIVITY_ALIAS_NAME);

            List<String> strings = Arrays.asList(mpTemplateMessage.getRepContent().split(","));
            int random = new Random().nextInt(strings.size());
            String aideTeacherQrcode = strings.get(random);
            userStatus = new WxMpYunchan001UserStatus();
            userStatus.setAidTeacherQrcode(aideTeacherQrcode);
            userStatus.setAppId(wxUser.getAppId());
            userStatus.setOpenId(wxUser.getOpenId());
            userStatus.setWxuserId(wxUser.getId());
            save(userStatus);
        }
        return null;
    }
}
