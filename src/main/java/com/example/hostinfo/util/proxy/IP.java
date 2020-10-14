package com.example.hostinfo.util.proxy;

import java.sql.Timestamp;

public class IP {
    private String IP;
    private String Port;
    private Timestamp ExpireTime;
    private String OutIp;

    /**
     * ip + port
     * @return
     */
    public String getCurIP() {
        return IP + ":" + Port;
    }

    public IP() {
    }

    public IP(String IP, String port, Timestamp expireTime, String outIp) {
        this.IP = IP;
        Port = port;
        ExpireTime = expireTime;
        OutIp = outIp;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getPort() {
        return Port;
    }

    public void setPort(String port) {
        Port = port;
    }

    public Timestamp getExpireTime() {
        return ExpireTime;
    }

    public void setExpireTime(Timestamp expireTime) {
        ExpireTime = expireTime;
    }

    public String getOutIp() {
        return OutIp;
    }

    public void setOutIp(String outIp) {
        OutIp = outIp;
    }
}
