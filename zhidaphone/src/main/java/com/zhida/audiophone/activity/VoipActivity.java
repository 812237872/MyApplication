package com.zhida.audiophone.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Toast;

import com.gyz.voipdemo_speex.util.Speex;
import com.zhida.audiophone.R;
import com.zhida.audiophone.audio.AudioDecoder;
import com.zhida.audiophone.audio.AudioPlayer;
import com.zhida.audiophone.audio.AudioRecorder;
import com.zhida.audiophone.net.PhoneCall;
import com.zhida.audiophone.net.message.AudioClosedMessage;
import com.zhida.audiophone.net.message.AudioLoginSuccess;
import com.zhida.audiophone.net.message.AudioMessage;
import com.zhida.audiophone.net.message.OpenResMessage;
import com.zhida.audiophone.provider.EncodeProvider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Created by YYY on 2019/8/19.
 */

public class VoipActivity extends AppCompatActivity implements View.OnClickListener {

    private Chronometer timer;


    private EncodeProvider provider; //音频播放的变量
    private AudioRecorder audioRecorder; //记录音频。

    private boolean isAnswer = false;  //是否接电话
    private boolean isBusy = false;  //是否正在通话中。true 表示正忙 false 表示为不忙。

    private CountDownTimer mCountDownTimer; //打电话超时计时器

    public String host = "10.2.210.216";//ip
    public int tcp_port = 8002;//端口

    private boolean configSuccess = false;
    private int type;//进入类型 0：主叫 1：来电
    private String userName;//用户姓名
    private String askName;//对方姓名
    private String YP_CMD_OPT;

