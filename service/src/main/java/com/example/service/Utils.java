package com.example.service;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Lenovo on 2019/8/12.
 */

public class Utils {
    public Utils() {

    }
    //读取文件内容
    public String getFile(String s) {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + s);
        if (file.exists()) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream,"GB2312");
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    while ((line = buffreader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    Log.e("tags", "str:" + stringBuilder);
                    String str = stringBuilder.toString();
                    instream.close();
                    return str;
                }
            } catch (Exception e) {
                Log.e("TestFile", e.getMessage());
            }
        }
        return "";
    }
    //文本时间格式截取 拼接为 年月日时分秒
    public long getTime(String line) {
        if (line!=null) {
            int i = line.indexOf("（" );
            String substring = line.substring(0,i);
            String time=substring+"0时0分0秒";
            long data = data(time);
            Log.e("tags","时间戳："+data);
            return data;
        }
        return 0;
    }
    //时间格式转换为时间戳
    public long data(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒", Locale.CHINA);
        Date date;
        long s = 0;
        try {
            date = sdr.parse(time);
            s = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }
    //时间戳转化为日时分
    public String getChaTime(long cha){
        Date data=new Date(cha);
        SimpleDateFormat time_type = new SimpleDateFormat("dd日HH时mm分ss秒");
        String format = time_type.format(data);
        return format;
    }
}

