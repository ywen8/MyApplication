package com.example.dell.myapplication.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.myapplication.R;
import com.tencent.av.opengl.ui.GLView;
import com.tencent.av.sdk.AVAudioCtrl;
import com.tencent.av.sdk.AVView;
import com.tencent.callsdk.ILVBCallMemberListener;
import com.tencent.callsdk.ILVCallConstants;
import com.tencent.callsdk.ILVCallListener;
import com.tencent.callsdk.ILVCallManager;
import com.tencent.callsdk.ILVCallNotification;
import com.tencent.callsdk.ILVCallOption;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.ilivesdk.view.AVVideoView;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 通话界面
 */
public class CallActivity extends Activity implements ILVCallListener, ILVBCallMemberListener, View.OnClickListener,AVRootView.onSubViewCreatedListener,AVRootView.onSurfaceCreatedListener {
    private Button  btnCamera, btnMic, btnSpeaker;
    private AVRootView avRootView;
    private TextView tvTitle, tvLog;
    private RelativeLayout rlControl;
    private LinearLayout llBeauty;
    private SeekBar sbBeauty;
    private ImageView btnEndCall,btn_accpCall;

    private String mHostId;
    private int mCallId;
    private int mCallType;
    private int mBeautyRate;
    private ILVCallNotification ilvCallNotification;
    private boolean bCameraEnable = true;
    private boolean bMicEnalbe = true;
    private boolean bSpeaker = true;
    private int mCurCameraId = ILiveConstants.FRONT_CAMERA;
    private int width;
    private int height;

    private void initView() {
        avRootView = (AVRootView) findViewById(R.id.av_root_view);
//        avRootView.setSubHeight(height);
//        avRootView.setSubWidth(width);
//        avRootView.setSubMarginX(0);
//        avRootView.setSubMarginY(0);
        btnEndCall =findViewById(R.id.btn_end);
        btnSpeaker = (Button) findViewById(R.id.btn_speaker);
        tvTitle = (TextView) findViewById(R.id.tv_call_title);
        btn_accpCall = findViewById(R.id.btn_accp);
        tvLog = (TextView) findViewById(R.id.tv_log);

        btnCamera = (Button) findViewById(R.id.btn_camera);
        btnMic = (Button) findViewById(R.id.btn_mic);

        llBeauty = (LinearLayout) findViewById(R.id.ll_beauty_setting);
        rlControl = (RelativeLayout) findViewById(R.id.rl_control);

        btnEndCall.setVisibility(View.VISIBLE);
        btn_accpCall.setVisibility(View.VISIBLE);
        btn_accpCall.setOnClickListener(this);
        btnEndCall.setOnClickListener(this);
    }

    private void changeCamera() {
        if (true) {
            ILVCallManager.getInstance().enableCamera(mCurCameraId, false);
            avRootView.closeUserView(ILiveLoginManager.getInstance().getMyUserId(), AVView.VIDEO_SRC_TYPE_CAMERA, true);
        } else {
            ILVCallManager.getInstance().enableCamera(mCurCameraId, true);
        }
    }

    private void changeMic() {
        if (bMicEnalbe) {
            ILVCallManager.getInstance().enableMic(false);
        } else {
            ILVCallManager.getInstance().enableMic(true);
        }

        bMicEnalbe = !bMicEnalbe;
        btnMic.setText(bMicEnalbe ? R.string.tip_close_mic : R.string.tip_open_mic);
    }

    private void changeSpeaker() {
        if (bSpeaker) {
            ILiveSDK.getInstance().getAvAudioCtrl().setAudioOutputMode(AVAudioCtrl.OUTPUT_MODE_HEADSET);
        } else {
            ILiveSDK.getInstance().getAvAudioCtrl().setAudioOutputMode(AVAudioCtrl.OUTPUT_MODE_SPEAKER);
        }
        bSpeaker = !bSpeaker;
        btnSpeaker.setText(bSpeaker ? R.string.tip_set_headset : R.string.tip_set_speaker);
    }

    private void switchCamera() {
        mCurCameraId = (ILiveConstants.FRONT_CAMERA == mCurCameraId) ? ILiveConstants.BACK_CAMERA : ILiveConstants.FRONT_CAMERA;
        ILVCallManager.getInstance().switchCamera(mCurCameraId);
    }

