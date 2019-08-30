package com.zhida.audiophone.net.handler;

import android.util.Log;

import com.zhida.audiophone.net.message.AskMessage;
import com.zhida.audiophone.net.message.CommandLoginMessage;
import com.zhida.audiophone.net.message.OpenResMessage;

import org.greenrobot.eventbus.EventBus;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;


/**
 * 命令消息处理
 */

public class CommandMessageHandler extends ChannelInboundHandlerAdapter {


    public CommandMessageHandler() {
    }

    /**
     * 定时心跳消息发送
     * */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // Log.d(CommandMessageHandler.class.getSimpleName(),"------userEventTriggered");
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                //触发心跳消息
                ctx.channel().writeAndFlush("hb "+System.currentTimeMillis()+"\r\n");
            }
        }
    }

    /**
     * 连接成功
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.d(CommandMessageHandler.class.getSimpleName(),"------channelActive");
        super.channelActive(ctx);
    }

    /**
     * 连接断开
     * */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.d(CommandMessageHandler.class.getSimpleName(),"------channelInactive");
        super.channelInactive(ctx);
    }

    /**
     * 命令消息接收
     * */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Log.d(CommandMessageHandler.class.getSimpleName(),"------channelRead");
        String str = (String) msg;
        Log.d(CommandMessageHandler.class.getSimpleName(),"收到的消息是:"+str);
        if (str.startsWith("login_res"))
        {
            //接收登录成功返回消息
            EventBus.getDefault().post(new CommandLoginMessage(str));
        }else if (str.startsWith("hp_res"))
        {
            //接收心跳返回消息
        }else if (str.startsWith("open_res"))
        {
            //接收请求应答
            String[] open_res=str.split("\\s+");
            if (open_res.length>1)
            {
                int tag=open_res.length;
                EventBus.getDefault().post(new OpenResMessage(open_res[tag-1]));
            }

        }else if (str.startsWith("ask"))
        {
            //接收平台转发呼叫
            String[] ask=str.split("\\s+");
            if (ask.length>1)
            {
                EventBus.getDefault().post(new AskMessage(ask[1]));
            }
        }else if (str.startsWith("shutdown"))
        {
            //接收通话中发送挂断命令

        }

    }

    /***
     * 链路连接异常回调
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 当引发异常时关闭连接。
        Log.d(CommandMessageHandler.class.getSimpleName(),"------exceptionCaught");
        cause.printStackTrace();
        ctx.close();
    }
}
