package com.zhida.audiophone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.zhida.audiophone.activity.VoipActivity;
import com.zhida.audiophone.net.PhoneCall;
import com.zhida.audiophone.net.message.AskMessage;
import com.zhida.audiophone.net.message.CommandLoginMessage;
import com.zhida.audiophone.net.message.OpenResMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText nameInput;
    private EditText callInput;
    private RecyclerView rec;

    private String userName;
    private String askName;

    private String commandIp;
    private int commandPort;

    List<UserBean> list;
    private boolean loginSuccess=false;//信令通道登录成功标志

    /**
     * 语音通话所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.login).setOnClickListener(this);
        findViewById(R.id.call).setOnClickListener(this);
        findViewById(R.id.imageView).setOnClickListener(this);
        nameInput= (EditText) findViewById(R.id.input1);
        callInput= (EditText) findViewById(R.id.input2);
        rec= (RecyclerView) findViewById(R.id.rec);
        commandIp=getString(R.string.command_ip);
        commandPort=getResources().getInteger(R.integer.command_port);
        /**
         *  信令通道初始化
         * */
        PhoneCall.getInstance().init(commandIp,commandPort);
        EventBus.getDefault().register(this);

        list=new ArrayList<>();
        for (int i = 0; i <16; i++) {
            list.add(new UserBean("10.55.200.66","周立国","1","老山-四惠"));
        }
        rec.setLayoutManager(new LinearLayoutManager(this));
        RecAdapter recAdapter=new RecAdapter(this,list);
        rec.setAdapter(recAdapter);
        recAdapter.setOnItemClickListener(new RecAdapter.ItemListener() {
            @Override
            public void onItemclick(int i,String ip) {
                Toast.makeText(MainActivity.this,"位置"+i+"，ip"+ip,Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
        PhoneCall.getInstance().disconnect();
    }

    /**
     * 监听语音呼叫
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerOpenResMessage(AskMessage message)
    {
        if (!TextUtils.isEmpty(message.getUserName()))
        {
            //判断拨号用户名是否为空 如果不为空则进入语音通话界面
            Intent intent=new Intent(MainActivity.this,VoipActivity.class);
            intent.putExtra("type",1);
            intent.putExtra("userName",userName);
            intent.putExtra("askName",message.getUserName());
            startActivity(intent);
        }
    }

    /**
     * 监听信令通道登录消息
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerOpenResMessage(CommandLoginMessage message)
    {
        loginSuccess=true;
        Toast.makeText(MainActivity.this,"信令通道登录成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.login:
                userName=nameInput.getText().toString();
                PhoneCall.getInstance().loginIn(userName, new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess())
                        {
                            Log.d(MainActivity.class.getSimpleName(),"登录发送成功");
                        }else
                        {
                            Log.d(MainActivity.class.getSimpleName(),"登录发送失败");
                        }
                    }
                });
                break;
            case R.id.call:
                askName=callInput.getText().toString();
                if (loginSuccess)//在信令通道登录成功才能拨号
                {
                    /**
                     * 要添加读写和录音权限判断  此处略
                     * */
                    if (!checkPermissions(NEEDED_PERMISSIONS))
                    {
                        ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
                    }else
                    {
                        Intent intent=new Intent(MainActivity.this,VoipActivity.class);
                        intent.putExtra("type",0);
                        intent.putExtra("userName",userName);
                        intent.putExtra("askName",askName);
                        startActivity(intent);
                    }

                }else
                {
                    userName=nameInput.getText().toString();
                    PhoneCall.getInstance().loginIn(userName, new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess())
                            {
                                Log.d(MainActivity.class.getSimpleName(),"登录发送成功");
                            }else
                            {
                                Log.d(MainActivity.class.getSimpleName(),"登录发送失败");
                            }
                        }
                    });
                }
                break;
            case R.id.imageView :
                rec.setVisibility(View.VISIBLE);
                break;
        }
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
