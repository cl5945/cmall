package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by changlei on 2018/12/4.
 *
 * reids 分片连接池
 *
 */
public class RedisShardedPool {

    private static ShardedJedisPool shardedJedisPool; // ShardedJedis Pool 连接池

    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20")); //最大连接数

    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10")); //JedisPool中最大的idle空闲状态的jedis实例个数

    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.man.idle", "2")); //JedisPool中最小的idle空闲状态的jedis实例个数
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port", "6379")); //redis 服务器端口

    private static String redis1Ip = String.valueOf(PropertiesUtil.getProperty("redis1.ip")); //redis 服务器端口

    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port", "6380")); //redis 服务器端口

    private static String redis2Ip = String.valueOf(PropertiesUtil.getProperty("redis2.ip")); //redis 服务器端口

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

        JedisShardInfo jedisShardInfo1 = new JedisShardInfo(redis1Ip,redis1Port,1000 * 2);
        JedisShardInfo jedisShardInfo2 = new JedisShardInfo(redis2Ip,redis2Port,1000 * 2);
        List<JedisShardInfo> jedisShardInfoList = new ArrayList<JedisShardInfo>();
        jedisShardInfoList.add(jedisShardInfo1);
        jedisShardInfoList.add(jedisShardInfo2);
        //Hashing.MURMUR_HASH 对应一致性算法      md5Holder 对应MD5算法
        //Sharded.DEFAULT_KEY_TAG_PATTERN
        shardedJedisPool = new ShardedJedisPool(jedisPoolConfig,jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    static {
        initPool();
    }

    /**
     * 获取 jedis 实例
     *
     * @return
     */
    public static ShardedJedis getJedis() {
        return shardedJedisPool.getResource();
    }

    /**
     * @param jedis
     */
    public static void returnResource(ShardedJedis jedis) {
        shardedJedisPool.returnResource(jedis);
    }

    public static void returnBrokenResource(ShardedJedis jedis) {
        shardedJedisPool.returnBrokenResource(jedis);
    }

    public static void main(String[] args) {
        ShardedJedis jedis = shardedJedisPool.getResource();
        for(int i=0;i<10; i ++){
            jedis.set("cl"+i, "cl"+i);
        }
        returnResource(jedis);
    }

}
