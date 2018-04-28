package com.example.dell.myapplication.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.dell.myapplication.R;
import com.example.dell.myapplication.application.MyApplication;
import com.example.dell.myapplication.broadcast.AlarmreRecerve;
import com.example.dell.myapplication.broadcast.LockScreenLintener;
import com.example.dell.myapplication.entity.LoginStatus;
import com.example.dell.myapplication.entity.MediaMessage;
import com.example.dell.myapplication.entity.Events;
import com.example.dell.myapplication.ui.activity.LinkmanActivity;
import com.example.dell.myapplication.utils.RxBus;
import com.example.dell.myapplication.utils.RxUtils;
import com.tencent.TIMConnListener;
import com.tencent.TIMManager;
import com.tencent.callsdk.ILVCallConfig;
import com.tencent.callsdk.ILVCallListener;
import com.tencent.callsdk.ILVCallManager;
import com.tencent.callsdk.ILVIncomingListener;
import com.tencent.callsdk.ILVIncomingNotification;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveLoginManager;

import java.util.List;


public class CallService extends Service implements ILVIncomingListener, ILVCallListener {
    PowerManager.WakeLock wakeLock = null;

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
    }

    @Override
    public void onCallEstablish(int paramInt) {
        RxBus.getInstance().post(new MediaMessage(1));
    }

    @Override
    public void onCallEnd(int paramInt1, int paramInt2, String paramString) {
        boolean isRunn = isClsRunning("com.example.dell.myapplication", LinkmanActivity.class.getName(), this);
        if (isRunn) {
            RxBus.getInstance().post(new MediaMessage(1));
        } else {
            Intent intent = new Intent(this, LinkmanActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            RxBus.getInstance().post(new MediaMessage(1));
        }
    }

    @Override
    public void onException(int paramInt1, int paramInt2, String paramString) {
        boolean isRunn = isClsRunning("com.example.dell.myapplication", LinkmanActivity.class.getName(), this);
        if (isRunn) {
            RxBus.getInstance().post(new MediaMessage(1));
        } else {
            Intent intent = new Intent(this, LinkmanActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            RxBus.getInstance().post(new MediaMessage(1));
        }
    }

    @Override
    public void onNewIncomingCall(final int paramInt1, final int paramInt2, final ILVIncomingNotification paramILVIncomingNotification) {
        boolean isRunn = isClsRunning("com.example.dell.myapplication", LinkmanActivity.class.getName(), this);
        if (isRunn) {
            RxBus.getInstance().post(new Events(paramInt1, paramInt2, paramILVIncomingNotification));
        } else {
            Intent intent = new Intent(this, LinkmanActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            RxBus.getInstance().post(new Events(paramInt1, paramInt2, paramILVIncomingNotification));
        }

    }

    @Override
    public void onDestroy() {
        ILVCallManager.getInstance().removeCallListener(this);
        ILVCallManager.getInstance().removeIncomingListener(this);
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        stopForeground(true);
        super.onDestroy();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("deeeeeeeeeeeee", "--------onStartCommand----------");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.eye_check);
        mBuilder.setContentTitle("");
        mBuilder.setContentText("程序正在运行!");


//        Notification notification=new Notification(R.mipmap.eye_check,"有消息来了",System.currentTimeMillis());
//            notification.flags = Notification.FLAG_SHOW_LIGHTS;
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_SHOW_LIGHTS;
        startForeground(1, notification);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int Minutes = 5 * 10 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + Minutes;
        Intent i = new Intent(this, AlarmreRecerve.class);
        i.setAction("arui.alarm.action");
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }


    public static boolean isClsRunning(String pkg, String cls, Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);

        ActivityManager.RunningTaskInfo task = tasks.get(0);

        if (task != null) {
            return TextUtils.equals(task.topActivity.getPackageName(), pkg) && TextUtils.equals(task.topActivity.getClassName(), cls);
        }
        return false;
    }

}
