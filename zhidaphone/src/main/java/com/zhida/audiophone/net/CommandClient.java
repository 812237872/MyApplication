package com.zhida.audiophone.net;

import android.os.SystemClock;
import android.util.Log;

import com.zhida.audiophone.net.handler.CommandMessageHandler;
import com.zhida.audiophone.net.listener.CommandMessageListener;
import com.zhida.audiophone.net.listener.CommandStateListener;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 信令通道客户端
 */

public class CommandClient {

    private static final String TAG = "CommandClient";

    private EventLoopGroup group;//Bootstrap参数

    private CommandStateListener listener;//连接状态回调监听对象
    private Channel channel;//通过对象发送数据到服务端
    private CommandMessageHandler commandMessageHandler;

    private boolean isConnect = false;//判断是否连接了

    private static int reconnectNum = Integer.MAX_VALUE;//定义的重连到时候用
    private boolean isNeedReconnect = true;//是否需要重连
    private boolean isConnecting = false;//是否正在连接
    private long reconnectIntervalTime = 5000;//重连的时间

    public String host;//ip
    public int tcp_port;//端口



    /*
    构造 传入 ip和端口
     */
    public CommandClient(String host, int tcp_port) {
        this.host = host;
        this.tcp_port = tcp_port;
    }

    /*
    连接方法
     */
    public void connect() {

        if (isConnecting) {
            return;
        }
        //起个线程
        Thread clientThread = new Thread("command-client") {
            @Override
            public void run() {
                super.run();
                isNeedReconnect = true;
                reconnectNum = Integer.MAX_VALUE;
                connectServer();
            }
        };
        clientThread.start();
    }

    //连接时的具体参数设置
    private void connectServer() {
        synchronized (CommandClient.this) {
            ChannelFuture channelFuture = null;//连接管理对象
            if (!isConnect) {
                isConnecting = true;
                commandMessageHandler=new CommandMessageHandler();
                group = new NioEventLoopGroup();//设置的连接group
                Bootstrap bootstrap= new Bootstrap().group(group)//设置的一系列连接参数操作等
                        .option(ChannelOption.TCP_NODELAY, true)//屏蔽Nagle算法试图
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() { // 5
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new IdleStateHandler(5,5,10, TimeUnit.SECONDS));
                                //ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                                ch.pipeline().addLast(new StringEncoder());
                                ch.pipeline().addLast(new StringDecoder());
                                ch.pipeline().addLast(commandMessageHandler);//需要的handlerAdapter
                            }
                        });

                try {
                    //连接监听
                    channelFuture = bootstrap.connect(host, tcp_port).addListener(new ChannelFutureListener() {

                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {
                                Log.d(CommandClient.class.getSimpleName(),"------------CommandClient连接成功");
                                isConnect = true;
                                channel = channelFuture.channel();
                                if (null!=listener)
                                {
                                    listener.onServiceStatusConnected();//这我自己定义的 接口标识
                                }
                            } else {
                                Log.d(CommandClient.class.getSimpleName(),"------------CommandClient连接失败");
                                isConnect = false;
                                if (null!=listener)
                                {
                                    listener.onServiceStatusClosed();
                                }
                            }
                            isConnecting = false;
                        }
                    }).sync();

                    // 等待连接关闭
                    Log.d(CommandClient.class.getSimpleName(),"------------CommandClient等待连接关闭");
                    channelFuture.channel().closeFuture().sync();
                    Log.d(CommandClient.class.getSimpleName(),"------------CommandClient等待断开连接");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isConnect = false;
                    if (null!=listener)
                        listener.onServiceStatusClosed();
                    if (null != channelFuture) {
                        if (channelFuture.channel() != null && channelFuture.channel().isOpen()) {
                            channelFuture.channel().close();
                        }
                    }
                    group.shutdownGracefully();
                    reconnect();//重新连接
                }
            }
        }
    }

    /**
     * 主动断开和信令服务器的连接
     * */
    public void disconnect() {
        System.out.println(CommandClient.class.getSimpleName()+"------------disconnect");
        isNeedReconnect = false;
        group.shutdownGracefully();
    }

    /**
     * 重新和信令服务器的连接
     * */
    public void reconnect() {
        Log.d(CommandClient.class.getSimpleName(),"------------reconnect");
        if (isNeedReconnect && reconnectNum > 0 && !isConnect) {
            reconnectNum--;
            SystemClock.sleep(reconnectIntervalTime);
            if (isNeedReconnect && reconnectNum > 0 && !isConnect) {
                Log.d(CommandClient.class.getSimpleName(),"------------重新连接");
                connectServer();
            }
        }
    }

    /**
     * 通用的消息发送方法
     * */
    public boolean sendMsgToServer(String data, ChannelFutureListener listener) {
        boolean flag = channel != null && isConnect;
        if (flag) {
            channel.writeAndFlush(data).addListener(listener);

        }
        return flag;
    }

    /**
     * 设置服务重连次数
     * */
    public void setReconnectNum(int reconnectNum) {
        this.reconnectNum = reconnectNum;
    }

    /**
     * 设置服务重连间隔
     * */
    public void setReconnectIntervalTime(long reconnectIntervalTime) {
        this.reconnectIntervalTime = reconnectIntervalTime;
    }

    /**
     * 获取现在连接状态
     * */
    public boolean getConnectStatus() {
        return isConnect;
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setConnectStatus(boolean status) {
        this.isConnect = status;
    }

    public void setListener(CommandStateListener listener) {
        this.listener = listener;
    }


}
