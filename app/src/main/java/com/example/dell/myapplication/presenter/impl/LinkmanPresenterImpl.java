package com.example.dell.myapplication.presenter.impl;

import android.content.Context;
import android.util.Log;

import com.example.dell.myapplication.presenter.LinkmanPresenter;
import com.example.dell.myapplication.ui.view.LinkmanView;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;

public class LinkmanPresenterImpl extends BasePresenterImpl implements LinkmanPresenter {
    private Context mContex;
    private LinkmanView  linkmanView;

    public LinkmanPresenterImpl(Context mContex, LinkmanView linkmanView) {
        this.mContex = mContex;
        this.linkmanView = linkmanView;
    }

    @Override
    public void loginout() {
        linkmanView.showProgress();
        Log.i("dddddddddddddd",ILiveLoginManager.getInstance().isLogin()+"");
        ILiveLoginManager.getInstance().iLiveLogout(new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                Log.i("dddddddddddddd",ILiveLoginManager.getInstance().isLogin()+"");
                linkmanView.hideProgress();
                linkmanView.toLogin();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                linkmanView.hideProgress();
                linkmanView.toLogin();
            }
        });
    }
}
