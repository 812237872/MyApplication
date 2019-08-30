package com.example.lenovo.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    //广播监听
    TextView textYear, textDay, textWeek, textTime, textTitle, textTimes;
    RecyclerView rec;
    //一天的毫秒
    public final int ONE_DAY=86400000;
    //U盘默认路径
    final String MOUNTS_FILE = "/mnt/sda1/text";
    public final String Ip = "10.55.200.66";
    public final int port = 8036;
    //车辆方向  上下行
    public String busDirection="";
    public SocketAddress s;
    private Socket socket;
    private SharedPreferences title_sp;
    //倒计时标题
    public String time_title = "";
    //bean类
    public List<CBean> list;
    //工具类
    private Utils utils;
    //time文件中读出倒计时的数据
    private String f_time;
    private InputStream inputStream;
    private OutputStream outputStream;
    private int tryCount = 0;//重试次数
    //recyleView适配器
    private RecAdapter recAdapter;
    //登录的线路 信息
    private String g_lineNoList;
    //广播跳转
    IntentFilter filter = null;
    //方向数组。线路数组
    private String[] dir_split;
    private String[] bus_split;
    //方向集合。线路集合
    private List<String> bus_list;
    private List<String> dir_list;
    Handler handler = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {
            initTime();
            //发车时间已过，数据过期
            long times = System.currentTimeMillis();
            for (int i = 0; i < list.size(); i++) {
                if (Long.parseLong(list.get(i).getBusTime()) * 1000 < times) {
                    list.remove(i);
                    recAdapter.notifyDataSetChanged();
                }
            }
            handler.postDelayed(this, 1000);
        }
    };
    Runnable rr = new Runnable() {
        @Override
        public void run() {
            getChaTime();
            handler.postDelayed(this, 1000);
        }
    };
    Handler mHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Collections.sort(list, new Comparator<CBean>() {
                @Override
                public int compare(CBean cBean, CBean t1) {
                    if (Long.parseLong(cBean.getBusTime()) > Long.parseLong(t1.getBusTime())) {
                        return 1;
                    } else if (Long.parseLong(cBean.getBusTime()) == Long.parseLong(t1.getBusTime())) {
                        return 0;
                    }
                    return -1;
                }
            });
            Log.e("LZB", "刷新" + "   list长度" + list.toString());
            recAdapter.notifyDataSetChanged();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
