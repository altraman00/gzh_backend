package com.ruoyi.project.activities.security.common;


import com.ruoyi.project.activities.security.entity.SysUserInfo;

/**
 * @Project : zhaopin-cloud
 * @Package Name : com.sunlands.zhaopin.framework.security.common
 * @Description : 用户登陆后，经过jwt解密token，将用户信息放如，全文都可以得到当前登陆者信息
 * @Author : xiekun
 * @Create Date : 2019年11月29日 17:53
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
public class LoginUserContext {

    private static ThreadLocal<SysUserInfo> threasLocal = new ThreadLocal<>();

    /**
     * 设置当前用户值
     * @param userInfo
     */
    public static void setUserInfo(SysUserInfo userInfo){
        SysUserInfo sysUserInfo = threasLocal.get();
        //如果当前上下文中没有登陆者信息，则将token解析获取的用户信息放入
        if(sysUserInfo == null){
            threasLocal.set(userInfo);
        }
    }

    /**
     * 获取当前登陆用户
     * @return
     */
    public static SysUserInfo getUserInfo(){
        return threasLocal.get();
    }

    /**
     * 退出
     */
    public static void removeUser(){
        threasLocal.remove();
    }

}
