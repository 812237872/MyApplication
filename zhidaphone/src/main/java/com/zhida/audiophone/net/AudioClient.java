package com.zhida.audiophone.net;

import android.util.Log;

import com.zhida.audiophone.net.handler.AudioMessageHandler;
import com.zhida.audiophone.net.listener.AudioMessageListener;
import com.zhida.audiophone.net.listener.CommandStateListener;
import com.zhida.audiophone.net.message.AudioClosedMessage;
import com.zhida.audiophone.net.message.AudioLoginSuccess;
import com.zhida.audiophone.net.message.AudioMessage;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.Charset;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;

/**
 * 媒体通道客户端
 */

public class AudioClient implements CommandStateListener,AudioMessageListener{

    private static final String TAG = "CommandClient";

    private EventLoopGroup group;//Bootstrap参数

    private Channel channel;//通过对象发送数据到服务端

    private boolean isConnect = false;//判断是否连接了
    private boolean isConnecting = false;//是否正在连接

    private boolean isNeedReconnect = false;//是否需要重连
    private static int reconnectNum = Integer.MAX_VALUE;//定义的重连到时候用
    private long reconnectIntervalTime = 5000;//重连的时间

    public String host;//ip
    public int tcp_port;//端口

    private String userName;

    /*
    构造 传入 ip和端口
     */
    public AudioClient(Builder builder) {
        host = builder.targetIp;
        tcp_port = builder.targetport;
        userName=builder.userName;
        connect();
    }

    /*
    连接方法
     */
    public void connect() {

        if (isConnecting) {
            return;
        }
        //起个线程
        Thread clientThread = new Thread("audio-client") {
            @Override
            public void run() {
                super.run();
                isNeedReconnect = false;
                reconnectNum = Integer.MAX_VALUE;
                connectServer();
            }
        };
        clientThread.start();
    }

    //连接时的具体参数设置
    private void connectServer() {
        synchronized (AudioClient.this) {
            ChannelFuture channelFuture = null;//连接管理对象
            if (!isConnect) {
                isConnecting = true;
                group = new NioEventLoopGroup();//设置的连接group
                Bootstrap bootstrap= new Bootstrap().group(group)//设置的一系列连接参数操作等
                        .option(ChannelOption.TCP_NODELAY, true)//屏蔽Nagle算法试图
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() { // 5
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                //ch.pipeline().addLast(new IdleStateHandler(10,10,20, TimeUnit.SECONDS));
                                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                                ch.pipeline().addLast(new MyStringEncoder(Charset.forName("utf-8")));
                                ch.pipeline().addLast(new MyStringDecoder(Charset.forName("utf-8")));
                                ch.pipeline().addLast(new AudioMessageHandler(AudioClient.this));
                            }
                        });

                try {
                    //连接监听
                    channelFuture = bootstrap.connect(host, tcp_port).addListener(new ChannelFutureListener() {

                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {
                                Log.d(AudioClient.class.getSimpleName(),"------------AudioClient连接成功");
                                isConnect = true;
                                channel = channelFuture.channel();
                                //连接成功后发送config命令登录
                                String command="config "+userName+"\r\n";
                                Log.d(AudioClient.class.getSimpleName(),"------------媒体通道发送的登录消息是："+command);
                                channel.writeAndFlush(command);
                                AudioClient.this.onServiceStatusConnected();
                            } else {
                                Log.d(AudioClient.class.getSimpleName(),"------------AudioClient连接失败");
                                isConnect = false;
                                AudioClient.this.onServiceStatusClosed();
                            }
                            isConnecting = false;
                        }
                    }).sync();

                    // 等待连接关闭
                    Log.d(AudioClient.class.getSimpleName(),"------------AudioClient等待连接关闭");
                    channelFuture.channel().closeFuture().sync();
                    Log.d(AudioClient.class.getSimpleName(),"------------AudioClient等待断开连接");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isConnect = false;
                    this.onServiceStatusClosed();
                    if (null != channelFuture) {
                        if (channelFuture.channel() != null && channelFuture.channel().isOpen()) {
                            channelFuture.channel().close();
                        }
                    }
                    group.shutdownGracefully();
                }
            }
        }
    }

    /**
     * 断开连接
     * */
    public void disconnect() {
        Log.d(AudioClient.class.getSimpleName(),"-----------disconnect");
        isNeedReconnect = false;
        group.shutdownGracefully();
    }


    /**
     * 通用的消息发送方法
     * */
    public boolean sendMsgToServer(String data) {
        boolean flag = channel != null && isConnect;
        if (flag) {
            channel.writeAndFlush(data);
        }
        return flag;
    }

    public boolean sendMsgToServer(byte[] data) {
        boolean flag = channel != null && isConnect;
        if (flag) {
            ByteBuf byteBuf=Unpooled.buffer();
            byteBuf.writeBytes(data);
            byteBuf.writeBytes("\r\n".getBytes());
            channel.writeAndFlush(byteBuf);
        }
        return flag;
    }


        @Override
    public void onServiceStatusClosed() {
        //媒体通道关闭
        Log.d(AudioClient.class.getSimpleName(),"------onServiceStatusConnected:媒体通道关闭了");
        EventBus.getDefault().post(new AudioClosedMessage());

    }

    @Override
    public void onServiceStatusConnected() {
        //媒体通道关闭
        Log.d(AudioClient.class.getSimpleName(),"------onServiceStatusConnected:媒体通道连接了");
    }

    /**
     * 用户登录成功
     * */
    @Override
    public void loginSuccess() {
        Log.d(AudioClient.class.getSimpleName(),"------loginSuccess:媒体通道登录成功");
        EventBus.getDefault().post(new AudioLoginSuccess());
    }

    /**
     * 接收到音频流信息
     * */
    @Override
    public void sendFramDate(AudioMessage frameDate) {

        EventBus.getDefault().post(frameDate);
    }

    //构造者模式
    public static final class Builder {
        private String targetIp;
        private String userName;
        private int targetport;

        public Builder() {
        }

        public Builder targetIp(String val) {
            targetIp = val;
            return this;
        }

        public Builder targetport(int val) {
            targetport = val;
            return this;
        }

        public Builder userName(String name) {
            userName = name;
            return this;
        }


        public AudioClient build() {
            return new AudioClient(this);
        }
    }
}
