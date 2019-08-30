package com.example.socket;

/**
 * Created by Lenovo on 2019/8/29.
 */

public class UserBean {
    String ip;
    String name;
    String busLine;
    //线路方案
    String busScheme;
    boolean ischecked;

    public UserBean(String ip, String name, String busLine, String busScheme, boolean ischecked) {
        this.ip = ip;
        this.name = name;
        this.busLine = busLine;
        this.busScheme = busScheme;
        this.ischecked = ischecked;
    }

    public boolean ischecked() {
        return ischecked;
    }

    public void setIschecked(boolean ischecked) {
        this.ischecked = ischecked;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBusLine() {
        return busLine;
    }

    public void setBusLine(String busLine) {
        this.busLine = busLine;
    }

    public String getBusScheme() {
        return busScheme;
    }

    public void setBusScheme(String busScheme) {
        this.busScheme = busScheme;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "ip='" + ip + '\'' +
                ", name='" + name + '\'' +
                ", busLine='" + busLine + '\'' +
                ", busScheme='" + busScheme + '\'' +
                ", ischecked=" + ischecked +
                '}';
    }
}
