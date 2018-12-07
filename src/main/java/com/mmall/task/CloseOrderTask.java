package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by changlei on 2018/12/7.
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService orderService;

//    @Scheduled(cron = "0 */1 * * * ?") //每分钟的整数倍执行一次
    public void closeOrderTaskV1(){
        log.info("关闭订单，定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
        orderService.closeOrder(hour);
        log.info("关闭订单，定时任务完成");
    }


    @Scheduled(cron = "0 */1 * * * ?") //每分钟的整数倍执行一次
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

    /**
     * 关闭 order
     */
    private void closeOrder(){
        RedisShardedPoolUtil.expire(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,50); // 有效期50 秒
        log.info("获取{}，thredName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
        orderService.closeOrder(hour);
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("释放{}，thredName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        log.info("=================================");
    }

}
