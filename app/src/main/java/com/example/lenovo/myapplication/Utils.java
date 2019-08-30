package com.example.lenovo.myapplication;

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
        File file = new File(s);
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
    //截取文本
    public String[] getTime(String line) {
        if (line!=null) {
            int i = line.lastIndexOf("（" );
            if(i>=0) {
                String substring = line.substring(0, i);
                String time = substring + "0时0分0秒";
                String[] split = time.split(",");
                return split;
            }
        }
        return null;
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

}
