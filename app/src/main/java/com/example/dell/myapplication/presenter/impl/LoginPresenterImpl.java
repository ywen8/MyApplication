package com.example.dell.myapplication.presenter.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.dell.myapplication.application.MyApplication;
import com.example.dell.myapplication.presenter.LoginPresenter;
import com.example.dell.myapplication.ui.view.LoginView;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;

public class LoginPresenterImpl extends BasePresenterImpl implements LoginPresenter {

    private LoginView loginView;
    private Context mContext;

    public LoginPresenterImpl(LoginView loginView, Context mContext) {
        this.loginView = loginView;
        this.mContext = mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences=MyApplication.getPreferences();
        String username=sharedPreferences.getString("username","");
        String password=sharedPreferences.getString("password","");
        boolean isLogin=sharedPreferences.getBoolean("login",false);
        loginView.setUserNameWithPassWord(username,password);
        if(isLogin){
            login();
        }
    }

    @Override
    public void login() {
        if (loginView.getUserName().length() == 0 || loginView.getUserName().equals("")) {
            Toast.makeText(mContext, "帐号不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (loginView.getPassWord().length() == 0 || loginView.getPassWord().equals("")) {
            Toast.makeText(mContext, "密码不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        loginView.showProgress();
        ILiveLoginManager.getInstance().tlsLogin(loginView.getUserName(), loginView.getPassWord(), new ILiveCallBack<String>() {
            @Override
            public void onSuccess(String data) {
                loginSDK(loginView.getUserName(), data);

            }
            @Override
            public void onError(String module, int errCode, String errMsg) {
                loginView.hideProgress();
            }
        });

    }

    private void loginSDK(final String id, String userSig){
        ILiveLoginManager.getInstance().iLiveLogin(id, userSig, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                loginView.hideProgress();
                Toast.makeText(mContext, "Login Success:", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor=MyApplication.getPreferences().edit();
                editor.putString("username",loginView.getUserName());
                editor.putString("password",loginView.getPassWord());
                editor.putBoolean("login",true);
                editor.commit();
                loginView.toMain();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                loginView.hideProgress();
                Toast.makeText(mContext, "Login failed:"+module+"|"+errCode+"|"+errMsg, Toast.LENGTH_SHORT).show();

            }
        });
    }
}
