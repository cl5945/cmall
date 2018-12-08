package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by changlei on 2018/12/8.
 */
@Component
@Slf4j
public class RedissonManager {

    private Config config = new Config();

    public Redisson getRedisson() {
        return redisson;
    }

    private Redisson redisson = null;

    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port", "6379")); //redis 服务器端口

    private static String redis1Ip = String.valueOf(PropertiesUtil.getProperty("redis1.ip")); //redis 服务器端口

    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port", "6380")); //redis 服务器端口

    private static String redis2Ip = String.valueOf(PropertiesUtil.getProperty("redis2.ip")); //redis 服务器端口

    @PostConstruct
    private void init(){
        try {
            config.useSingleServer().setAddress(new StringBuilder(redis1Ip).append(":").append(redis1Port).toString());
            redisson = (Redisson) Redisson.create(config);
            log.info("初始化Redissson 结束");
        } catch (Exception e) {
            log.error("初始化出异常",e);
            e.printStackTrace();
        }
    }


}
