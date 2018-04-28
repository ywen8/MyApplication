package com.example.dell.myapplication.ui.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.myapplication.R;
import com.example.dell.myapplication.application.MyApplication;
import com.example.dell.myapplication.broadcast.LockScreenLintener;
import com.example.dell.myapplication.entity.Events;
import com.example.dell.myapplication.entity.MediaMessage;
import com.example.dell.myapplication.presenter.LinkmanPresenter;
import com.example.dell.myapplication.presenter.impl.LinkmanPresenterImpl;
import com.example.dell.myapplication.service.CallService;
import com.example.dell.myapplication.ui.view.LinkmanView;
import com.example.dell.myapplication.utils.RxBus;
import com.tencent.callsdk.ILVCallManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.functions.Action1;

public class LinkmanActivity extends AppCompatActivity implements LinkmanView {
    Subscription mSubscription;
    private AlertDialog mIncomingDlg;
    private int mCurIncomingId;
    private String sender = "";
    @BindView(R.id.act_linkman_content_line)
    LinearLayout linkman_content_line;
    @BindView(R.id.act_linkman_progress_line)
    LinearLayout linkman_progress_line;
    @BindView(R.id.act_linkman_call_line)
    LinearLayout linkman_call_line;
    public LinkmanPresenter presenter;
    private static MediaPlayer mediaPlayer = null;
    private Intent serviceIntent;
    private boolean isShowCall = false;
    public Events events;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getSupportActionBar()) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_linkman);
        ButterKnife.bind(this);
        requestPermission(Manifest.permission.READ_PHONE_STATE, 2);
        initRxBus();
        stopMedia();
        presenter = new LinkmanPresenterImpl(this, this);
        serviceIntent = new Intent(this, CallService.class);
        startService(serviceIntent);
    }

    @OnClick(R.id.act_linkman_loginout)
    void loginout() {
        presenter.loginout();
    }

    @OnClick(R.id.act_linkman_call_accp)
    void accp() {
        stop();
        isShowCall = false;
        acceptCall(events.callId, events.notification.getSponsorId(), events.callType);
    }

    @OnClick(R.id.act_linkman_call_turndown)
    void turndown() {
        isShowCall = false;
        ILVCallManager.getInstance().rejectCall(mCurIncomingId);
    }

    public void setEvents(Events events) {
        this.events = events;
    }

    private void initRxBus() {
        mSubscription = RxBus.getInstance().toObserverable(Events.class).subscribe(new Action1<Events>() {
            @Override
            public void call(final Events events) {
                if (events instanceof Events) {

                    sendNotification();
                    setEvents(events);
                    mCurIncomingId = events.callId;
                    if (mediaPlayer == null)
                        playSoundByMedia(R.raw.stone);
                    showCall();
                    isShowCall = true;
                }
            }
        });

    }

    public boolean isGranted(String permission) {
        return !isMarshmallow() || isGranted_(permission);
    }

    private void requestPermission(String permission, int requestCode) {
        if (!isGranted(permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        } else {
            //直接执行相应操作了
        }
    }

    private boolean isGranted_(String permission) {
        int checkSelfPermission = ActivityCompat.checkSelfPermission(this, permission);
        return checkSelfPermission == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private void acceptCall(int callId, String hostId, int callType) {
        Intent intent = new Intent();
        intent.setClass(LinkmanActivity.this, CallActivity.class);
        intent.putExtra("HostId", hostId);
        intent.putExtra("CallId", mCurIncomingId);
        intent.putExtra("CallType", callType);
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isShowCall) {
            isShowCall = false;
            hideCall();
            ILVCallManager.getInstance().rejectCall(mCurIncomingId);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        stopService(serviceIntent);
        stop();
        ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        manager.killBackgroundProcesses(getPackageName());
        mSubscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission Denied
                Toast.makeText(LinkmanActivity.this, "您没有授权该权限，请允许打开该授权", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void showProgress() {
        linkman_content_line.setVisibility(View.GONE);
        linkman_progress_line.setVisibility(View.VISIBLE);
        linkman_call_line.setVisibility(View.GONE);
    }

    @Override
    public void hideProgress() {
        linkman_content_line.setVisibility(View.VISIBLE);
        linkman_progress_line.setVisibility(View.GONE);
        linkman_call_line.setVisibility(View.GONE);
    }


    @Override
    public void toLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showCall() {
        linkman_content_line.setVisibility(View.GONE);
        linkman_progress_line.setVisibility(View.GONE);
        linkman_call_line.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideCall() {
        linkman_content_line.setVisibility(View.VISIBLE);
        linkman_progress_line.setVisibility(View.GONE);
        linkman_call_line.setVisibility(View.GONE);
    }

    public static void playSoundByMedia(int rawId) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);
            try {
                AssetFileDescriptor file = MyApplication.getmContext().getResources().openRawResourceFd(
                        rawId);
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(0.50f, 0.50f);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e) {
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    };

    private void stopMedia() {
        mSubscription = RxBus.getInstance().toObserverable(MediaMessage.class).subscribe(
                new Action1<MediaMessage>() {
                    @Override
                    public void call(MediaMessage events) {
                        if (events instanceof MediaMessage) {
                            Log.e("QQQQQQQQQQQQQQQQ","--------------停止播放----------");
                            stop();
                            hideCall();
                        }
                    }
                }
        );
    }

    private void sendNotification() {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        long[] vibrate = new long[]{0, 500, 1000, 1500};
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //获取NotificationManager实例
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //实例化NotificationCompat.Builde并设置相关属性
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                //设置小图标
                .setSmallIcon(R.mipmap.eye_check)
                //设置通知标题
                .setContentTitle("")
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                //设置通知内容
                .setContentText("正在运行").setVibrate(vibrate)
                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.weixin))
                .setContentIntent(mainPendingIntent).setAutoCancel(true);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledARGB = 0xff0000f;//Led颜色
        notification.ledOnMS = 300;//led亮的时间
        notification.ledOffMS = 300;
        ;//led灭的时间
        notifyManager.notify(1, notification);
    }

    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }
}

