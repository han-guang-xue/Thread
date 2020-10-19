package com.example.hostinfo.util.script;


import com.example.hostinfo.conf.Task;
import com.example.hostinfo.util.ProcessWithTimeout;
import com.example.hostinfo.util.exception.ConnectionFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ScriptClient {
    public static Logger logger = LoggerFactory.getLogger(ScriptClient.class);

    public static String path = "tools\\Client\\Debug\\";

    private static String program = "Calabash.Plugin.ConsoleApp.exe";

    public static String server = "tools\\Server\\Debug\\Calabash.Business.Daemon.Host.exe";


    public static boolean getInfo(String platform, String roomNum, int timeout) throws IOException, TimeoutException, ConnectionFailureException {
        return getInfo(platform,roomNum,timeout,null);
    }
    /**
     * 平台
     * @param platform
     * @param roomNum
     * @param timeout
     * @param ip 代理 ip
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    public static boolean getInfo(String platform, String roomNum, int timeout, String ip) throws IOException, TimeoutException, ConnectionFailureException {
        List<String> list = new ArrayList<String>();
        list.add(path + program);
        list.add(platform);
        roomNum = null != ip ? roomNum + "!" + ip : roomNum;
        list.add(roomNum);

        ProcessBuilder pBuilder = new ProcessBuilder(list);

        //类似 cd path
        pBuilder.directory(new File(path));

        //启动程序
        Process process = pBuilder.start();

        //将标准输出 和 错误输出合并输出
        pBuilder.redirectErrorStream(true);
        ProcessWithTimeout processWithTimeout = new ProcessWithTimeout(process);

        Integer exitCode = processWithTimeout.waitForProcess(timeout);

        if(exitCode == Integer.MIN_VALUE){
            throw new TimeoutException("程序超时退出");
        }else{
            String resFile = path + "result\\"+ platform + "_" + roomNum.split("!")[0] + ".txt";
            if(new File(resFile).exists()){
                return true;
            }
            throw new ConnectionFailureException();
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
//        Thread.sleep(1000*5);
//        getInfo("Douyu", "5720533", Task.timeout);
//        getInfo2("Douyu", "7223839");
//        getInfo("Douyu", "1011");
//        server();
//        threadPool();
    }

    public static boolean getInfo2(String platform, String roomNum) throws IOException, InterruptedException, TimeoutException {
        List<String> list = new ArrayList<String>();
        list.add(path + program);

        //参数 platform
        list.add(platform);
        //参数 roomNum
        list.add(roomNum);

        ProcessBuilder pBuilder = new ProcessBuilder(list);

        //类似 cd path
        pBuilder.directory(new File(path));

        //启动程序
        Process process = pBuilder.start();

        //将标准输出 和 错误输出合并输出
        pBuilder.redirectErrorStream(true);

        ProcessWithTimeout processWithTimeout = new ProcessWithTimeout(process);

        Integer exitCode = processWithTimeout.waitForProcess(1000*5); //一分钟

        if(exitCode == Integer.MIN_VALUE) {
            throw new TimeoutException("程序超时退出");
        }else if(exitCode == -1){
            logger.info("程序结束");
            return true;
        }

        return true;
    }

    /**
     * 通过线程池执行多个 .exe;
     */
    public static void threadPool() throws InterruptedException {
        /**
         * Java通过Executors提供四种线程池，分别为：
         * newCachedThreadPool创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。
         * newFixedThreadPool 创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待。
         * newScheduledThreadPool 创建一个定长线程池，支持定时及周期性任务执行。
         * newSingleThreadExecutor 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。
         */
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        try {
                            getInfo("Douyu", "5720533", Task.timeout);
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                            logger.error("进程超时");
                        } catch (ConnectionFailureException e) {
                            e.printStackTrace();
                        }
                        System.out.println("线程执行完毕 + " + (finalI + 1));
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("执行失败");
                    }
                }
            });
        }

        System.out.println("该代码在for执行完之后接着执行, 不受 fixedThreadPool 影响");

        fixedThreadPool.shutdown();
        //timeout 检测线程池是否关闭时间间隔, unit 超时时间的单位
        boolean flag = fixedThreadPool.awaitTermination(1000, TimeUnit.SECONDS);

        if(flag) {
            System.out.println("程序执行完毕");
            threadPool();
        } else {
            System.out.println("程序异常结束");
        }
    }

    public static void getInfo1(String platform, String roomNum) throws IOException {
        Runtime.getRuntime().exec("cmd /c start /b " + path + program + " " + platform + " " + roomNum);

        /**
         *  这个是 .exec() 方法的源码, 最终调用的是 ProcessBuilder 的方法
         *
         *        public Process exec(String[] cmdarray, String[] envp, File dir)
         *         throws IOException {
         *             return new ProcessBuilder(cmdarray)
         *                     .environment(envp)
         *                     .directory(dir)
         *                     .start();
         *         }
         */
    }
}