//        getDensity();
        setContentView(R.layout.activity_main);
        //获取SharedPreferences
        title_sp = getSharedPreferences("title", MODE_PRIVATE);
        //广播接受者
        textYear = (TextView) findViewById(R.id.text_year);
        textDay = (TextView) findViewById(R.id.text_day);
        textWeek = (TextView) findViewById(R.id.text_week);
        textTime = (TextView) findViewById(R.id.text_time);
        textTitle = (TextView) findViewById(R.id.text_title);
        textTimes = (TextView) findViewById(R.id.text_times);
        rec = (RecyclerView) findViewById(R.id.rec);
        //初始化工具类
        utils = new Utils();
        //需要抛到主线程的代码
        list = new ArrayList<>();
        recAdapter = new RecAdapter(this, list, MainActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rec.setLayoutManager(layoutManager);
        rec.setAdapter(recAdapter);
//        g_lineNoList = title_sp.getString("busName","886,885");
        //线路方向
        busDirection=title_sp.getString("busDirection","");
        //公交线路
        g_lineNoList = title_sp.getString("busName", "");
        //标题
        String f_title = title_sp.getString("title", "");
        textTitle.setText(f_title);
        //倒计时
        int chaTime = title_sp.getInt("time",0);
            if (chaTime<=0) {
                SharedPreferences.Editor edit = title_sp.edit();
                edit.remove("time");
                edit.remove("t_title");
                edit.commit();
                textTimes.setVisibility(View.GONE);
                handler.removeCallbacks(rr);
            } else {
                String t_title = title_sp.getString("t_title", "");
                textTimes.setText(t_title + chaTime+"天");
                textTimes.setVisibility(View.VISIBLE);
                handler.postDelayed(rr, 1000);
            }
        //时间格式
        initTime();
        //时间轮询
        handler.postDelayed(r, 1000);

        boolean udiskExist = isUdiskExist();
        if(udiskExist){
            getFile(MOUNTS_FILE);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                getXin();
                read();
            }
        }).start();

        //广播监听U盘挂载
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);   //接受外媒挂载过滤器
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);   //接受外媒挂载过滤器
        filter.addDataScheme("file");
        registerReceiver(usbReceiver, filter, "android.permission.READ_EXTERNAL_STORAGE", null);
    }

    //心跳
    public void getXin() {
        try {
            // 建立Socket连接
            if (socket == null) {
                socket = new Socket();
                s = new InetSocketAddress(Ip, port);
                socket.connect(s, 0);
                System.out.println("已发起服务器连接，并进入后续流程～");
                System.out.println("客户端信息：" + socket.getLocalAddress() + " P:" + socket.getLocalPort());
                System.out.println("服务器信息：" + socket.getInetAddress() + " P:" + socket.getPort());
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                String str = "$$AO|" + g_lineNoList + "##";
                Log.e("tags","登录:"+g_lineNoList);
                byte[] c = str.getBytes();
                outputStream.write(c);
                outputStream.flush();
            }
            // 创建读取服务器心跳的线程
            //开启心跳,每隔15秒钟发送一次心跳
            handler.post(mHeartRunnable);
            tryCount = 1;
        } catch (Exception e) {
            tryCount++;
            e.printStackTrace();
            Log.d("tags", "Socket连接建立失败,正在尝试第" + tryCount + "次重连");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getXin();
                }
            }, 10 * 1000);
        }
    }


    public void read() {
        try {
            InputStreamReader isr = new InputStreamReader(inputStream, "GBK");
            StringBuffer stringBuffer = new StringBuffer();
            int len = 0;
            char[] ch = new char[2048];
            while (true) {
                //输入流关闭时循环才会停止
                //数据读完了，再读是等于0
                String strs = "";
                stringBuffer.delete(0, stringBuffer.length());
                len = isr.read(ch);
                Log.e("LZB", "len:" + len);
                for (int i = 0; i < len; i++) {
                    if (ch[i] != '\0') {
                        stringBuffer.append(ch[i]);
                    }
                }
                strs = stringBuffer.toString();
                Log.d("LZB", "我收到来自服务器的消息: " + strs);
                if (strs != null && !strs.equals("")) {
                    int o = strs.indexOf('O');
                    int x = strs.lastIndexOf('#');
                    if (strs.length() - 1 <= o) {
                        Log.e("LZB", "等待消息");
                    } else if (strs.length() - 1 <= x) {
                        Log.e("LZB", "心跳信息");
                        //收到心跳消息以后，首先移除断连消息，然后创建一个新的60秒后执行断连的消息。
                        //这样每次收到心跳后都会重新创建一个60秒的延时消息，在60秒后还没收到心跳消息，表明服务器已死，就会执行断开Socket连接
                        //在60秒钟内如果收到过一次心跳消息，就表明服务器还活着，可以继续与之通讯。
                        handler.removeCallbacks(disConnectRunnable);
                        handler.postDelayed(disConnectRunnable, 1000 * 60);
                    } else {
                        String[] split = strs.split("\\*");
//                        Log.e("LZB", "返回数据长度:" + split.length);
                        if (split.length > 0) {
                            for (int i = 0; i < split.length; i++) {
                                String[] split1 = split[i].split(",");
                                //获取车辆方向
                                String fx=split1[3];
                                //公交线路
                                String bus = split1[0];
                                Log.e("LZB","方向:"+fx);
                                for (int j = 0; j <bus_list.size() ; j++) {
                                    if(!bus_list.get(j).equals("") && bus_list.get(j).equals(bus)){
                                        if(dir_list.get(j).equals("0") && !dir_list.get(j).equals(fx)){
                                                Log.e("LZB","跳过0");
                                                Log.e("LZB","后太："+dir_list.get(j));
                                                continue;
                                        }else if(dir_list.get(j).equals("1")){
                                            if(!dir_list.get(j).equals(fx)){
                                                Log.e("LZB","跳过1");
                                                continue;
                                            }
                                        }
                                    }
                                }
                                //获取班次字段
                                String ban = split1[2];
                                //去重操作
                                for (int k = 0; k < list.size(); k++) {
                                    if (ban.equals(list.get(k).getBusBan()) && fx.equals(list.get(k).getBusDirection())) {
                                        list.remove(k);
                                        Log.d(MainActivity.class.getSimpleName(), "班次。方向一样 覆盖");
                                    }
                                }
                                CBean cBean = new CBean(split1[0], split1[1], split1[2], split1[3], split1[4], split1[5], split1[6], split1[7], split1[8]);
                                list.add(cBean);
                                Log.d(MainActivity.class.getSimpleName(), "班次不样 新增");
                            }
                        }
                        Log.e("LZB", "集合长度" + list.toString());
                        mHander.sendEmptyMessage(1);

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("LZB", "eeeee" + e.toString());
        }

    }

    private Runnable mHeartRunnable = new Runnable() {
        @Override
        public void run() {
            sendData();
        }
    };

    private void sendData() {
        try {
            String str_xin = "$$BO|时间戳##";
            byte[] bytes = str_xin.getBytes();
            outputStream.write(bytes);
            //一定不能忘记这步操作
            outputStream.flush();
            handler.postDelayed(mHeartRunnable, 45 * 1000);
            Log.d("tags", "我发送给服务器的消息: " + str_xin);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("tags", "心跳任务发送失败，正在尝试第" + tryCount + "次重连");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getXin();
                }
            }, 10 * 1000);
        }
    }


    private Runnable disConnectRunnable = new Runnable() {
        @Override
        public void run() {
            disConnect();
        }
    };

    private void disConnect() {
        try {
            Log.d("tag", "正在执行断连: disConnect");
            //执行Socket断连
            handler.removeCallbacks(mHeartRunnable);
            if (outputStream != null) {
                outputStream.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean setSplit(){
        dir_list.clear();
        bus_list.clear();
        dir_split = busDirection.split(",");
        dir_list = new ArrayList<>();
        for (int i = 0; i <dir_split.length ; i++) {
            if(!dir_split[i].equals("")){
                dir_list.add(dir_split[i]);
            }
        }
        bus_split = g_lineNoList.split(",");
        bus_list = new ArrayList<>();
        for (int i = 0; i < bus_split.length; i++) {
            if(!bus_split[i].equals("")){
                bus_list.add(bus_split[i]);
            }
        }
        if(bus_list.size()!=dir_list.size()){
            Toast.makeText(this,"bus文件格式错误，请查看用户手册",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    //获取系统时间
    public void initTime() {
        long times = System.currentTimeMillis();
        Date date = new Date(times);
        SimpleDateFormat year = new SimpleDateFormat("yyyy年");
        SimpleDateFormat day = new SimpleDateFormat("MM月dd日");
        SimpleDateFormat week = new SimpleDateFormat("EEEE");
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        //年
        String for_year = year.format(date);
        String for_day = day.format(date);
        String for_week = week.format(date);
        String for_time = time.format(date);
        textYear.setText(for_year);
        textDay.setText(for_day);
        textWeek.setText(for_week);
        textTime.setText(for_time);
    }

//    //获取设备数据
//    public void getDensity() {
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);//display = getWindowManager().getDefaultDisplay();display.getMetrics(dm)（把屏幕尺寸信息赋值给DisplayMetrics dm）;
//        int width = dm.widthPixels;
//        int height = dm.heightPixels;
//        // 屏幕密度（1.0 / 1.5 / 2.0）
//        float density = dm.density;
//        // 屏幕密度DPI（160 / 240 / 320）
//        int densityDpi = dm.densityDpi;
//        Log.e("tag", "当前设备的分辨率宽=（" + width + "*  高度=" + height + "）  densityDpi =" + densityDpi + "  density=" + density);
////        Toast.makeText(this,"宽度："+width+"高度:"+height/13,Toast.LENGTH_LONG).show();
//    }

    //全屏 隐藏标题栏
    public void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void getFile(String str) {
        SharedPreferences.Editor edit = title_sp.edit();
        String bus_path = str + VolumeInfo.bus;
        File bus_file = new File(bus_path);
        if (bus_file.exists()) {
            String busName = utils.getFile(bus_path);
            int i = busName.lastIndexOf("（");
            String substring = busName.substring(0, i);
            if (!substring.equals("")) {
                String[] split = substring.split("/");
                if(split!=null){
                    g_lineNoList = split[0];
                    busDirection=split[1];
                    boolean b = setSplit();
                    if(b) {
                        edit.putString("busName", busName);
                        edit.putString("busDirection", busDirection);
                    }else{
                        edit.remove("busName");
                        edit.remove("busDirection");
                    }
                    Toast.makeText(this,"bus："+g_lineNoList,Toast.LENGTH_SHORT).show();
                    Toast.makeText(this,"方案："+busDirection,Toast.LENGTH_SHORT).show();
                }
            }
        }
        String title_path = str + VolumeInfo.title;
        File title_file = new File(title_path);
        if (title_file.exists()) {
            String title = utils.getFile(title_path);
            if (!title.equals("")) {
                textTitle.setText(title);
                Toast.makeText(this,"标题："+title,Toast.LENGTH_SHORT).show();
                edit.putString("title", title);
            } else {
                textTitle.setVisibility(View.GONE);
                Toast.makeText(this,"标题："+title,Toast.LENGTH_SHORT).show();
                edit.remove("title");
            }
        }
        String time_path = str + VolumeInfo.time;
        File time_file = new File(time_path);
        if (time_file.exists()) {
            f_time = utils.getFile(time_path);
            if (f_time != "") {
                getChaTime();
            }
        }
        edit.commit();
    }

    //获取倒计时时长
    public void getChaTime() {
        String[] split = utils.getTime(f_time);
        SharedPreferences.Editor edit = title_sp.edit();
        if (split != null) {
            time_title = split[0];
            long will_times = utils.data(split[1]);
            long now_time = System.currentTimeMillis();
            long cha_time = will_times - now_time;
            //相差天数
            if(cha_time>0){
                int i = (int) (cha_time/ONE_DAY);
                if((cha_time%ONE_DAY)>0){
                   i++;
                }
                edit.putString("t_title", split[0]);
                edit.putInt("time", i);
                textTimes.setVisibility(View.VISIBLE);
                textTimes.setText(time_title + i+"天");
            }else{
                textTimes.setVisibility(View.GONE);
                edit.remove("t_title");
                edit.remove("time");
            }
        }else{
            textTimes.setVisibility(View.GONE);
            edit.remove("t_title");
            edit.remove("time");
        }
        edit.commit();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mHander.removeCallbacksAndMessages(null);
            handler.removeCallbacksAndMessages(null);
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否有U盘插入,当U盘开机之前插入使用该方法.
     * @param
     * @return
     */
    public boolean isUdiskExist()
    {
        boolean ret=false;
        File file = new File(MOUNTS_FILE);
        if(file!=null && file.isDirectory()){
            ret=true;
        }
        return ret;
    }

    BroadcastReceiver usbReceiver=new  BroadcastReceiver(){
        String mountPath;
        private StorageManager mStorageManager;
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                mountPath = intent.getData().getPath();
            }
            Toast.makeText(context, "path:" + intent.getData().getPath(), Toast.LENGTH_SHORT).show();
            if (!TextUtils.isEmpty(mountPath)) {
                String dir_Path=mountPath+"/text";
                File dir = new File(dir_Path);
                if (dir.exists() && dir.isDirectory()) {
                    Toast.makeText(context, "文件path:"+dir_Path, Toast.LENGTH_SHORT).show();
                    getFile(dir_Path);
                }else{
                    Toast.makeText(context,"文件不存在！",Toast.LENGTH_SHORT).show();

                }
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)) {
                Log.i("123", "remove ACTION_MEDIA_REMOVED");
            }
        }


    };
}