    private void setBeauty() {
        if (null == sbBeauty) {
            sbBeauty = (SeekBar) findViewById(R.id.sb_beauty_progress);
            sbBeauty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                    Toast.makeText(CallActivity.this, "beauty " + mBeautyRate + "%", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    // TODO Auto-generated method stub
                    mBeautyRate = progress;
                    ILiveSDK.getInstance().getAvVideoCtrl().inputBeautyParam(9.0f * progress / 100.0f);
                }
            });
        }
        llBeauty.setVisibility(View.VISIBLE);
        rlControl.setVisibility(View.INVISIBLE);
    }

    private void showInviteDlg() {
        final EditText etInput = new EditText(this);
        new AlertDialog.Builder(this).setTitle(R.string.invite_tip)
                .setView(etInput)
                .setPositiveButton(R.string.invite, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!TextUtils.isEmpty(etInput.getText().toString())) {
                            List<String> nums = new ArrayList<String>();
                            nums.add(etInput.getText().toString());
                            ILVCallManager.getInstance().inviteUser(mCallId, nums);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }

    /**
     * 输出日志
     */
    private void addLogMessage(String strMsg) {
        String msg = tvLog.getText().toString();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        msg = msg + "\r\n[" + formatter.format(curDate) + "] " + strMsg;
        tvLog.setText(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        setContentView(R.layout.activity_simple_call);
        initView();
        // 添加通话回调
        ILVCallManager.getInstance().addCallListener(this);

        Intent intent = getIntent();
        mHostId = intent.getStringExtra("HostId");
        mCallType = intent.getIntExtra("CallType", ILVCallConstants.CALL_TYPE_VIDEO);
        mCallId = intent.getIntExtra("CallId", 0);
        ILVCallOption option = new ILVCallOption(mHostId)
                .callTips("CallSDK Demo")
                .setMemberListener(this)
                .setCallType(mCallType);
        if (0 == mCallId) { // 发起呼叫
            List<String> nums = intent.getStringArrayListExtra("CallNumbers");
            if (nums.size() > 1) {
                mCallId = ILVCallManager.getInstance().makeMutiCall(nums, option);
            } else {
                mCallId = ILVCallManager.getInstance().makeCall(nums.get(0), option);
            }

        } else {  // 接听呼叫
            ILVCallManager.getInstance().acceptCall(mCallId, option);
        }

        ILiveLoginManager.getInstance().setUserStatusListener(new ILiveLoginManager.TILVBStatusListener() {
            @Override
            public void onForceOffline(int error, String message) {
                finish();
            }
        });

        ILVCallManager.getInstance().initAvView(avRootView);
        avRootView.setSubCreatedListener(this);
        avRootView.setSurfaceCreateListener(this);

    }

    @Override
    protected void onResume() {
        ILVCallManager.getInstance().onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        ILVCallManager.getInstance().onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ILVCallManager.getInstance().removeCallListener(this);
        ILVCallManager.getInstance().onDestory();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        ILVCallManager.getInstance().endCall(mCallId);
    }


    @Override
    public void onClick(View v) {
        // library中不能使用switch索引资源id
        if (ilvCallNotification == null) {
            ilvCallNotification = new ILVCallNotification();
        }
            ilvCallNotification.setSender(ILVCallManager.getInstance().getSponsorId());
        if (v.getId() == R.id.btn_end) {
            ilvCallNotification.setNotifId(2000);
            ILVCallManager.getInstance().postNotification(mCallId, ilvCallNotification);
            finish();
        } else if (v.getId() == R.id.btn_accp) {
            ilvCallNotification.setNotifId(2001);
            ILVCallManager.getInstance().postNotification(mCallId, ilvCallNotification);
            finish();
        } else if (v.getId() == R.id.btn_camera) {
            changeCamera();
        } else if (v.getId() == R.id.btn_mic) {
            changeMic();
        } else if (v.getId() == R.id.btn_switch_camera) {
            switchCamera();
        } else if (v.getId() == R.id.btn_speaker) {
            changeSpeaker();
        } else if (v.getId() == R.id.btn_beauty) {
            setBeauty();
        } else if (v.getId() == R.id.btn_beauty_setting_finish) {
            llBeauty.setVisibility(View.GONE);
            rlControl.setVisibility(View.VISIBLE);
        } else if (v.getId() == R.id.btn_invite) {
            showInviteDlg();
        } else if (v.getId() == R.id.btn_log) {
            if (View.VISIBLE == tvLog.getVisibility()) {
                tvLog.setVisibility(View.INVISIBLE);
            } else {
                tvLog.setVisibility(View.VISIBLE);
            }
        }
    }


    /**
     * 会话建立回调
     *
     * @param callId
     */
    @Override
    public void onCallEstablish(int callId) {
        avRootView.swapVideoView(0, 1);
        // 设置点击小屏切换及可拖动
        for (int i = 1; i < ILiveConstants.MAX_AV_VIDEO_NUM; i++) {
            final int index = i;
            AVVideoView minorView = avRootView.getViewByIndex(i);
            if (ILiveLoginManager.getInstance().getMyUserId().equals(minorView.getIdentifier())) {
                minorView.setMirror(true);      // 本地镜像
            }
            minorView.setDragable(true);    // 小屏可拖动
            minorView.setGestureListener(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    avRootView.swapVideoView(0, index);     // 与大屏交换
                    return false;
                }
            });
        }
//        changeCamera();
    }

    /**
     * 会话结束回调
     *
     * @param callId
     * @param endResult 结束原因
     * @param endInfo   结束描述
     */
    @Override
    public void onCallEnd(int callId, int endResult, String endInfo) {
        finish();
    }

    @Override
    public void onException(int iExceptionId, int errCode, String errMsg) {
        finish();
    }

    @Override
    public void onCameraEvent(String id, boolean bEnable) {
        addLogMessage("[" + id + "] " + (bEnable ? "open" : "close") + " camera");
    }

    @Override
    public void onMicEvent(String id, boolean bEnable) {
        addLogMessage("[" + id + "] " + (bEnable ? "open" : "close") + " mic");
    }

    @Override
    public void onSubViewCreated() {
        Log.e("qqqqqqqqqqqqqqqqqqqqq","-------------视图已经准备完毕-------------");
    }

    @Override
    public void onSurfaceCreated() {
        Log.e("qqqqqqqqqqqqqqqqqqqqq","-------------SurfaceView已经准备完毕-------------");
    }
}
