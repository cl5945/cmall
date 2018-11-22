package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by changlei on 2018/11/22.
 */
public class RedisPool {

    private static JedisPool pool; // Jedis 连接池

    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20")); //最大连接数

    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10")); //JedisPool中最大的idle空闲状态的jedis实例个数

    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.man.idle", "2")); //JedisPool中最小的idle空闲状态的jedis实例个数
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port", "6379")); //redis 服务器端口

    private static String redisIp = String.valueOf(PropertiesUtil.getProperty("redis.ip")); //redis 服务器端口

    private static Boolean testOnBorrow = Boolean.valueOf(PropertiesUtil.getProperty("redis.test.borrow", "true")); //在borrow 一个jedis实例的时候，是否进行验证操作，如果赋值为true则得到的jedis连接必然是可用的

    private static Boolean testOnReturn = Boolean.valueOf(PropertiesUtil.getProperty("redis.test.return", "true")); //在return一个jedis实例的时候，是否进行验证操作，如果赋值为true则放到jedisPool 的 jedis连接必然是可用的

    private static void initPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        jedisPoolConfig.setTestOnReturn(testOnReturn);
        jedisPoolConfig.setBlockWhenExhausted(true); // 连接耗尽时会阻塞，false 会抛出异常，true 会等待是否超时

        pool = new JedisPool(jedisPoolConfig, redisIp, redisPort, 1000 * 2);
    }

    static {
        initPool();
    }

    /**
     * 获取 jedis 实例
     *
     * @return
     */
    public static Jedis getJedis() {
        return pool.getResource();
    }

    /**
     * @param jedis
     */
    public static void returnResource(Jedis jedis) {
        pool.returnResource(jedis);
    }

    public static void returnBrokenResource(Jedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    public static void main(String[] args) {
        Jedis jedis = pool.getResource();
        jedis.set("cl", "cl");
        returnResource(jedis);

    }

}
