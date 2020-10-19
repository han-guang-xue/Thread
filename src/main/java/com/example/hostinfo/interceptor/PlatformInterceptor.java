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
        Elements errorTip = document.getElementsByClass("error-tip");  //房间号不存在
        Elements preStartTime = document.getElementsByClass("host-prevStartTime"); //没有开播
        Elements tip = document.getElementsByClass("tip"); //违规

        if(preStartTime.size() > 0 || errorTip.size() > 0 || tip.size() >0){
            return false;
        }else{
            return true;
        }
    }

    public static void main(String[] args) {
//        System.out.println(getLiveBroadcastStatus_Huya1("1000"));
//        System.out.println(getLiveBroadcastStatus_Huya1("wjz520cx"));
        System.out.println(getLiveBroadcastStatus_Huya1("23466002")); //违规
        System.out.println(getLiveBroadcastStatus_Huya1("haddis"));
    }

    public static boolean getLiveBroadcastStatus_Huya1(String roomId){
        String url = "https://www.huya.com";
        String respones = NetworkUtil.sendGet(url, roomId);
        Document document = Jsoup.parse(respones);
        Elements errorTip = document.getElementsByClass("error-tip");  //房间号不存在
        Elements preStartTime = document.getElementsByClass("host-prevStartTime"); //没有开播
        Elements tip = document.getElementsByClass("tip"); //违规
        if(preStartTime.size() > 0 || errorTip.size() > 0 || tip.size() >0){
            return false;
        }else{
            return true;
        }
    }
}
