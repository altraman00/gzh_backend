package com.ruoyi.project.weixin.service;

import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpTemplateMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.weixin.vo.EditWxTemplateVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
public interface IWxMpTemplateMessageService extends IService<WxMpTemplateMessage> {

    WxMp bindWxActivityTemplate(String templateId, String appId);

    List<WxMpTemplateMessage> getMpTemplateMessageList(String appId);

    WxMpTemplateMessage updateMpTemplateMessage(String id, EditWxTemplateVO editWxTemplateVO);

    void editActivityStatus(String appId, Boolean status);

    Map<String, Object> previewPoster(String messageId);
}
