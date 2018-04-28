package com.example.dell.myapplication.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.TIMConnListener;
import com.tencent.TIMManager;
import com.tencent.bugly.imsdk.crashreport.CrashReport;
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MyApplication extends Application {
    private static String TAG = "MyApplication";
    private boolean bTLSAccount = true;
    private static Context mContext;
    private static SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        // 获取当前包名
        String packageName = context.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        CrashReport.initCrashReport(getApplicationContext(), "7b140db763", false);
        if (bTLSAccount) {
            ILiveSDK.getInstance().initSdk(getApplicationContext(), 1400028285, 11818);
        } else {
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
                Log.e(TAG, "[DEV]onDisconnected->enter: " + i + ", " + s);
            }

            @Override
            public void onWifiNeedAuth(String s) {
                Log.e(TAG, "[DEV]onWifiNeedAuth->enter:" + s);
            }
        });
        this.mContext = this;
        this.preferences = getSharedPreferences("login_call", MODE_PRIVATE);
    }


    public static Context getmContext() {
        return mContext;
    }

    public static SharedPreferences getPreferences() {

        return preferences;
    }

    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

}
