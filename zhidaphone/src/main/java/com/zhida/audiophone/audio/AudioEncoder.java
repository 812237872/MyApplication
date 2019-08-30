package com.zhida.audiophone.audio;

import android.util.Log;

import com.gyz.voipdemo_speex.util.Speex;
import com.thf.logger.LogTool;
import com.zhida.audiophone.HexUtil;
import com.zhida.audiophone.provider.EncodeProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Describe: 监听收集到的音频流，并且进行编码，让后调用EncodeProvider 将数据发送出去
 */
public class AudioEncoder implements Runnable{
    String LOG = "AudioEncoder";
    //单例模式构造对象
    private static AudioEncoder encoder;
    //是否正在编码
    private boolean isEncoding = false;

    //每一帧的音频数据的集合
    private List<AudioData> dataList = null;


    public static AudioEncoder getInstance() {
        if (encoder == null) {
            encoder = new AudioEncoder();
        }
        return encoder;
    }

    private AudioEncoder( ) {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
    }

    //存放录音的数据 short字节数组
    public void addData(short[] data, int size) {
        AudioData rawData = new AudioData();
        rawData.setSize(size);
        short[] tempData = new short[size];
        System.arraycopy(data, 0, tempData, 0, size);
        rawData.setRealData(tempData);
        dataList.add(rawData);
    }


    /**
     * start encoding 开始编码
     */
    public void startEncoding() {

        Log.e("ccc", "编码子线程启动");
        if (isEncoding) {
            Log.e(LOG, "encoder has been started  !!!");
            return;
        }
        //开子线程
        new Thread(this).start();
    }

    /**
     * end encoding	停止编码
     */
    public void stopEncoding() {
        this.isEncoding = false;
    }

    @Override
    public void run() {
        int encodeSize = 0;
        byte[] encodedData;
        isEncoding = true;
        while (isEncoding) {
            if (dataList.size() == 0) { //如果没有编码数据则进行等待并且释放线程
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (isEncoding) {
                AudioData rawData = dataList.remove(0);
                LogTool.info("录音乐原始数据:", Arrays.toString(rawData.getRealData()));
                encodedData = new byte[Speex.getInstance().getFrameSize()];
                encodeSize = Speex.getInstance().encode(rawData.getRealData(),
                        0, encodedData, rawData.getSize());
                if (encodeSize > 0) {
                    //实现发送数据。
                    //方法一  发送的字节数据  后面有0填充
                    /**
                    LogTool.info("编码发送数据", Arrays.toString(encodedData));
                    String encodedDataString= HexUtil.byteArrToHex(encodedData);
                    **/


                    //方法二 通过ByteBuf，去掉编码后尾部填充的0
                    ByteBuf aaa= Unpooled.buffer();
                    aaa.writeBytes(encodedData,0,encodeSize);
                    byte[] aaa1=new byte[encodeSize];
                    aaa.readBytes(aaa1,0,encodeSize);
                    LogTool.info("编码发送字节数据", Arrays.toString(aaa1));
                    String encodedDataString= HexUtil.byteArrToHex(aaa1);
                    LogTool.info("编码发送十六字节数据",encodedDataString);
                    if (EncodeProvider.getProvider()!=null)
                        //EncodeProvider.getProvider().sendAudioFrame(encodedData); //发送录音数据
                        EncodeProvider.getProvider().sendAudioFrameString(encodedDataString); //发送录音数据
                }

            }
        }
    }
}
