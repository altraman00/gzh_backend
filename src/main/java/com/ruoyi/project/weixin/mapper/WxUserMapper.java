package com.ruoyi.project.weixin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.project.weixin.entity.WxUser;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 微信用户
 *
 * @author JL
 * @date 2019-03-25 15:39:39
 */
public interface WxUserMapper extends BaseMapper<WxUser> {

    /**
     * 查询关注的且任务未完成的用户
     * @param appId
     * @param templateId
     * @return
     */
    List<WxUser> getNotCompleteUser(@Param("appId") String appId, @Param("templateId") String templateId);
}
