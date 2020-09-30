package com.example.hostinfo.conf;

import com.alibaba.fastjson.JSON;
import com.example.hostinfo.bean.Anchor;
import com.example.hostinfo.service.HostService;
import com.example.hostinfo.util.CalabashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class Task {
    public final static Integer default_threadNum = 1;
    public final static Long default_interval = 15*60*1000L;
    public final static int default_timeout = 5*60*1000;
    public final static int default_interval_time = 5*60*1000;

    public static Logger logger = LoggerFactory.getLogger(Task.class);

    @Autowired
    HostService hostService;

    public static Integer index;

    //客户端运行数
    public static Integer threadNum = null;

    //间隔 开始的时候 间隔时间为 15 分钟
    public static Long interval = null;

    //进程超时时间
    public static Integer timeout = null;

    private static ExecutorService fixedThreadPool = null;
    private static Thread thread = null;

    public boolean runServer (Integer threadNum,Long interval, Integer timeout) {

        this.threadNum = null == threadNum ? default_threadNum : threadNum;
        this.interval = null == interval ? default_interval : interval;
        this.timeout = null == timeout ? default_timeout : timeout;

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runClient();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return status();
    }

    public boolean runServer() {
        return runServer(null,null,null);
    }



    public static Map<String,String> typeMap = new HashMap<>();

    {
        typeMap.put("1","Huya");
        typeMap.put("2","Douyu");
        typeMap.put("3","YY");
        typeMap.put("4","Douyin");
        typeMap.put("5","Kuaishou");
        typeMap.put("6","Bilibili");
    }

    public static String getTypeByValue(String value) {
        if(StringUtils.isEmpty(value)){
            return null;
        }
        Set<String> list = typeMap.keySet();
        for (String key : list) {
            if(typeMap.get(key).equals(value)){
                return key;
            }
        }
        return null;
    }

    public List<Anchor> getList() {
        return hostService.getData_toDay(interval);
    }

    public void start() throws InterruptedException {
        logger.info("程序启动, 读取 Anchor 数据...");
        List<Anchor> list = this.getList();
        if(list.size() == 0){
            logger.info("当前时间:" + new Timestamp(System.currentTimeMillis()).toString() + " 没有获取数据, 程序将于两分钟后运行");
            Thread.sleep(1000*60*2);
            start();
        }

        logger.info("读取到数据总共有"+ list.size() +"条");
        fixedThreadPool = Executors.newFixedThreadPool(threadNum);
        logger.info("成功创建线程池, 线程池最大并发数: " + threadNum);
        logger.info("数据读取中...");
        for (int i = 0; i < list.size(); i++) {
            int finalI = i;
            Anchor anchor = list.get(finalI);
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    runInThread(anchor, finalI);
                    try {
                        Thread.sleep(default_interval_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        fixedThreadPool.shutdown();
        boolean flag = fixedThreadPool.awaitTermination(1000, TimeUnit.SECONDS);
        if(flag) {
            hostService.insert();
            start();
        }else{
            logger.error("程序异常结束");
        }
    }

    public static boolean runInThread(Anchor anchor, int index) {

        logger.debug("获取信息> 平台: " + typeMap.get(anchor.getPlantform())
                    + "; 用户更新时间: " + anchor.getUpdateTime()
                    + "; 房间号: " + anchor.getRoomId()
                    + "; 用户Id: " + anchor.getId()
                    + "; 第" + (index + 1)
                    + "条"
            );

        try {
            Long startTime = System.currentTimeMillis();
            CalabashUtil.getInfo(typeMap.get(anchor.getPlantform()),anchor.getRoomId(),timeout);
            Long endTime = System.currentTimeMillis();
            logger.info("第" + (index + 1) + "条, 用时:" + (endTime - startTime)/1000 + "秒");
        } catch (IOException e) {
            logger.error("第" + (index + 1) + "条数据执行失败,错误信息如下:");
            logger.error(e.getMessage());
        } catch (TimeoutException e) {
            logger.error("第" + (index + 1) + "条数据获取超时; 详细数据:" + JSON.toJSONString(anchor));
        }
        return true;
    }


    public boolean status() {
        if(null == thread){
            return false;
        }else {
            return thread.isAlive();
        }
    }

    public void runClient() throws Exception {
        //启动服务端
        logger.info("开启 Server(Calabash.Business.Daemon.Host)...");
        if(CalabashUtil.server()){
            //等待5秒,保证 Calabash.Business.Daemon.Host 能够启动
            Thread.sleep(1000*5);
            this.start();
        }else{
            logger.error("服务端开启失败");
        }
    }

    public void shutdown () {
        if(null != thread) {
            CalabashUtil.shutdown();
            // 暂不晓得如何立即关闭 线程池 ExecutorService
        }
    }

    public Map<String,Object> getPool() {
        Map<String,Object> resMap = new HashMap<>();
        resMap.put("FixedThreadPool", status());
        return resMap;
    }

}
