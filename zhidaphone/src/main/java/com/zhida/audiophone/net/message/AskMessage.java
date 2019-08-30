package com.zhida.audiophone.net.message;

import java.io.Serializable;

/**
 * Created by YYY on 2019/8/19.
 * 拨号通知消息
 */

public class AskMessage implements Serializable{

    private String userName;

    public AskMessage(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
