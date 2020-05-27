package com.ruoyi.project.activities.yunchan.yunchan001;


import cn.hutool.json.JSONObject;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001UserStatus;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001UserStatusService;
import com.ruoyi.project.activities.yunchan.yunchan001.vo.WenjuanVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/open/mp/yunchan001/wenjuan")
@Api(value = "Yunchan001WenjuanController", tags = "孕产001问卷相关接口")
@Slf4j
public class Yunchan001WenjuanController {

    public static final String MONGO_COLLECTION_YUNCHAN_001_WENJUAN = "bi_yunchan_001_wenjuan";
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IWxMpYunchan001UserStatusService userStatusService;


    @ApiOperation("提交问卷接口")
    @PostMapping()
    public AjaxResult createWenjuan(@RequestBody WenjuanVO wenjuanVO){
        log.debug("commit wenjuan :{}",wenjuanVO);
        String openid = wenjuanVO.getOpenid();
        WxMpYunchan001UserStatus userStatus = userStatusService.findUserStatusByOpenId(openid);
        //只有锁定状态的可以提交问卷，提交以后解锁第一阶段状态
        if(userStatus.getFirstStageStatus().equals(WxMpYunchan001UserStatus.LOCK_STATUS_LOCKED)){

            JSONObject jsonObject = wenjuanVO.getAnswer();
            jsonObject.put("openid",wenjuanVO.getOpenid());
            mongoTemplate.insert(jsonObject, MONGO_COLLECTION_YUNCHAN_001_WENJUAN);
            //解锁第一阶段
            userStatusService.unlockFirstStage(openid);
            return AjaxResult.success();
        }else{
            return AjaxResult.error("100001","已经提交过问卷");
        }


    }


    @GetMapping("/hello")
    public AjaxResult hello(){
        return AjaxResult.success("OK");
    }

}
