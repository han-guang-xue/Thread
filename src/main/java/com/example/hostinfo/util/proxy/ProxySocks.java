package com.example.hostinfo.util.proxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class ProxySocks {
	public final static Logger logger = LoggerFactory.getLogger(ProxySocks.class);

	public static String getTarURL(int num) {
		return "http://webapi.http.zhimacangku.com/getip?num="
				+(num == 0 ? 1: num)
				+ "&type=2&pro=&city=0&yys=0&port=1&time=1&ts=1&ys=0&cs=0&lb=1&sb=0&pb=4&mr=1&regions=";
	}

	public static void main(String[] args) throws IOException {
		List<IP> ips = getProxyIP(5);
		System.out.println(ips.size());
	}

	public static List<IP> getIP(int number) {
		try {
			return getProxyIP(number);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<IP> getProxyIP(int number) throws IOException {
		return getNewIP(number);
	}

	public static List<IP> getNewIP(int number) throws IOException {
		URL url = new URL(getTarURL(number));
		URLConnection conn = url.openConnection();
		conn.connect();
		InputStream in;
		try {
			in = conn.getInputStream();
		}catch (IOException e){
			return null;
		}
		return parse(IO2String(in));
	}

	/**
	 * 对 ip 解析成对象
	 * @param res
	 * @return
	 */
	public static List<IP> parse(String res) {
		JSONObject resJSON = JSONObject.parseObject(res);
		boolean status = resJSON.getBoolean("success");
		List<IP> resIPS = new ArrayList<>();
		if (status) {
			JSONArray ips = resJSON.getJSONArray("data");
			ips.forEach(item->{
				JSONObject _ip = JSONObject.parseObject(item.toString());
				resIPS.add(new IP(_ip.getString("ip"), _ip.getString("port"), _ip.getTimestamp("expire_time"), _ip.getString("outip")));
			});
		}
		return resIPS;
	}
	/**
	 * 将输入流转换成字符串
	 *
	 * @param inStream
	 * @return
	 * @throws IOException
	 */
	public static String IO2String(InputStream inStream) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = inStream.read(buffer)) != -1) {
			result.write(buffer, 0, len);
		}
		String str = result.toString(StandardCharsets.UTF_8.name());
		return str;
	}
}
