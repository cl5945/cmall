package com.mmall.util;

import com.mmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * jedis 框架 操作 redis 的 api
 *
 * Created by changlei on 2018/11/22.
 */
@Slf4j
public class RedisPoolUtil {

    /**
     * jedis 设置 值
     * @param key
     * @param value
     * @return
     */
    public static String set(String key , String value){
        String result = null;
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.set(key,value);
        } catch (Exception e) {
            log.error("set key:{}  value:{} error",key,value,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 设置有效期，
     * @param key
     * @param value
     * @param exTime
     * @return
     */
    public static String setEx(String key , String value, int exTime){
        String result = null;
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.setex(key, exTime, value);
        } catch (Exception e) {
            log.error("setex key:{}  value:{} exTime:{} error",key,value,exTime,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * setEx
     * @param key
     * @param exTime
     * @return
     */
    public static Long expire(String key , int exTime){
        Long result = null;
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.expire(key, exTime);
        } catch (Exception e) {
            log.error("expire key:{}  value:{} exTime:{} error",key,exTime,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 通过key 获取value
     * @param key
     * @return
     */
    public static String get(String key){
        String result = null;
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("set key:{}  error",key,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 删除方法
     * @param key
     * @return
     */
    public static Long del(String key){
        Long result = null;
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del key:{}  error",key,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }


    public static void main(String[] args) {
        Jedis jedis = RedisPool.getJedis();

        RedisPoolUtil.set("keyTest","value");
//        RedisPoolUtil.expire("keyTest",100);
        RedisPoolUtil.setEx("keyTest","exvalue",100);
        String value = RedisPoolUtil.get("keyTest");

        RedisPoolUtil.del("keyTest");

        System.out.println("value:"+value);


    }

}
