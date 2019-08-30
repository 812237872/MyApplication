package com.zhida.audiophone.net;

import java.io.Serializable;

/**
 * Created by YYY on 2019/8/16.
 */

public class Message implements Serializable{

    public static final String PHONE_HEAT_CALL="PHONE_HEAT_CALL";
    public static final String PHONE_MAKE_CALL = "PHONE_MAKE_CALL"; //拨打电话
    public static final String PHONE_ANSWER_CALL = "PHONE_ANSWER_CALL"; //接听电话
    public static final String PHONE_IS_BUSY = "PHONE_IS_BUSY"; //通话中
    public static final String PHONE_CALL_END = "PHONE_CALL_END"; //通话结束
    public static final String PHONE_CALL_END_OK = "PHONE_CALL_END_OK"; //通话结束成功


    private String type;//消息类型
    private String msgIp;//对象Ip
    private long timestamp;//时间戳


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMsgIp() {
        return msgIp;
    }

    public void setMsgIp(String msgIp) {
        this.msgIp = msgIp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
