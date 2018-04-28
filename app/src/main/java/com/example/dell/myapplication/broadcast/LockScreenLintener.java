package com.example.dell.myapplication.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;


public class LockScreenLintener {

    private Context mContext;
    private LockScreenCallBack screenCallBack;
    private LockScreenBroadcastReceiver screenBroadcastReceiver;

    public LockScreenLintener(Context mContext) {
        this.mContext= mContext;
        screenBroadcastReceiver=new LockScreenBroadcastReceiver();
    }

    /**
     * screen状态广播接收者
     */
    private class LockScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
                screenCallBack.openScreen();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
                screenCallBack.lockScreen();
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁
                screenCallBack.deblocking();
            }
        }
    }
    public void begin(LockScreenCallBack listener) {
        screenCallBack = listener;
        registerListener();
        getScreenState();
    }

    /**
     * 获取screen状态
     */
    private void getScreenState() {
        PowerManager manager = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);
        if (manager.isScreenOn()) {
            if (screenCallBack != null) {
                screenCallBack.openScreen();
            }
        } else {
            if (screenCallBack != null) {
                screenCallBack.lockScreen();
            }
        }
    }

    private void registerListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(screenBroadcastReceiver, filter);
    }

    public interface LockScreenCallBack {

        void  openScreen();

        void lockScreen();

        void deblocking();

    }


}