    /**
     * 语音通话所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_voip);
        EventBus.getDefault().register(this);
        host = getString(R.string.audio_ip);
        tcp_port = getResources().getInteger(R.integer.audio_port);
        init();
    }

    /**
     * 初始化一些控件和多媒体通道
     */
    private void init() {
        //设置挂断按钮
        findViewById(R.id.calling_hangup).setOnClickListener(this);
        findViewById(R.id.talking_hangup).setOnClickListener(this);
        findViewById(R.id.ring_pickup).setOnClickListener(this);
        findViewById(R.id.ring_hang_off).setOnClickListener(this);
        timer = (Chronometer) findViewById(R.id.timer);

        type = getIntent().getIntExtra("type", -1);
        userName = getIntent().getStringExtra("userName");
        askName = getIntent().getStringExtra("askName");

        //拨打电话倒计时计时器。倒计时10s
        mCountDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (!isAnswer) { //如果没有人应答，则挂断
                    hangupOperation(0);
                    Toast.makeText(VoipActivity.this, "打电话超时，请稍后再试！", Toast.LENGTH_SHORT).show();
                }

            }
        };


        if (type == 0 || type == 1) {
            if (TextUtils.isEmpty(userName)) {
                Toast.makeText(this, "userName不能为空", Toast.LENGTH_SHORT).show();
                finish();
            }
            if (TextUtils.isEmpty(askName)) {
                Toast.makeText(this, "askName", Toast.LENGTH_SHORT).show();
                finish();
            }
            if (type == 0) {
                //展示拨号页面
                showCallingView();
            } else if (type == 1) {
                //展示来电页面
                showRingView();
            }
            Speex.getInstance().init();
            audioRecorder = new AudioRecorder();
            provider = new EncodeProvider(host, tcp_port, userName);
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭流媒体通道
        AudioPlayer.getInstance().stopPlaying();
        provider.shutDownSocket();
        EventBus.getDefault().unregister(this);

    }

    //先检查是否在流媒体通道上登录成功了
    private void realStart() {
        if (configSuccess) {
            PhoneCall.getInstance().call(askName, new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {

                }
            });
            if (OpenResMessage.YP_CMD_OPT_CONSENT.equals(YP_CMD_OPT)) {
                showTalkingView();
                audioRecorder.startRecording(); //开始语音播放。
                isAnswer = true; //接通电话为真
            }
        }
    }


    /**
     * 监听信令通道消息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerOpenResMessage(OpenResMessage message) {
        YP_CMD_OPT = message.getYP_CMD_OPT();
        switch (message.getYP_CMD_OPT()) {
            case OpenResMessage.YP_CMD_OPT_NOUSER://对方不存在
                Toast.makeText(VoipActivity.this, "不存在此用户", Toast.LENGTH_SHORT).show();
                break;
            case OpenResMessage.YP_CMD_OPT_OFFLINE://对方不在线
                Toast.makeText(VoipActivity.this, "对方用户不在线", Toast.LENGTH_SHORT).show();
                break;
            case OpenResMessage.YP_CMD_OPT_BUSY://对方忙
                Toast.makeText(VoipActivity.this, "对方忙", Toast.LENGTH_SHORT).show();
                break;
            case OpenResMessage.YP_CMD_OPT_ASK://已经通知对方
                Toast.makeText(VoipActivity.this, "已经通知对方", Toast.LENGTH_SHORT).show();
                break;
            case OpenResMessage.YP_CMD_OPT_CONSENT://对方同意
                realStart();
                break;
            case OpenResMessage.YP_CMD_OPT_REFUSE://对方拒绝
                Toast.makeText(VoipActivity.this, "对方拒绝", Toast.LENGTH_SHORT).show();
                break;
            case OpenResMessage.YP_CMD_OPT_SOCKETERROR://媒体链接建立失败
                Toast.makeText(VoipActivity.this, "媒体链接建立失败", Toast.LENGTH_SHORT).show();
                break;
            case OpenResMessage.YP_CMD_OPT_TIMEOUT://超时
                Toast.makeText(VoipActivity.this, "拨号超时", Toast.LENGTH_SHORT).show();
                break;
            case OpenResMessage.YP_CMD_OPT_PHONE://通话中
                break;


        }
    }

    /**
     * 处理接收到的流媒体信息
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void handlerAudioMessage(AudioMessage message) {
        if (isAnswer) {
            byte[] frameDate = message.getFrame();
            AudioDecoder.getInstance().addData(frameDate, frameDate.length);
        }
    }

    /**
     * 处理流媒体通道登录成功消息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerAudioLoginSuccess(AudioLoginSuccess message) {
        configSuccess = true;
        Toast.makeText(VoipActivity.this, "媒体通道登录成功！", Toast.LENGTH_SHORT).show();
        realStart();
    }

    /**
     * 处理接收到的流媒体通道关闭消息
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void handlerAudioClosedMessage(AudioClosedMessage message) {
        hangupOperation(3);
    }


    // 显示呼叫时候的view
    private void showCallingView() {
        findViewById(R.id.calling_view).setVisibility(View.VISIBLE);
        findViewById(R.id.talking_view).setVisibility(View.GONE);
        findViewById(R.id.ring_view).setVisibility(View.GONE);

        //开启定时器。
        mCountDownTimer.start();
    }

    //显示说话时候的view
    private void showTalkingView() {

        findViewById(R.id.talking_view).setVisibility(View.VISIBLE);
        findViewById(R.id.calling_view).setVisibility(View.GONE);
        findViewById(R.id.ring_view).setVisibility(View.GONE);
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
    }

    //显示响铃界面
    private void showRingView() {
        findViewById(R.id.ring_view).setVisibility(View.VISIBLE);
        findViewById(R.id.calling_view).setVisibility(View.GONE);
        findViewById(R.id.talking_view).setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ring_pickup: //在响铃界面接电话
                if (configSuccess) {
                    if (!checkPermissions(NEEDED_PERMISSIONS)) {
                        ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
                    } else {
                        PhoneCall.getInstance().ringPickup(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {

                                //发送接听命令成功
                                if (future.isSuccess()) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showTalkingView();
                                            audioRecorder.startRecording();// 开始发送语音信息
                                            isAnswer = true; //接通电话为真
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
                break;
            case R.id.ring_hang_off: //在响铃界面拒绝  发送ask_res 6命令
                hangupOperation(1);
                break;
            case R.id.calling_hangup: //正在拨打中挂断 发送close命令
                hangupOperation(2);
                break;
            case R.id.talking_hangup: //通话中挂断 发送close命令
                hangupOperation(2);
                break;
        }
    }

    /**
     * 进行挂断电话时候的逻辑
     * type: 0-拨号超时挂断  1-响铃决绝挂断
     */
    private void hangupOperation(int type) {
        switch (type) {
            case 1:
                PhoneCall.getInstance().answer(OpenResMessage.YP_CMD_OPT_REFUSE, new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        Log.d(VoipActivity.class.getSimpleName(), "拒绝消息发送结束");
                    }
                });
                break;
            case 2:
                PhoneCall.getInstance().closePhone(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        Log.d(VoipActivity.class.getSimpleName(), "挂断消息发送结束");
                    }
                });
                break;
        }
        //发送挂断消息
        audioRecorder.stopRecording(); //关闭录音和发送数据
        finish();
    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

}
