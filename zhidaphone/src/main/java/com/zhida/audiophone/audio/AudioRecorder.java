package com.zhida.audiophone.audio;


import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.util.Log;

import com.gyz.voipdemo_speex.util.Speex;

/**
 * Describe: 进行音频录音 这个是音频录制的入口文件
 */
public class AudioRecorder implements Runnable {
    String LOG = "Recorder";

    //是否正在录制
    private boolean isRecording = false;
    //音频录制对象
    private AudioRecord audioRecord;

    private int audioBufSize = 0;
    //回声消除
    private AcousticEchoCanceler canceler;


    //开始录音的逻辑
    public void startRecording() {
        Log.e("ccc", "开启录音");
        //计算缓存大小 参数的意思分别是采样率 声道数 采样精度(一个采样点16比特，相当于2个字节)
        audioBufSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLERATE,
                AudioConfig.PLAYER_CHANNEL_CONFIG2, AudioConfig.AUDIO_FORMAT);
        //实例化录制对象
        if (null == audioRecord && audioBufSize != AudioRecord.ERROR_BAD_VALUE) {
            audioRecord = new AudioRecord(AudioConfig.AUDIO_RESOURCE,
                    AudioConfig.SAMPLERATE,
                    AudioConfig.PLAYER_CHANNEL_CONFIG2,
                    AudioConfig.AUDIO_FORMAT, audioBufSize);
        }
        //消回音处理
        initAEC(audioRecord.getAudioSessionId());
        new Thread(this).start(); //启动本线程。即实现接口的启动方式
    }

    //消除回音
    public boolean initAEC(int audioSession) {
        if (canceler != null) {
            return false;
        }
        if (!AcousticEchoCanceler.isAvailable()){
            return false;
        }
        canceler = AcousticEchoCanceler.create(audioSession);
        canceler.setEnabled(true);
        return canceler.getEnabled();
    }

    //关闭录制
    public void stopRecording() {
        this.isRecording = false;
    }
    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public void run() {
        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e("ccc", "unInit");
            return;
        }

        // 在录音之前实例化一个编码类，在编码类中实现的数据的发送。
        AudioEncoder encoder = AudioEncoder.getInstance();
        encoder.startEncoding();
        audioRecord.startRecording();

        Log.e(AudioRecorder.class.getSimpleName(), "开始编码");
        this.isRecording = true;
        int size = Speex.getInstance().getFrameSize();
        Log.d(AudioRecorder.class.getSimpleName(),"size:"+size);
        //原来的读取原生代码

        short[] samples = new short[size];
        while (isRecording) {
            int bufferRead = audioRecord.read(samples, 0, size);
            Log.d(AudioRecorder.class.getSimpleName(),"bufferRead:"+bufferRead);
            if (bufferRead > 0) {
                encoder.addData(samples,bufferRead);
            }
        }


        audioRecord.stop();
        encoder.stopEncoding();
    }
}
