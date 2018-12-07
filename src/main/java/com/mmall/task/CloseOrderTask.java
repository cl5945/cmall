package com.mmall.task;

import ch.qos.logback.classic.gaffer.PropertyUtil;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
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

    @Scheduled(cron = "0 */1 * * * ?") //每分钟的整数倍执行一次
    public void closeOrderTaskV1(){
        log.info("关闭订单，定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
        orderService.closeOrder(hour);
        log.info("关闭订单，定时任务完成");
    }

}
