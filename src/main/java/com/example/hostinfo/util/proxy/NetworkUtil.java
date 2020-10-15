package com.example.hostinfo.util.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class NetworkUtil {

    private static final Logger logger = LoggerFactory.getLogger(NetworkUtil.class);

    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url   发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "/" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            //连接超时时间设置
            connection.setConnectTimeout(60*1000);
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
//            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.out.println(key + "--->" + map.get(key));
//            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * POST请求
     *
     * @param requestUrl 请求地址
     * @param param      请求数据
     * @return
     */
    public static String sendPost(String requestUrl, String param) {

        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;

        try {
            // 创建远程url连接对象
            URL url = new URL(requestUrl);

            // 通过远程url对象打开一个连接，强制转换为HttpUrlConnection类型
            connection = (HttpURLConnection) url.openConnection();

            // 设置连接方式：POST
            connection.setRequestMethod("POST");
            // 设置连接主机服务器超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(60000);

            // 设置是否向httpUrlConnection输出，设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个
            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);

            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 通过连接对象获取一个输出流
            os = connection.getOutputStream();
            // 通过输出流对象将参数写出去/传输出去，它是通过字节数组写出的
            // 若使用os.print(param);则需要释放缓存：os.flush();即使用字符流输出需要释放缓存，字节流则不需要
            if (param != null && param.length() > 0) {
                os.write(param.getBytes());
            }

            // 请求成功：返回码为200 */
            if (connection.getResponseCode() == 200) {
                // 通过连接对象获取一个输入流，向远程读取
                is = connection.getInputStream();
                // 封装输入流is，并指定字符集
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                // 存放数据 */
                StringBuffer sbf = new StringBuffer();
                String line = null;
                while ((line = br.readLine()) != null) {
                    sbf.append(line);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源 */
            try {

                if (null != br) {
                    br.close();
                }

                if (null != is) {
                    is.close();
                }

                if (null != os) {
                    os.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // 关闭远程连接
            // 断开连接，最好写上，disconnect是在底层tcp socket链接空闲时才切断。如果正在被其他线程使用就不切断。
            // 固定多线程的话，如果不disconnect，链接会增多，直到收发不出信息。写上disconnect后正常一些
            connection.disconnect();

//            System.out.println("--------->>> POST request end <<<----------");
        }

        return result;
    }


}
