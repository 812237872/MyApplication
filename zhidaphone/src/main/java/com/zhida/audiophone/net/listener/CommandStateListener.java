package com.zhida.audiophone.net.listener;

/**
 * Created by YYY on 2019/8/16.
 */

public interface CommandStateListener {
    void onServiceStatusClosed();
    void onServiceStatusConnected();
}
