package com.example.hostinfo.conf;

import com.example.hostinfo.service.HostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Time;


@Component
public class Timing{
    public static Logger  logger = LoggerFactory.getLogger(Time.class);


    @Autowired
    HostService hostService;

    @Autowired
    Task task;


    //每十分钟执行一次
    @Scheduled(cron = "0 */1 * * * *")
    public void start() {
        hostService.insert();
    }

//    //每两分钟执行一次, 监听服务器状态
//    @Scheduled(cron = "0 */2 * * * *")
//    public void listenServer() {
//        boolean flag = CalabashUtil.status();
//        logger.debug("监听服务状态 " + (flag ? "正常" : "异常关闭"));
//        if(!flag) {
//            logger.info("服务异常关闭,重启中...");
//            CalabashUtil.restart();
//        }
//    }
}
