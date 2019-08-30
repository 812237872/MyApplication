package com.zhida.audiophone.net;

import android.util.Log;

import com.zhida.audiophone.net.listener.CommandStateListener;
import com.zhida.audiophone.net.message.OpenResMessage;

import io.netty.channel.ChannelFutureListener;

/**
 * Created by YYY on 2019/8/16.
 */

public class PhoneCall implements CommandStateListener {


    public static PhoneCall single;
    private CommandClient commandClient;//信命通道客户端

    //静态工厂方法
    public static PhoneCall getInstance() {
        if (single == null) {  //第一次检查，避免不必要的同步
            synchronized (PhoneCall.class) {  //同步
                if (single == null) {   //第二次检查，为null时才创建实例
                    single = new PhoneCall();
                }
            }
        }
        return single;
    }

    public void init(String ip,int port) {
        commandClient = new CommandClient(ip, port);
        commandClient.setListener(this);
        commandClient.connect();
    }

    public void disconnect() {
        if (null != commandClient)
            commandClient.disconnect();
    }


    //信道连上了
    @Override
    public void onServiceStatusClosed() {
        Log.d(PhoneCall.class.getSimpleName(), "断开了断开了");
    }

    //信道断开了
    @Override
    public void onServiceStatusConnected() {
        Log.d(PhoneCall.class.getSimpleName(), "连接上了连接上了");
    }

    //信道登录
    public void loginIn(String userName, ChannelFutureListener listener) {
        commandClient.sendMsgToServer("login " + userName+"\r\n", listener);
    }

    //主动呼叫
    public void call(String callName, ChannelFutureListener listener) {
        String command="open "+callName+"\r\n";
        commandClient.sendMsgToServer(command, listener);
    }

    /**
     * 应答
     * cmdOpt 5:响应 6:拒绝 7:媒体连接失败
     * */
    public void answer(String cmdOpt,ChannelFutureListener listener)
    {
        String command="ask_res "+cmdOpt+"\r\n";
        Log.d(PhoneCall.class.getSimpleName(),"answer:"+command);
        commandClient.sendMsgToServer(command,listener);
    }

    /**
     * 挂断通话
     * */
    public void closePhone(ChannelFutureListener listener)
    {
        String command="close\r\n";
        commandClient.sendMsgToServer(command,listener);
    }

    /**
     * 接听电话
     * */
    public void ringPickup(ChannelFutureListener listener)
    {
        String command="ask_res "+ OpenResMessage.YP_CMD_OPT_CONSENT+"\r\n";
        commandClient.sendMsgToServer(command,listener);
    }
}
