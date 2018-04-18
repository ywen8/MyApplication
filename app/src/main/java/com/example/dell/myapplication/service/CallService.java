package com.example.dell.myapplication.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.dell.myapplication.R;
import com.example.dell.myapplication.application.MyApplication;
import com.example.dell.myapplication.entity.LoginStatus;
import com.example.dell.myapplication.entity.MediaMessage;
import com.example.dell.myapplication.entity.Events;
import com.example.dell.myapplication.ui.activity.LinkmanActivity;
import com.example.dell.myapplication.utils.RxBus;
import com.tencent.callsdk.ILVCallListener;
import com.tencent.callsdk.ILVCallManager;
import com.tencent.callsdk.ILVIncomingListener;
import com.tencent.callsdk.ILVIncomingNotification;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;

public class CallService extends Service implements ILVIncomingListener, ILVCallListener{
    PowerManager.WakeLock wakeLock = null;
    private final static int GRAY_SERVICE_ID = 1001;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, CallService.class.getName());
        wakeLock.acquire();
        ILVCallManager.getInstance().addIncomingListener(this);
        ILVCallManager.getInstance().addCallListener(this);
        SharedPreferences sharedPreferences=MyApplication.getPreferences();
        String username=sharedPreferences.getString("username","");
        String password=sharedPreferences.getString("password","");
        login(username,password);
    }

    @Override
    public void onCallEstablish(int paramInt) {
        RxBus.getInstance().post(new MediaMessage(1));
    }

    @Override
    public void onCallEnd(int paramInt1, int paramInt2, String paramString) {
        RxBus.getInstance().post(new MediaMessage(1));
    }

    @Override
    public void onException(int paramInt1, int paramInt2, String paramString) {
        RxBus.getInstance().post(new MediaMessage(1));
    }

    @Override
    public void onNewIncomingCall(final int paramInt1, final int paramInt2, final ILVIncomingNotification paramILVIncomingNotification) {
        RxBus.getInstance().post(new Events(paramInt1,paramInt2,paramILVIncomingNotification));
    }

    @Override
    public void onDestroy() {
        ILVCallManager.getInstance().removeCallListener(this);
        ILVCallManager.getInstance().removeIncomingListener(this);
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        super.onDestroy();

    }
    private  void login(final String  username, String password){
        ILiveLoginManager.getInstance().tlsLogin(username, password, new ILiveCallBack<String>() {
            @Override
            public void onSuccess(String data) {

                loginSDK(username, data);

            }
            @Override
            public void onError(String module, int errCode, String errMsg) {

            }
        });
    }
    private void loginSDK(final String id, String userSig){
        ILiveLoginManager.getInstance().iLiveLogin(id, userSig, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                RxBus.getInstance().post(new LoginStatus(1,""));
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {

                RxBus.getInstance().post(new LoginStatus(2,"Login failed:"+module+"|"+errCode+"|"+errMsg));

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setPriority(Notification.PRIORITY_MIN);// 设置该通知优先级
        //  mBuilder.setSmallIcon(R.drawable.gpsblue);
        Notification notification = mBuilder.build();
        startForeground(1, notification);
        return super.onStartCommand(intent, flags, startId);
    }
    private  void countDown(){

    }

    BroadcastReceiver MyBroadCastReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i("application", "Screen went OFF");
                Toast.makeText(context, "screen OFF", Toast.LENGTH_LONG).show();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.i("application", "Screen went ON");
                Toast.makeText(context, "screen ON", Toast.LENGTH_LONG).show();
            }
        }
    };
}
