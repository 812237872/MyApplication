package com.zhida.audiophone.net.message;

import java.io.Serializable;

/**
 * Created by YYY on 2019/8/26.
 */

public class CommandLoginMessage implements Serializable{

    private String message;

    public CommandLoginMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
