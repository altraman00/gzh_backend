package com.ruoyi.project.weixin.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Project : elearning
 * @Package Name : com.sunlands.common
 * @Description : 对象锁，根据指定对象加锁，注意一定要解锁，否者会导致locks持续增加，内存会爆
 *
 * @Author : tandy
 * @Create Date : 2018年10月26日 下午3:19
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
public class ObjectLockUtil {

    private static Logger logger = LoggerFactory.getLogger(ObjectLockUtil.class);

    public static ConcurrentHashMap locks = new ConcurrentHashMap<String,Object>();

    public static Object lock(String key){
        Object lock = new Object();
        Object result = locks.putIfAbsent(key,lock);
        if(null == result){
            result = lock;
        }
        logger.debug("lock by key : {}->{}",key,result);
        return result;
    }

    public static void unlock(String key) {
        logger.debug("unlock by key:{}",key);
        locks.remove(key);
    }
}
