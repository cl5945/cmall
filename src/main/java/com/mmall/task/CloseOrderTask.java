package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * Created by changlei on 2018/12/7.
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedissonManager redissonManager;

 /*   @PreDestroy
    public void delLock(){
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    }*/

    //    @Scheduled(cron = "0 */1 * * * ?") //每分钟的整数倍执行一次
    public void closeOrderTaskV1() {
        log.info("关闭订单，定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        orderService.closeOrder(hour);
        log.info("关闭订单，定时任务完成");
    }


/*    @Scheduled(cron = "0 *//*1 * * * ?") //每分钟的整数倍执行一次
     public void closeOrderTaskV2(){
        log.info("关闭订单V2，定时任务启动");
        Long timeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout"));  // timeout 锁的失效时间

        // System.currentTimeMillis() + timeout 锁如果在这时还没有释放，锁自动过期
        Long setNxResult = RedisShardedPoolUtil.setNx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis() + timeout));

        if(setNxResult != null && setNxResult.intValue() == 1){  //如果返回值是1，代表设置成功 获取锁
            closeOrder();
        } else {
            log.info("没有获得分布式锁：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }


        log.info("关闭订单V2，定时任务完成");
    }

    */

    /**
     * 关闭 order
     *//*
    private void closeOrder(){
        RedisShardedPoolUtil.expire(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,50); // 有效期50 秒
        log.info("获取{}，thredName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
        orderService.closeOrder(hour);
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("释放{}，thredName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        log.info("=================================");
    }*/

/*    @Scheduled(cron = "0 *//*1 * * * ?") //每分钟的整数倍执行一次
     public void closeOrderTaskV3(){
        log.info("关闭订单V3，定时任务启动");
        Long timeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout"));  // timeout 锁的失效时间

        // System.currentTimeMillis() + timeout ，这个是过期时间，这个时间后可以删掉这个锁 锁如果在这时还没有释放，锁自动过期
        Long setNxResult = RedisShardedPoolUtil.setNx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis() + timeout));

        if(setNxResult != null && setNxResult.intValue() == 1){  //如果返回值是1，代表设置成功 获取锁
            closeOrder();
        } else {
            String lockValueStr  = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            if(lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)){
                // 原子操作
                String getSetResult = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis() + timeout));
                // 再次用当前时间戳key的旧值->旧值判断，是否可以获取锁
                // 当key没有旧值时，即key不存在时，返回nil--> 获取锁
                // set新值获取旧值
                // (getSetResult != null && lockValueStr.equals(getSetResult)) 代表lockValueStr 没有被刷新而且上面已说明时间已过期，所以可以继续执行，获得锁
                if(getSetResult == null || (getSetResult != null && lockValueStr.equals(getSetResult))){
                    closeOrder();
                }else {
                    log.info("没有获得分布式锁：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            }else {
                log.info("没有获得分布式锁：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }

        }


        log.info("关闭订单V3，定时任务完成");
    }*/
    @Scheduled(cron = "0 */1 * * * ?") //每分钟的整数倍执行一次
    public void closeOrderTaskV4() {
        log.info("关闭订单V4，定时任务启动");
        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        try {
            // wait time 最好设置为零，如果waittime 时间太久可能第一个获取锁的已经执行结束，其他的还在等待，回再次执行
            if (getLock = lock.tryLock(0, 5, TimeUnit.SECONDS)) {
                log.info("redisson 过去分布式锁 {} ThreadName {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
//                orderService.closeOrder(hour);
            }else {
                log.info("没有获取到分布式锁 {} ThreadName {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("Redisson 分布式锁获取异常",e);
            e.printStackTrace();
        }  finally {
            if(!getLock){
                return;
            }
            lock.unlock();
            log.info("释放分布式锁");
        }

        log.info("关闭订单V4，定时任务完成");
    }

    /**
     * 关闭 order
     */
    private void closeOrder() {
        RedisShardedPoolUtil.expire(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, 5); // 有效期50 秒
        log.info("获取{}，thredName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        orderService.closeOrder(hour);
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("释放{}，thredName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        log.info("=================================");
    }

}
