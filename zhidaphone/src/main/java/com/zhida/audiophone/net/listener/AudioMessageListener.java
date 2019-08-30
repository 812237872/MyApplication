package com.zhida.audiophone.net.listener;

import com.zhida.audiophone.net.message.AudioMessage;

/**
 * Created by YYY on 2019/8/19.
 */

public interface AudioMessageListener {

    //媒体信道连接成功
    void loginSuccess();
    //接收到语音信息
    void sendFramDate(AudioMessage frameDate);
}
