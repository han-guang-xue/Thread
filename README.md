#### 需求,使用java命令 执行 xxx.exe 脚本,获取各个直播平台的主播的相关信息

为了提高效率, 需要多线程执行该脚本

要点: 
1. Runtime.getRuntime().exec(cmd); 这个方法最终调用的是 ProcessBuilder 的方法执行的命令, 所以 ProcessBuilder 比 Process 提供了更多的方法用于对进程的控制
2. cmd 命令执行 .exe 如何传递参数
3. 执行.exe 文件, 但是cmd 窗口隐藏,不显示
4. 如何监听cmd命令执行 .exe 文件结束
5. 使用线程池执行脚本,提高效率
6. *_ 多线程执行的时候, 如何设置超时结束 和 超时时间内正常运行结束的方法


> 以下是测试代码
>
```java
package com.example.hostinfo.conf;

import com.example.hostinfo.bean.Anchor;
import com.example.hostinfo.service.HostService;
import com.example.hostinfo.util.CalabashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
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

@Component
public class Task implements CommandLineRunner {

    public static Logger logger = LoggerFactory.getLogger(Task.class);

    @Autowired
    HostService hostService;

    public static Integer index;

    //客户端运行数
    public static Integer threadNum = 1;

    //一批进程执行的时候 停顿时间 三分钟
    public static Long threadIntervalTime = 1000*60*4L;

    //间隔 开始的时候 间隔时间为 14 分钟
    public static Long interval = 3*60*60*1000L;

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
            Thread.sleep(1000*60*15);
            logger.info("当前时间:" + new Timestamp(System.currentTimeMillis()).toString() + " 没有获取数据, 程序将于十五分钟后运行");
            start();
        }

        logger.info("读取到数据总共有"+ list.size() +"条");
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadNum);
        logger.info("成功创建线程池, 线程池并发数: " + threadNum);
        for (int i = 0; i < list.size(); i++) {
            int finalI = i;
            Anchor anchor = list.get(finalI);
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runInThread(anchor, finalI);
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

        logger.info("平台: " + typeMap.get(anchor.getPlantform())
                    + "; 用户更新时间: " + anchor.getUpdateTime()
                    + "; 房间号: " + anchor.getRoomId()
                    + "; 用户Id: " + anchor.getId()
                    + "; 第" + (index + 1)
                    + "条"
            );

        try {
            Long startTime = System.currentTimeMillis();
            CalabashUtil.getInfo(typeMap.get(anchor.getPlantform()),anchor.getRoomId());
            Long endTime = System.currentTimeMillis();
            logger.info("第" + (index + 1) + "条, 用时:" + (endTime - startTime)/1000 + "秒");
        } catch (IOException e) {
            logger.error("第" + (index + 1) + "条数据执行失败");
        }
        return true;
    }

    @Override
    public void run(String... args) throws Exception {
        //启动服务端
        logger.info("开启 Server(Calabash.Business.Daemon.Host)...");
        if(CalabashUtil.server()){
            //等待20秒,保证 Calabash.Business.Daemon.Host 能够启动
            Thread.sleep(1000*5);
            this.start();
        }else{
            logger.error("服务端开启失败");
        }
    }
}
```


> 进程 超时时间,正常结束 方法实现
```java
package com.example.hostinfo.util;

/**
 * 进程超时时间和正常时间结束运行
 */
public class ProcessWithTimeout extends Thread {
    private Process process;
    private int exitCode = Integer.MIN_VALUE;

    public ProcessWithTimeout(Process process) {
        this.process = process;
    }

    public int waitForProcess(int milliseconds) {
        this.start();

        try {
            this.join(milliseconds);
        } catch (InterruptedException e) {
            this.interrupt();
            Thread.currentThread().interrupt();
        } finally {
            process.destroy();
        }
        return exitCode;
    }

    @Override
    public void run() {
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException ignore) {
            // Do nothing
        } catch (Exception ex) {
            // Unexpected exception
        }
    }
}
```