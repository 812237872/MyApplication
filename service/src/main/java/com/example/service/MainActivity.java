package com.example.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.lenovo.myapplication.IMyAidlInterface;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class MainActivity extends AppCompatActivity {

    public IMyAidlInterface iMyAidlInterface;
    EditText edit_text;
    Button btn_change;
    public String text = "/bus.txt";
    public String time = "/time.txt";
    public String Ip="10.55.200.66";
    public int port=8036;
    public Socket socket;
    public SocketAddress s;
//    Handler handler=new Handler();
//    Runnable r=new Runnable() {
//        @Override
//        public void run() {
//            getChaTime();
//            handler.postDelayed(this,1000);
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_main);
        edit_text = (EditText) findViewById(R.id.edit_text);
        btn_change = (Button) findViewById(R.id.btn_change);
        String str="1,30664,32,0,1565937900,王春生,四惠-老山,101,72,*";
        String[] strs=str.split("\\*");
        Log.e("tagss","strs"+strs.toString());
        for (int i = 0; i <strs.length; i++) {
            String[] split = strs[i].split(",");
            for (int j = 0; j <split.length; j++) {
            Log.e("tags",split[j]);
        }
        }

        //                                    diaoBean.setBusLu(split1[0]);
//                                    diaoBean.setBusName(split1[1]);
//                                    diaoBean.setBusBan(split1[2]);
//                                    diaoBean.setBusDirection(split1[3]);
//                                    diaoBean.setBusTime(split1[4]);
//                                    diaoBean.setBusPeople(split1[5]);
//                                    diaoBean.setBusScheme(split1[6]);
//                                    diaoBean.setBusSchemesAttributes(split1[7]);
//                                    diaoBean.setBusOnePoint(split1[8]);

        //倒计时
//        getChaTime();
//        handler.postDelayed(r,1000);
//        ServiceConnection con = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//                iMyAidlInterface = IMyAidlInterface.Stub.asInterface(iBinder);
//                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
//                Log.e("tag", "连接成功:" + componentName);
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName componentName) {
//                Log.e("tag", "连接失败:" + componentName);
//                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
//            }
//        };
//
//        bindMyService(con);
//
//        btn_change.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String str = edit_text.getText().toString();
//                try {
//                    if (!str.equals("") && str.length() <= 30) {
//                        iMyAidlInterface.sendMessage(str);
//                        Log.e("tag", "Str:" + str);
//                    }
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

//        String file = utils.getFile(text);
//        btn_change.setText(file);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    socket = new Socket();
//                    s=new InetSocketAddress(Ip,port);
//                    // 连接本地，端口2000；超时时间3000ms
//                    socket.connect(s,3000);
//                    System.out.println("已发起服务器连接，并进入后续流程～");
//                    System.out.println("客户端信息：" + socket.getLocalAddress() + " P:" + socket.getLocalPort());
//                    System.out.println("服务器信息：" + socket.getInetAddress() + " P:" + socket.getPort());
//                    // 发送接收数据
//                    OutputStream outputStream=socket.getOutputStream();
//                    DataOutputStream writer = new DataOutputStream(outputStream);
//                    String g_LineNoList="463";
//                    String str = "$$AO|" + g_LineNoList + "##";
//                    writer.writeUTF(str); // 写一个UTF-8的信息
//                    writer.flush();
//                    writer.close();
//                    socket.shutdownInput();
//                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    String line = br.readLine();
//                    Log.d("tags", line);
//
//                    // 释放资源
//                    socket.close();
//                    System.out.println("客户端已退出～");

//                    if(socket==null){
//                        socket = new Socket("10.55.200.66",8036);
//                        Log.e("tags","正在连接");
//                        Log.e("tags", "与服务器建立连接：" );
//                        OutputStream outputStream=socket.getOutputStream();
//                        DataOutputStream writer = new DataOutputStream(outputStream);
//                        String g_LineNoList="463";
//                        String str = "$$AO|" + g_LineNoList + "##";
//                        writer.writeUTF(str); // 写一个UTF-8的信息
//                        writer.flush();
//                        writer.close();
//                        socket.shutdownInput();
//                        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                        String line = br.readLine();
//                    }
//            socketCon(socket);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

    }


//    public void bindMyService(ServiceConnection con) {
//        Intent intent = new Intent();
//        intent.setAction("com.example.lenovo.myapplication.MyService");
//        intent.setPackage("com.example.lenovo.myapplication");
//        bindService(intent, con, Context.BIND_AUTO_CREATE);
//    }
    //获取倒计时时长
    public void getChaTime(){
        Utils utils=new Utils();
        String f_time = utils.getFile(time);
        long will_times = utils.getTime(f_time);
        long now_time = getTime();
        long cha_time=will_times-now_time;
        String chaTime = utils.getChaTime(cha_time);
        btn_change.setText(chaTime);
    }

    public long getTime(){
            long times = System.currentTimeMillis();
        return times;
    }
    public void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        //隐藏状态栏
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        handler.removeCallbacksAndMessages(null);
    }
}
