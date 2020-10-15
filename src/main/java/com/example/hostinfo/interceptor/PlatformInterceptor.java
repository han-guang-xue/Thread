package com.example.hostinfo.interceptor;

import com.example.hostinfo.util.exception.NotLiveException;
import com.example.hostinfo.util.proxy.NetworkUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 针对不同的平台拦截
 */
public class PlatformInterceptor {
    public static final Logger logger = LoggerFactory.getLogger(PlatformInterceptor.class);

    public static boolean getLiveStatus(String platform, String roomId){
        if(!getLiveStatusByDiffPlatform(platform,roomId)) {
            throw new NotLiveException("主播未直播");
        }
        return true;
    }

    public static boolean getLiveStatusByDiffPlatform(String platform, String roomId) {
        switch (platform) {
            case "Huya": return getLiveBroadcastStatus_Huya(roomId);
            case "Douyu": return true;
            case "YY": return true;
            case "Douyin": return true;
            case "Kuaishou": return true;
            case "Bilibili": return true;
        }
        return false;
    }

    public static boolean getLiveBroadcastStatus_Huya(String roomId){
        String url = "https://www.huya.com";
        String respones = NetworkUtil.sendGet(url, roomId);
        Document document = Jsoup.parse(respones);
        Elements preStartTime = document.getElementsByClass("host-prevStartTime");

        if(preStartTime.size() > 0){
            return false;
        }else{
            return true;
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println(getLiveStatus("Huya","21271670"));
//            System.out.println(getLiveBroadcastStatus_Huya("22125566"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
