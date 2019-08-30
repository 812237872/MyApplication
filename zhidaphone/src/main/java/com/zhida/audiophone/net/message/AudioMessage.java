package com.zhida.audiophone.net.message;

import java.io.Serializable;

/**
 * 流媒体消息的封装
 */

public class AudioMessage implements Serializable{

    private String date;
    private byte[] frame;

    public byte[] getFrame() {
        return frame;
    }

    public void setFrame(byte[] frame) {
        this.frame = frame;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
