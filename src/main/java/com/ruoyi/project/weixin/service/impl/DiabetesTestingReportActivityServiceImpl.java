package com.ruoyi.project.weixin.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpClient;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.DiabetesConstant;
import com.ruoyi.project.weixin.dto.WxMpXmlMessageDTO;
import com.ruoyi.project.weixin.entity.*;
import com.ruoyi.project.weixin.service.ActivityService;
import com.ruoyi.project.weixin.service.IWxMpTemplateMessageService;
import com.ruoyi.project.weixin.service.WxMsgService;
import com.ruoyi.project.weixin.service.WxUserService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.service.impl
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月13日 14:14
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
@Component
@Service
@Slf4j
public class DiabetesTestingReportActivityServiceImpl implements ActivityService {

    @Override
    @Async
    public void subscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxActivityTemplate template, String openId) {

        log.info("【DiabetesTestingSubscrib】subscrib event inMessage:[{}],wxMp:[{}],template[{}],openId[{}]", inMessage, wxMp, template, openId);

    }


    /**
     * 取消关注糖知家用户关注公众号
     *
     * @param inMessage
     * @param wxMp
     * @param template
     * @param openId
     */
    @Override
    @Async
    public void unsubscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxActivityTemplate template, String openId) {

        log.info("【DiabetesTestingSubscrib】unsubscrib event inMessage:[{}],wxMp:[{}],template[{}],openId[{}]", inMessage, wxMp, template, openId);

    }


}
