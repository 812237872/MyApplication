package com.zhida.audiophone;

import android.os.Environment;

import com.thf.logger.LogConfig;
import com.thf.logger.LogTool;

/**
 * Created by YYY on 2019/8/16.
 */

public class DemoApplication extends android.app.Application{


    @Override
    public void onCreate()
    {
        super.onCreate();
        LogConfig logConfig=new LogConfig();
        logConfig.setDefaultTag("test");
        logConfig.setFileCount(2);
        logConfig.setLogName("ZdLog");
        logConfig.setLogPath(Environment.getExternalStorageDirectory().getPath());
        logConfig.setMaxSize(10);
        LogTool.init(logConfig);
    }
}
