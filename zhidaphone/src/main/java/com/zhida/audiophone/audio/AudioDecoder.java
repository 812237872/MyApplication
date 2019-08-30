package com.zhida.audiophone.audio;

import android.util.Log;

import com.gyz.voipdemo_speex.util.Speex;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Describe:  根据接收到的音频流，进行解码和播放。
 */
public class AudioDecoder implements Runnable {
    String LOG = "AudioDecoder";
    private static AudioDecoder decoder;

    private static final int MAX_BUFFER_SIZE = 2048;

    private short[] decodedData;
    private byte[] decodedData1;
    private boolean isDecoding = false;
    private List<AudioData> dataList = null;


    public static AudioDecoder getInstance() {
        if (decoder == null) {
            decoder = new AudioDecoder();
        }
        return decoder;
    }

    private AudioDecoder() {
        this.dataList = Collections
                .synchronizedList(new LinkedList<AudioData>());
        startDecoding();
    }


    public void addData(byte[] data, int size) {
        AudioData adata = new AudioData();
        adata.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        Log.d(AudioDecoder.class.getSimpleName(),"接受到的数据tempData长度3："+tempData.length);
        adata.setReceiverdata(tempData);
        dataList.add(adata);
    }

    /**
     * start decode AMR data
     */
    public void startDecoding() {
        System.out.println(LOG + "开始解码");
        if (isDecoding) {
            return;
        }
        new Thread(this).start();
    }

    @Override
    public void run() {

        //新建音频播放器
        AudioPlayer player = AudioPlayer.getInstance();

        player.startPlaying();
        this.isDecoding = true;
        Log.d(LOG, LOG + "初始化播放器");
        int decodeSize = 0;
        while (isDecoding) {
            if (dataList.size() > 0) {
                AudioData encodedData = dataList.remove(0);
                decodedData = new short[Speex.getInstance().getFrameSize()];
                byte[] data = encodedData.getReceiverdata(); //获取接收到的数据

                //接收到的数据去掉了尾部的0,所以现在要填充0，恢复原来的长度
                ByteBuf byteBuf= Unpooled.buffer(Speex.getInstance().getFrameSize());
                byteBuf.writeBytes(data,0,data.length);
                data=byteBuf.array();

                decodeSize = Speex.getInstance().decode(data, decodedData, data.length);
                if (decodeSize > 0) {
                    // 将数据添加到播放器
                    player.addData(decodedData, decodeSize);
                }


            }
        }
        System.out.println(LOG + "停止解码");
        // stop playback audio
        player.stopPlaying();
    }

    public void stopDecoding() {
        this.isDecoding = false;
    }
}
