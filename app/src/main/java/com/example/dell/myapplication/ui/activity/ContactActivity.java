package com.example.dell.myapplication.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.myapplication.manager.AccountMgr;
import com.example.dell.myapplication.entity.Events;
import com.example.dell.myapplication.R;
import com.example.dell.myapplication.utils.RxBus;
import com.example.dell.myapplication.service.CallService;
import com.tencent.callsdk.ILVCallConstants;
import com.tencent.callsdk.ILVCallListener;
import com.tencent.callsdk.ILVCallManager;
import com.tencent.callsdk.ILVIncomingListener;
import com.tencent.callsdk.ILVIncomingNotification;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;

import java.util.ArrayList;

import rx.Subscription;
import rx.functions.Action1;

/**
 * 联系人界面
 */
public class ContactActivity extends Activity implements View.OnClickListener, ILVIncomingListener, ILVCallListener, ILVCallManager.CallBack {
    private static String TAG = "ContactActivity";
    private TextView tvMyAddr, tvMsg;
    private EditText etDstAddr, idInput, pwdInput;
    private ListView lvCallList;
    private Button confrim, regist;
    ArrayList<String> callList = new ArrayList<String>();
    private ArrayAdapter adapterCallList;
    private LinearLayout callView, loginView, llDstNums;
    private AlertDialog mIncomingDlg;
    private int mCurIncomingId;
    private String sender = "";
    Subscription mSubscription;
    public final static int REQUEST_READ_PHONE_STATE = 1;
    public static final int CAMERA = 2;

    private boolean bTLSAccount = true; // 默认为托管模式，与iOS一致

    // 多人视频控件列表
    private ArrayList<EditText> mEtNums = new ArrayList<>();

    private AccountMgr mAccountMgr = new AccountMgr();

    private boolean bLogin; // 记录登录状态

    // 内部方法
    private void initView() {
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        tvMyAddr = (TextView) findViewById(R.id.tv_my_address);
        etDstAddr = (EditText) findViewById(R.id.et_dst_address);
        lvCallList = (ListView) findViewById(R.id.lv_call_list);
        idInput = (EditText) findViewById(R.id.id_account);
        pwdInput = (EditText) findViewById(R.id.id_password);
        confrim = (Button) findViewById(R.id.confirm);
        regist = (Button) findViewById(R.id.regist);
        callView = (LinearLayout) findViewById(R.id.call_view);
        loginView = (LinearLayout) findViewById(R.id.login_view);
        llDstNums = (LinearLayout) findViewById(R.id.ll_dst_numbers);
        confrim.setOnClickListener(this);
        regist.setOnClickListener(this);
        adapterCallList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                callList);
        lvCallList.setAdapter(adapterCallList);
        lvCallList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strNums = (String) adapterCallList.getItem(position);
                String[] numArrs = strNums.split(",");
                ArrayList<String> nums = new ArrayList<String>();
                for (int i = 0; i < numArrs.length; i++) {
                    nums.add(numArrs[i]);
                }
                makeCall(ILVCallConstants.CALL_TYPE_VIDEO, nums);
            }
        });

    }


    private void addCallList(String remoteId) {
        if (!callList.contains(remoteId)) {
            if (callList.add(remoteId)) {
                adapterCallList.notifyDataSetChanged();
            }
        }
    }
    private void initRxBus(){
        mSubscription= RxBus.getInstance().toObserverable(Events.class).subscribe(new Action1<Events>() {
            @Override
            public void call(final Events events) {
                Log.e("gggggggggggggggg","----------收到消息-----------");
                if(events instanceof Events){
                    if (null != mIncomingDlg) {  // 关闭遗留来电对话框
                        mIncomingDlg.dismiss();
                    }
                    mCurIncomingId = events.callId;
                    mIncomingDlg = new AlertDialog.Builder(ContactActivity.this)
                            .setTitle("New Call From " + events.notification.getSender())
                            .setMessage(events.notification.getNotifDesc())
                            .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sender = events.notification.getSponsorId();
                                    acceptCall(events.callId, events.notification.getSponsorId(), events.callType);
//                        addLogMessage("Accept Call :"+mCurIncomingId);
                                }
                            })
                            .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int ret = ILVCallManager.getInstance().rejectCall(mCurIncomingId);
//                        addLogMessage("Reject Call:"+ret+"/"+mCurIncomingId);
                                }
                            })
                            .create();
                    mIncomingDlg.setCanceledOnTouchOutside(false);
                    mIncomingDlg.show();
                }
            }
        });

    }
    /**
     * 注销后处理
     */
    private void onLogout() {
        // 注销成功清除用户信息，并跳转到登陆界面
        //finish();
        bLogin = false;
        callView.setVisibility(View.INVISIBLE);
//        loginView.setVisibility(View.VISIBLE);
    }

    /**
     * 输出日志
     */
    private void addLogMessage(String strMsg) {
        Log.i("-----call----------", strMsg);
//        String msg = tvMsg.getText().toString();
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
//        msg = msg + "\r\n["+formatter.format(curDate)+"] " + strMsg;
//        tvMsg.setText(msg);
    }

    /**
     * 注销
     */
    private void logout() {
        ILiveLoginManager.getInstance().iLiveLogout(new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                onLogout();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                onLogout();
            }
        });
    }

    // 覆盖方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_simple_main);
        //TODO 初始化随心播
        initView();
        requestPermission(Manifest.permission.READ_PHONE_STATE, CAMERA);
        // 设置通话回调
