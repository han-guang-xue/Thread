package com.example.hostinfo.conf;

import com.alibaba.fastjson.JSON;
import com.example.hostinfo.bean.Anchor;
import com.example.hostinfo.bean.Failure;
import com.example.hostinfo.service.HostService;
import com.example.hostinfo.util.CalabashUtil;
import com.example.hostinfo.util.proxy.IP;
import com.example.hostinfo.util.proxy.ProxySocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class Task {
    public final static Boolean useAgent = true;  //是否使用代理
    public final static String default_proxy_IP = "119.7.231.157:4261"; //默认一个
    public final static Integer default_proxy_num = 1; //使用多少个代理跑, 每个代理代表一个进程
    public final static Integer default_threadNum = 1; //每个代理ip下执行的线程
    public final static Long default_interval = 15*60*1000L; //获取多久时间之前的数据
    public final static int default_timeout = 3*60*1000; //超时时间
    public final static int default_interval_time = 0*60*1000; //每个ip下线程间执行的时间间隔
    public static volatile Map<Integer,IP> process_ProxyIP = null; //存储每个进程的代理IP
    public static volatile Map<Integer, Failure> process_ProxyIP_Status = null;
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

        List<List<Anchor>> halve = getHalve(list,default_proxy_num);
        int actual_proxy_num = halve.size();
        ExecutorService halvefool = Executors.newFixedThreadPool(actual_proxy_num);
        logger.info("成功创建代理进程" + actual_proxy_num + ";");

        //设置代理IP
        setProxyNum(actual_proxy_num);

        for (int i = 0; i < halve.size(); i++) {
            int finalI = i;
            halvefool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        start(halve.get(finalI),finalI);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
        halvefool.shutdown();
        boolean flag = halvefool.awaitTermination(1000, TimeUnit.SECONDS);
        if(flag) {
            logger.info("代理线程结束, 重新运行");
            this.start();
        }else{
            logger.error("代理线程异常结束");
        }
    }



    public boolean start(List<Anchor> anchors,Integer proxyID) throws InterruptedException {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadNum);
        logger.info("成功创建线程池, 线程池最大并发数: " + threadNum);
        for (int i = 0; i < anchors.size(); i++) {
            int finalI = i;
            Anchor anchor = anchors.get(finalI);
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    //判断代理IP 是否失效, 如果失效,则重新获取IP并替换
                    IP ip = getIP(proxyID);
                    if(null == ip) {
                        logger.error("proxyID-"+ proxyID + "-获取代理IP失败");
                    }
                    runInThread(anchor, finalI, ip.getCurIP(), proxyID);
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
            return true;
        }else{
            logger.error("程序异常结束");
            return false;
        }
    }

    public static boolean runInThread(Anchor anchor, int index, String ip, Integer proxyID) {
        logger.debug("获取信息> 平台: " + typeMap.get(anchor.getPlantform())
                + "; 代理进程: " + (proxyID + 1)
                + "; 代理IP: " + ip
                + "; 用户更新时间: " + anchor.getUpdateTime()
                + "; 房间号: " + anchor.getRoomId()
                + "; 用户Id: " + anchor.getId()
                + "; 第" + (index + 1)
                + "条"
            );

        String logProx = "代理IP: " + ip;
        try {
            Long startTime = System.currentTimeMillis();
            CalabashUtil.getInfo(typeMap.get(anchor.getPlantform()), anchor.getRoomId(), timeout, ip);
            Long endTime = System.currentTimeMillis();
            logger.info("代理进程: "+ proxyID + ",第" + (index + 1) + "条,successed,用时:" + (endTime - startTime)/1000 + "秒," + logProx);
        } catch (IOException e) {
            logger.error("代理进程: "+ proxyID + ",第" + (index + 1) + "条,failure--,错误信息如下:");
            logger.error(e.getMessage());
        } catch (TimeoutException e) {
            logger.error("代理进程: "+ proxyID + ",第" + (index + 1) + "条,failure--," + logProx + "; 详细数据:" + JSON.toJSONString(anchor) + ";");
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

//    public void runClient() throws Exception {
//        //启动服务端
//        logger.info("开启 Server(Calabash.Business.Daemon.Host)...");
//        if(CalabashUtil.server()){
//            //等待5秒,保证 Calabash.Business.Daemon.Host 能够启动
//            Thread.sleep(1000*5);
//            this.start();
//        }else{
//            logger.error("服务端开启失败");
//        }
//    }

    /**
     * 只启动客户端
     * @throws Exception
     */
    public void runClient() throws Exception {
        this.start();
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
    private List<List<Anchor>> getHalve(List<Anchor> list, Integer num) {
        List<List<Anchor>> havle = new ArrayList<>();
        if(list.size() == 0){
            return null;
        }
        if(list.size() < num) {
            havle.add(list);
            return havle;
        }
        Integer sum = list.size();
        List<Anchor> tmp = new ArrayList<>();
        int havle_num = (int) Math.ceil(sum/(double)num);
        for (int i = 0; i < sum; i++) {
            tmp.add(list.get(i));
            if(((i+1)%havle_num == 0) || (i + 1 == sum)) {
                havle.add(tmp);
                tmp = new ArrayList<>();
            }
        }
        return havle;
    }

    public IP getIP(Integer proxy_ID) {
        IP ip = process_ProxyIP.get(proxy_ID);
        if(ip.getExpireTime().getTime() < System.currentTimeMillis()) {
            List<IP> ips = getIPS(1);
            String logStr = "系统重新获取; ";
            if(ips.size() != 0) {
                ip = ips.get(0);
                logStr += JSON.toJSONString(ip);
                process_ProxyIP.put(proxy_ID, ip);
            }else {
                logStr += ", 获取失败";
            }
            logger.info("代理进程: " + proxy_ID + "; 代理IP过时," + logStr);
        }
        return ip;
    }

    public void setProxyNum(int num) {
        List<IP> ips = getIPS(num);
        if(ips.size() == 0) {
            logger.error("代理IP获取失败, process_ProxyIP 设置失败");
            System.exit(0);
        }
        logger.info("代理IP获取成功 " + JSON.toJSONString(ips));
        process_ProxyIP = new HashMap<>();
        for (int i = 0; i < num; i++) {
            process_ProxyIP.put(i,ips.get(i));
        }
    }

    public List<IP> getIPS(int num){
        try {
            return getIPS(num, 3);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<IP> getIPS(int num, int timeoutTimes) throws InterruptedException {
        List<IP> ips = ProxySocks.getIP(num);
        if(ips.size() == 0 && timeoutTimes > 0) {
            Thread.sleep(30*1000);
            return getIPS(num,timeoutTimes - 1);
        }else {
            return ips;
        }
    }

}
