package com.zhida.audiophone.net.message;

import java.io.Serializable;

/**
 * Created by YYY on 2019/8/19.
 * 拨号信道应答消息
 */

public class OpenResMessage implements Serializable{

    public static final String YP_CMD_OPT_RECV="0";//收到
    public static final String YP_CMD_OPT_NOUSER="1";//对方不存在
    public static final String YP_CMD_OPT_OFFLINE="2";//对方不在线
    public static final String YP_CMD_OPT_BUSY="3";//对方忙
    public static final String YP_CMD_OPT_ASK="4";//已经通知对方
    public static final String YP_CMD_OPT_CONSENT="5";// 同意
    public static final String YP_CMD_OPT_REFUSE="6";//拒绝
    public static final String YP_CMD_OPT_SOCKETERROR="7";//socket 失败; 媒体链接建立失败
    public static final String YP_CMD_OPT_TIMEOUT="8";//超时
    public static final String YP_CMD_OPT_PHONE="9";//通话中


    public OpenResMessage(String YP_CMD_OPT) {
        this.YP_CMD_OPT = YP_CMD_OPT;
    }

    private String YP_CMD_OPT;//呼叫返回类型

    public String getYP_CMD_OPT() {
        return YP_CMD_OPT;
    }

    public void setYP_CMD_OPT(String YP_CMD_OPT) {
        this.YP_CMD_OPT = YP_CMD_OPT;
    }
}
