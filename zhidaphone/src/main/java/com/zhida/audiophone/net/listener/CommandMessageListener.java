package com.zhida.audiophone.net.listener;

import com.zhida.audiophone.net.Message;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by YYY on 2019/8/16.
 */

public interface CommandMessageListener {

    //来电回调
    void phoneMakeCall(Message msg);
    //应答回调
    void phoneAnswerCall(Message msg);
    //挂断回调
    void phoneCallEnd(Message msg);

    void sendHeatMessage(ChannelHandlerContext ctx);
}
