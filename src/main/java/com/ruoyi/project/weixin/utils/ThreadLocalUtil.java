package com.ruoyi.project.weixin.utils;

/**
 * @Classname ThreadLocalUtil
 * @Description
 * @Date 2019-10-28 11:27
 * @Created by pjz
 */
public class ThreadLocalUtil {
    private ThreadLocal<ThreadDataDTO> context = ThreadLocal.withInitial(() -> new ThreadDataDTO());

    private ThreadLocalUtil(){
    }

    private static class InstanceHolder{
        private static ThreadLocalUtil instance = new ThreadLocalUtil();
    }

    private class ThreadDataDTO {
        String appId;
    }


    public static ThreadLocalUtil getInstance(){
        return InstanceHolder.instance;
    }

    public static ThreadLocal<ThreadDataDTO> getContext(){
        return getInstance().context;
    }

    public static void setAppId(String appId){
        getContext().get().appId = appId;
    }

    public static String getAppId(){
        return getContext().get().appId;
    }


}
