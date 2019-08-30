package com.zhida.audiophone.provider;

import com.zhida.audiophone.net.AudioClient;

/**
 * Created by YYY on 2019/8/18.
 */

public class EncodeProvider {

    private AudioClient audioClient; //媒体通道客户端
    private static EncodeProvider provider;

    public static EncodeProvider getProvider() {
        if (provider != null) {
            return provider;
        }
        return null;
    }

    //整合发送类和接收类。 此为构造方法
    public EncodeProvider(String targetIp, int targetPort,String userName) {
        // 1配置client的信息，目标ip和端口。
        audioClient = new AudioClient.
                Builder()
                .targetIp(targetIp)
                .targetport(targetPort)
                .userName(userName)
                .build();
        provider = this;
    }



    //发送音频数据
    public void sendAudioFrame(byte[] data) {
        //Log.d(EncodeProvider.class.getSimpleName(),"sendAudioFrame------发送编码数据");
        //Log.d(EncodeProvider.class.getSimpleName(),"sendAudioFrame------编码数据长度:"+data.length);
        //audioClient.sendMsgToServer(new String(data, Charset.forName("UTF-8"))+"\r\n");
        audioClient.sendMsgToServer(data);
    }

    //发送音频数据
    public void sendAudioFrameString(String data) {
        audioClient.sendMsgToServer(data+"\r\n");
    }

    //通过指定ip，发送文字指令
    public void login(String userName) {
        audioClient.sendMsgToServer("config "+userName+"\r\n");
    }

    public void shutDownSocket(){
        audioClient.disconnect();
    }
}
