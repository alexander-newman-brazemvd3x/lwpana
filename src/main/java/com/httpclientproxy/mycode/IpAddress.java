package com.httpclientproxy.mycode;

/**
 * Created by Tianjinjin on 2016/11/2.
 */
public class IpAddress {
    String ip;
    int port;

    IpAddress(){

    }

    IpAddress(String ip, int port){
        this.ip=ip;
        this.port=port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


}
