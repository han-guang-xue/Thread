package com.example.hostinfo.listener;

import com.example.hostinfo.conf.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@Component
public class TomcatListener implements ServletContextListener {
    @Autowired
    Task task;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        task.runServer();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("服务结束");
    }

}
