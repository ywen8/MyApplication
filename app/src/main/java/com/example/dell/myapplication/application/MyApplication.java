package com.example.dell.myapplication.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.TIMConnListener;
import com.tencent.TIMManager;
import com.tencent.callsdk.ILVCallConfig;
import com.tencent.callsdk.ILVCallListener;
import com.tencent.callsdk.ILVCallManager;
import com.tencent.callsdk.ILVCallNotification;
import com.tencent.callsdk.ILVCallNotificationListener;
import com.tencent.callsdk.ILVIncomingListener;
import com.tencent.callsdk.ILVIncomingNotification;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveLoginManager;

public class MyApplication  extends Application {
    private static String TAG = "MyApplication";
    private boolean bTLSAccount = true;
    private static Context mContext;
    private static SharedPreferences preferences;
    @Override
    public void onCreate() {
        super.onCreate();
        if (bTLSAccount){
            ILiveSDK.getInstance().initSdk(getApplicationContext(), 1400028285, 11818);
        }else {
            ILiveSDK.getInstance().initSdk(getApplicationContext(), 1400016949, 8002);
        }

        ILVCallManager.getInstance().init(new ILVCallConfig()
                .setAutoBusy(true));
        TIMManager.getInstance().setConnectionListener(new TIMConnListener() {
            @Override
            public void onConnected() {
                Log.e(TAG, "[DEV]onConnected->enter");
            }

            @Override
            public void onDisconnected(int i, String s) {
                Log.e(TAG, "[DEV]onDisconnected->enter: "+i+", "+s);
            }

            @Override
            public void onWifiNeedAuth(String s) {
                Log.e(TAG, "[DEV]onWifiNeedAuth->enter:"+s);
            }
        });
        this.mContext=this;
        this.preferences=getSharedPreferences("login_call",MODE_PRIVATE);
    }


    public static Context  getmContext(){
        return mContext;
    }

    public static SharedPreferences  getPreferences(){

        return preferences;
    }
}