//        ILVCallManager.getInstance().addIncomingListener(this);
//        ILVCallManager.getInstance().addCallListener(this);
//        ILVCallManager.getInstance().setCallBack(this);
//        addLogMessage("Init CallSDK...");
        login("aaa321321","12345678");
//        initRxBus();
        Intent intent = new Intent(this, CallService.class);
        startService(intent);
    }

    @Override
    public void onBackPressed() {

        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
//        if (bLogin){
//            ILiveLoginManager.getInstance().iLiveLogout(new ILiveCallBack() {
//                @Override
//                public void onSuccess(Object data) {
//                    finish();
//                }
//
//                @Override
//                public void onError(String module, int errCode, String errMsg) {
//                    finish();
//                }
//            });
//        }else{
//            finish();
//        }
    }

    @Override
    protected void onDestroy() {
//        ILVCallManager.getInstance().removeIncomingListener(this);
//        ILVCallManager.getInstance().removeCallListener(this);
        mSubscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

            if(keyCode==KeyEvent.KEYCODE_BACK){
                if(null!=mIncomingDlg){
                    mIncomingDlg.dismiss();
                    int ret = ILVCallManager.getInstance().rejectCall(mCurIncomingId);
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    home.addCategory(Intent.CATEGORY_HOME);
                    startActivity(home);
                    return  false;
                }
            }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onClick(View v) {
        if (R.id.btn_logout == v.getId()) {
            logout();
        } else if (R.id.btn_make_call == v.getId()) {
            String remoteId = etDstAddr.getText().toString();
            if (TextUtils.isEmpty(remoteId)) {
                Toast.makeText(this, R.string.toast_phone_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> nums = new ArrayList<>();
            String tmpNum;
            String calllist = "";
            nums.add(remoteId);
            for (EditText etNum : mEtNums) {
                tmpNum = etNum.getText().toString();
                if (!TextUtils.isEmpty(tmpNum)) {
                    nums.add(tmpNum);
                    calllist = calllist + tmpNum + ",";
                }
            }
            calllist = calllist + remoteId;

            // 添加通话记录
            addCallList(calllist);
            makeCall(ILVCallConstants.CALL_TYPE_VIDEO, nums);
        } else if (R.id.regist == v.getId()) {
            tryRegister();
        } else if (R.id.confirm == v.getId()) {
            tryLogin();
        } else if (R.id.btn_add == v.getId()) {
            addNewInputNumbers();
        }
    }

    private void tryRegister() {

        idInput.setError(null);
        pwdInput.setError(null);

        final String userName = idInput.getText().toString();
        final String password = pwdInput.getText().toString();

        boolean cancel = false;
        View focusAfter = null;

        if (TextUtils.isEmpty(userName) || !isValidUserName(userName)) {
            idInput.setError(getString(R.string.tip_hit_account));
            focusAfter = idInput;
            cancel = true;
        } else if (TextUtils.isEmpty(password) || !isValidPassword(password)) {
            pwdInput.setError(getString(R.string.tip_hit_password));
            focusAfter = pwdInput;
            cancel = true;
        }

        if (cancel) {
            focusAfter.requestFocus();
        } else {
            regist(idInput.getText().toString(), pwdInput.getText().toString());
        }
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.length() <= 16 && password.matches("^[A-Za-z0-9]*$");
    }

    private boolean isValidUserName(String userName) {
        return userName.length() >= 4 &&
                userName.length() <= 24 &&
                userName.matches("^[A-Za-z0-9]*[A-Za-z][A-Za-z0-9]*$");
    }

    private void tryLogin() {

        idInput.setError(null);
        pwdInput.setError(null);

        final String userName = idInput.getText().toString();
        final String password = pwdInput.getText().toString();

        boolean cancel = false;
        View focusAfter = null;

        if (TextUtils.isEmpty(userName)) {
            idInput.setError("用户名不可为空");
            focusAfter = idInput;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            pwdInput.setError("密码不可为空");
            focusAfter = pwdInput;
            cancel = true;
        }

        if (cancel) {
            focusAfter.requestFocus();
        } else {
            login(idInput.getText().toString(), pwdInput.getText().toString());
        }
    }

    /**
     * 使用userSig登录iLiveSDK(独立模式下获有userSig直接调用登录)
     */
    private void loginSDK(final String id, String userSig) {
        ILiveLoginManager.getInstance().iLiveLogin(id, userSig, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                bLogin = true;
//                addLogMessage("Login CallSDK success:"+id);
                tvMyAddr.setText(ILiveLoginManager.getInstance().getMyUserId());
                callView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Toast.makeText(ContactActivity.this, "Login failed:" + module + "|" + errCode + "|" + errMsg, Toast.LENGTH_SHORT).show();
                loginView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 登录并获取userSig(*托管模式，独立模式下直接用userSig调用loginSDK登录)
     */
    private void login(final String id, String password) {
        loginView.setVisibility(View.INVISIBLE);

        if (bTLSAccount) {
            ILiveLoginManager.getInstance().tlsLogin(id, password, new ILiveCallBack<String>() {
                @Override
                public void onSuccess(String data) {
                    loginSDK(id, data);
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    Toast.makeText(getApplicationContext(), "login failed:" + module + "|" + errCode + "|" + errMsg, Toast.LENGTH_SHORT).show();
                    loginView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            mAccountMgr.login(id, password, new AccountMgr.RequestCallBack() {
                @Override
                public void onResult(int error, String response) {
                    if (0 == error) {
                        loginSDK(id, response);
                    } else {
                        Toast.makeText(getApplicationContext(), "login failed:" + response, Toast.LENGTH_SHORT).show();
                        loginView.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    /**
     * 注册用户名(*托管模式，独立模式下请向自己私有服务器注册)
     */
    private void regist(String account, String password) {
        if (bTLSAccount) {
            ILiveLoginManager.getInstance().tlsRegister(account, password, new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    Toast.makeText(getApplicationContext(), "regist success!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    Toast.makeText(getApplicationContext(), "regist failed:" + module + "|" + errCode + "|" + errMsg, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            mAccountMgr.regist(account, password, new AccountMgr.RequestCallBack() {
                @Override
                public void onResult(int error, String response) {
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 添加新的用户号码输入
     */
    private void addNewInputNumbers() {
        if (mEtNums.size() >= 3) {
            return;
        }
        final LinearLayout linearLayout = new LinearLayout(this);
        final EditText etNum = new EditText(this);
        mEtNums.add(etNum);
        Button btnDel = new Button(this);
        btnDel.setText("-");
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtNums.remove(etNum);
                llDstNums.removeView(linearLayout);
            }
        });
        linearLayout.addView(btnDel, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(etNum, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        llDstNums.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * 发起呼叫
     */
    private void makeCall(int callType, ArrayList<String> nums) {
        Intent intent = new Intent();
        intent.setClass(this, CallActivity.class);
        intent.putExtra("HostId", ILiveLoginManager.getInstance().getMyUserId());
        intent.putExtra("CallId", 0);
        intent.putExtra("CallType", callType);
        intent.putStringArrayListExtra("CallNumbers", nums);
        startActivity(intent);
    }

    private void acceptCall(int callId, String hostId, int callType) {
        Intent intent = new Intent();
        intent.setClass(ContactActivity.this, CallActivity.class);
        intent.putExtra("HostId", hostId);
        intent.putExtra("CallId", mCurIncomingId);
        intent.putExtra("CallType", callType);
        startActivity(intent);
    }

    /**
     * 回调接口 来电
     *
     * @param callId       来电ID
     * @param callType     来电类型
     * @param notification 来电通知
     */
    @Override
    public void onNewIncomingCall(final int callId, final int callType, final ILVIncomingNotification notification) {
//        Log.i("------strMsg-------",notification.getSender()+"/"+callId+"-"+notification+"--------");
//        addLogMessage("New Call from:"+notification.getSender()+"/"+callId+"-"+notification);
        if (null != mIncomingDlg) {  // 关闭遗留来电对话框
            mIncomingDlg.dismiss();
        }
        mCurIncomingId = callId;
        mIncomingDlg = new AlertDialog.Builder(this)
                .setTitle("New Call From " + notification.getSender())
                .setMessage(notification.getNotifDesc())
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sender = notification.getSponsorId();
                        acceptCall(callId, notification.getSponsorId(), callType);
//                        addLogMessage("Accept Call :"+mCurIncomingId);
                    }
                })
                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int ret = ILVCallManager.getInstance().rejectCall(mCurIncomingId);
//                        addLogMessage("Reject Call:"+ret+"/"+mCurIncomingId);
                    }
                })
                .create();
        mIncomingDlg.setCanceledOnTouchOutside(false);
        mIncomingDlg.show();
        addCallList(notification.getSender());
    }

    @Override
    public void onCallEstablish(int callId) {
//        addLogMessage("Call Establish :"+callId);
    }

    @Override
    public void onCallEnd(int callId, int endResult, String endInfo) {

        if (mCurIncomingId == callId) {
            mIncomingDlg.dismiss();
        }
        Log.i("-----notify----", endInfo + "------------" + callId);
//        addLogMessage("End Call:"+endResult+"-"+endInfo+"/"+callId);
    }

    @Override
    public void onException(int iExceptionId, int errCode, String errMsg) {
//        addLogMessage("Exception id:"+iExceptionId+", "+errCode+"-"+errMsg);
    }


    @Override
    public void accpt() {
        Log.e("ggggggggg", "----同意---");
        ILVCallManager.getInstance().endCall(0);
    }

    @Override
    public void reject() {
        Log.e("ggggggggg", "----拒绝---");
        ILVCallManager.getInstance().endCall(0);
        ILVCallManager.getInstance().endCall(0);
    }

    public boolean isGranted(String permission) {
        return !isMarshmallow() || isGranted_(permission);
    }

    private boolean isGranted_(String permission) {
        int checkSelfPermission = ActivityCompat.checkSelfPermission(this, permission);
        return checkSelfPermission == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    //shouldShowRequestPermissionRationale主要用于给用户一个申请权限的解释，该方法只有在用户在上一次已经拒绝过你的这个权限申请。也就是说，用户已经拒绝一次了，你又弹个授权框，你需要给用户一个解释，为什么要授权，则使用该方法。
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission Denied
                Toast.makeText(ContactActivity.this, "您没有授权该权限，请允许打开该授权", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
