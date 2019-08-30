package com.zhida.audiophone.net.handler;


import android.util.Log;

import com.thf.logger.LogTool;
import com.zhida.audiophone.HexUtil;
import com.zhida.audiophone.net.listener.AudioMessageListener;
import com.zhida.audiophone.net.message.AudioMessage;

import java.util.Arrays;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;


/**
 * Created by YYY on 2019/8/16.
 */

public class AudioMessageHandler extends ChannelInboundHandlerAdapter {

    private AudioMessageListener messageListener;

    public AudioMessageHandler(AudioMessageListener messageListener)
    {
        this.messageListener=messageListener;
    }

    /**
     * 定时心跳消息发送
     * */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        Log.d(AudioMessageHandler.class.getSimpleName(),"------userEventTriggered");
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                //触发心跳消息
                //媒体通道无需发送心跳信息
                //ctx.channel().writeAndFlush("hb "+System.currentTimeMillis()+"\r\n");
            }
        }
    }

    /**
     * 连接成功，通道激活
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.d(AudioMessageHandler.class.getSimpleName(),"------channelActive");
        super.channelActive(ctx);
    }

    /**
     * 命令消息接收
     * */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Log.d(AudioMessageHandler.class.getSimpleName(),"------channelRead");
        /**
        if (msg instanceof String)
        {
            String str = (String) msg;
            Log.d(AudioMessageHandler.class.getSimpleName(),"收到的消息是:"+str);
            if (str.startsWith("config_res"))
            {
                //媒体通道登录成功返回消息
                messageListener.loginSuccess();
            }
        }else
        {
            String aaa= Util.bytesToHexString((byte[]) msg);
            LogTool.info("接收到数据",aaa);

            //媒体语音推流信息
            AudioMessage audioMessage=new AudioMessage();
            audioMessage.setFrame((byte[]) msg);
            messageListener.sendFramDate(audioMessage);
        }**/
        String str = (String) msg;
        Log.d(AudioMessageHandler.class.getSimpleName(),"收到的消息是:"+str);
        if (str.startsWith("config_res"))
        {
            //媒体通道登录成功返回消息
            messageListener.loginSuccess();
        }else
        {
            byte[] frameDate= HexUtil.hexToByteArr((String) msg);
            LogTool.info("编码接收数据", Arrays.toString(frameDate));
            AudioMessage audioMessage=new AudioMessage();
            audioMessage.setFrame(frameDate);
            messageListener.sendFramDate(audioMessage);
        }


    }


    /**
     * 连接断开
     * */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.d(AudioMessageHandler.class.getSimpleName(),"------channelInactive");
        super.channelInactive(ctx);
    }

    /***
     * 链路连接异常回调
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 当引发异常时关闭连接。
        Log.d(AudioMessageHandler.class.getSimpleName(),"------exceptionCaught");
        cause.printStackTrace();
        ctx.close();
    }


}
