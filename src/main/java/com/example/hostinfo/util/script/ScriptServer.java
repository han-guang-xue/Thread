package com.example.hostinfo.util.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScriptServer {
    public static Logger logger = LoggerFactory.getLogger(ScriptServer.class);
    private static String server = ScriptClient.server;
    private static Process serverProcess = null;


    /**
     * 启动服务
     * @return
     * @throws IOException
     */
    public static boolean start() throws IOException {
        try {
            List<String> list = new ArrayList<String>();
            list.add("cmd");
            list.add(server);
            serverProcess =  new ProcessBuilder(list).start();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean restart() {
        for (int i = 0; i < 3; i++) {
            boolean flag = status();
            if(flag) {
                logger.warn("服务器已重启");
                return true;
            }else{
                if( i != 0) {
                    logger.error("服务重启失败, 尝试第 "+ ("一,二,三".split(",")[i]) +" 次启动...");
                }
                try {
                    start();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
                if(i == 2) {
                    if(!status()) {
                        logger.info("服务异常,重启失败");
                    }else {
                        logger.info("服务重启成功");
                    }
                }
            }
        }
        return true;
    }

    public static void shutdown() {
        if(null != serverProcess){
            serverProcess.destroy();
            serverProcess = null;
        }
    }

    public static boolean status() {
        serverProcess.hashCode();
        return serverProcess.isAlive();
    }
}
