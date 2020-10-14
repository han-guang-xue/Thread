package com.example.hostinfo.util;

import com.example.hostinfo.bean.HostInfo;
import com.example.hostinfo.conf.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    public static String resultsPath =  CalabashUtil.path + "result";
    public static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static BufferedReader reader;

    public static File []files = null;


    public static List<HostInfo> getRes() throws IOException {

        File file = new File(resultsPath);
        List<HostInfo> anchors = new ArrayList<>();

        if(file.isDirectory()){
            files = file.listFiles();
            if(files.length == 0) {
                return null;
            }
            for (int i = 0; i < files.length; i++) {
               reader = new BufferedReader(new InputStreamReader(new FileInputStream(files[i].getPath())));
               HostInfo hostInfo = readFile(reader);
               if(null != hostInfo){
                   anchors.add(hostInfo);
               }
            }
            //读取完数据之后, 将文件删除掉
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }
        return anchors;
    }

    /**
     * 文件格式
     * Douyu,2445443
     * 关注:216,热度:20347,贵宾数:3
     */
    private static HostInfo readFile(BufferedReader reader) throws IOException {
        HostInfo hostInfo = new HostInfo();
        try{
            String []line1 = reader.readLine().split(",");
            String []line2 = reader.readLine().split(",");
            try{
                hostInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
                hostInfo.setPlatform(Integer.parseInt(Task.getTypeByValue(line1[0].trim())));
                hostInfo.setRoomId(line1[1].trim());
                hostInfo.setFocusNum(Integer.parseInt(line2[0].split(":")[1]));
                hostInfo.setHotNum(Integer.parseInt(line2[1].split(":")[1]));
                hostInfo.setVipNum(Integer.parseInt(line2[2].split(":")[1]));
            }catch (Exception e){
                logger.error("转化失败 line1: " + line1 + "; line2: " + line2);
                logger.error(e.getMessage());
                return null;
            }
        }catch (Exception e) {
            return null;
        }
        reader.close();
        return hostInfo;
    }

    public static void main(String[] args) throws IOException {
        getRes();
    }
}
