package com.example.lenovo.myapplication;

/**
 * Created by Lenovo on 2019/8/20.
 */

public class CBean {
    //线路名称
    String busLu;
    //车辆名称
    String busName;
    //班次
    String busBan;
    //方向
    String busDirection;
    //待发时间
    String busTime;
    //司机名称
    String busPeople;
    //方案名称
    String busScheme;
    //方案属性
    String busSchemesAttributes;
    //单程点
    String busOnePoint;

    public CBean(String busLu, String busName, String busBan, String busDirection, String busTime, String busPeople, String busScheme, String busSchemesAttributes, String busOnePoint) {
        this.busLu = busLu;
        this.busName = busName;
        this.busBan = busBan;
        this.busDirection = busDirection;
        this.busTime = busTime;
        this.busPeople = busPeople;
        this.busScheme = busScheme;
        this.busSchemesAttributes = busSchemesAttributes;
        this.busOnePoint = busOnePoint;
    }

    public CBean() {
    }



    public String getBusLu() {
        return busLu;
    }

    public void setBusLu(String busLu) {
        this.busLu = busLu;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public String getBusBan() {
        return busBan;
    }

    public void setBusBan(String busBan) {
        this.busBan = busBan;
    }

    public String getBusDirection() {
        return busDirection;
    }

    public void setBusDirection(String busDirection) {
        this.busDirection = busDirection;
    }

    public String getBusTime() {
        return busTime;
    }

    public void setBusTime(String busTime) {
        this.busTime = busTime;
    }

    public String getBusPeople() {
        return busPeople;
    }

    public void setBusPeople(String busPeople) {
        this.busPeople = busPeople;
    }

    public String getBusScheme() {
        return busScheme;
    }

    public void setBusScheme(String busScheme) {
        this.busScheme = busScheme;
    }

    public String getBusSchemesAttributes() {
        return busSchemesAttributes;
    }

    public void setBusSchemesAttributes(String busSchemesAttributes) {
        this.busSchemesAttributes = busSchemesAttributes;
    }

    public String getBusOnePoint() {
        return busOnePoint;
    }

    public void setBusOnePoint(String busOnePoint) {
        this.busOnePoint = busOnePoint;
    }


    @Override
    public String toString() {
        return "CBean{" +
                "busLu='" + busLu + '\'' +
                ", busName='" + busName + '\'' +
                ", busBan='" + busBan + '\'' +
                ", busDirection='" + busDirection + '\'' +
                ", busTime='" + busTime + '\'' +
                ", busPeople='" + busPeople + '\'' +
                ", busScheme='" + busScheme + '\'' +
                ", busSchemesAttributes='" + busSchemesAttributes + '\'' +
                ", busOnePoint='" + busOnePoint + '\'' +
                '}';
    }
}
