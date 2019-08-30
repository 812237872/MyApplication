package com.example.socket;


import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.R.id.list;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    ImageView imageView;
    RecyclerView rec;
    private WindowManager mWindowManager;
    private View viewmFloatView;
    private int width;
    private int height;
    List<UserBean> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getDensity();
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 1);
            } else {
                //TODO do something you need
            }
        }
        list=new ArrayList<>();
        for (int i = 0; i <16; i++) {
            list.add(new UserBean("10.55.200.66","周立国","1","老山-四惠",false));
        }
        textView = (TextView) findViewById(R.id.show_tv);
//        imageView = (ImageView) findViewById(R.id.image);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "12134", Toast.LENGTH_LONG).show();
                Intent intent=new Intent(MainActivity.this,ShowActivity.class);
                startActivity(intent);
            }
        });
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, ShowActivity.class);
//                startActivity(intent);
//            }
//        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        addView();
    }

    public void addView() {
        if(mWindowManager==null) {
            mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            // 创建一个新的布局
            WindowManager.LayoutParams param = new WindowManager.LayoutParams();
            // 设置窗口属性
            param.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            // 设置为系统警告窗, 可以悬浮在其他应用之上
            param.format = PixelFormat.TRANSLUCENT;
            // 支持透明
            param.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    // 可在全屏幕布局, 不受状态栏影响
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // 最初不可获取焦点, 这样不影响底层应用接收触摸事件
            param.alpha = 0.9f;
            // 悬浮窗的透明度
            param.gravity = Gravity.LEFT | Gravity.TOP;
            // 悬浮窗的重力效果
            param.width = width / 4;
            // 悬浮窗宽度
            param.height = height / 3;
            // 悬浮窗高度
            // 以下将悬浮穿定位在屏幕中央
            int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
            int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
            param.x = (screenWidth - param.width);
            param.y = (screenHeight - param.height);
            // 创建悬浮窗
            viewmFloatView = View.inflate(this, R.layout.view_float_window, null);
            //获取控件
            rec = viewmFloatView.findViewById(R.id.rec);
            imageView = viewmFloatView.findViewById(R.id.image);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rec.setVisibility(View.VISIBLE);
                }
            });
            rec.setLayoutManager(new LinearLayoutManager(this));
            final RecAdapter recAdapter = new RecAdapter(this, list);
            rec.setAdapter(recAdapter);
            recAdapter.setOnItemClickListener(new RecAdapter.ItemListener() {
                @Override
                public void onItemclick(int i, String ip) {
                    Toast.makeText(MainActivity.this, "位置" + i + "，ip" + ip, Toast.LENGTH_LONG).show();
//                    rec.setVisibility(View.GONE);
                        list.get(i).setIschecked(true);
                        for (int j = 0; j < list.size(); j++) {
                            if(j!=i) {
                                list.get(j).setIschecked(false);
                            }
                        }
                        recAdapter.notifyDataSetChanged();
                    }
            });
//        ButterKnife.bind(this, mFloatView);
            // 添加到屏幕
            mWindowManager.addView(viewmFloatView, param);
            viewmFloatView.setOnTouchListener(new View.OnTouchListener() {
                // 记录上次移动的位置
                float lastX = 0;
                float lastY = 0;
                // 是否是移动事件
                boolean isMoved = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            isMoved = false;
                            // 记录按下位置
                            lastX = event.getRawX();
                            lastY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            isMoved = true;
                            // 记录移动后的位置
                            float moveX = event.getRawX();
                            float moveY = event.getRawY();
                            // 获取当前窗口的布局属性, 添加偏移量, 并更新界面, 实现移动
                            WindowManager.LayoutParams param = (WindowManager.LayoutParams) viewmFloatView.getLayoutParams();
                            param.x += (int) (moveX - lastX);
                            param.y += (int) (moveY - lastY);
                            mWindowManager.updateViewLayout(viewmFloatView, param);
                            lastX = moveX;
                            lastY = moveY;
                        case MotionEvent.ACTION_CANCEL:
                            isMoved = true;
                            break;
                    }
                    // 如果是移动事件, 则消费掉; 如果不是, 则由其他处理, 比如点击
                    return isMoved;
                }

            });
        }
    }


    //获取设备数据
    public void getDensity() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);//display = getWindowManager().getDefaultDisplay();display.getMetrics(dm)（把屏幕尺寸信息赋值给DisplayMetrics dm）;
        width = dm.widthPixels;
        height = dm.heightPixels;
        // 屏幕密度（1.0 / 1.5 / 2.0）
        float density = dm.density;
        // 屏幕密度DPI（160 / 240 / 320）
        int densityDpi = dm.densityDpi;
        Log.e("tag", "当前设备的分辨率宽=（" + width + "*  高度=" + height + "）  densityDpi =" + densityDpi + "  density=" + density);
//        Toast.makeText(this,"宽度："+width+"高度:"+height/13,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this,"销毁",Toast.LENGTH_LONG).show();
            mWindowManager.removeViewImmediate(viewmFloatView);
            mWindowManager=null;


    }
}
