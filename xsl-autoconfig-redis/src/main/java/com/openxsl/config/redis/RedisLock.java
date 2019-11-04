package com.openxsl.config.redis;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Redis实现分布式锁，推荐使用Zookeeper锁
 * @author xiongsl
 * @modify xiongsl-2019-08-29
 */
public class RedisLock {
	private static final Random RANDOM = new Random();
	private final Logger logger  = LoggerFactory.getLogger(this.getClass());
	
	private final ValueOperations<String,Object> valueOps;
	private final String key;
    private final long timeout;
    
    private long lockedValue = -1;
    private long expires = 3000L;
    
    @SuppressWarnings("unchecked")
	public RedisLock(RedisTemplate<String,?> redisTemplate, String key,long timeout){
        this.valueOps = (ValueOperations<String,Object>)redisTemplate.opsForValue();
        this.key = "LOCK_" + key;
        this.timeout = timeout;
    }
    /**
     * 设置锁的生命周期（毫秒，默认3000L）
     * @param expires
     */
    public void setExpires(long expires) {
    	this.expires = expires;
    }
    
    /**
     * 获取Redis锁
     */
    public boolean lock(){
        try {
            final long start = System.currentTimeMillis();
            final long value = start + expires;
            while (System.currentTimeMillis()-start < timeout){
                if (valueOps.setIfAbsent(key, value)){  //setNx(key,);
                	valueOps.getOperations().expire(key, expires, TimeUnit.MILLISECONDS);
                	lockedValue = value;
                	if (logger.isDebugEnabled()) {
                		logger.debug("获取锁[{}]成功",key);
                	}
                    return true;
                } else {
                	Thread.sleep(RANDOM.nextInt(100));
                }
            }
        } catch (Exception e) {
            logger.error("获取锁[{}]异常", key, e);
        }
        return false;
    }

    /**
     * 释放锁，要防止由于自身操作回来Timeout了且其它人设置了锁而误删
     */
    public void unlock(){
        try {
            if (lockedValue != -1){
            	long value = (Long)valueOps.get(key);
            	if (lockedValue < value) {  //其他人的锁
            		return;
            	}
            	valueOps.getOperations().delete(key);
            	if (logger.isDebugEnabled()) {
            		logger.debug("释放锁[{}]成功",key);
            	}
            }
        } catch (Exception e) {
            logger.error("释放锁[{}]异常", key, e);
        }
    }
        
}
