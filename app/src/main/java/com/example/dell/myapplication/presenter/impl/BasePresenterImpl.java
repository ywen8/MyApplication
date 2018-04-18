package com.example.dell.myapplication.presenter.impl;

import com.example.dell.myapplication.presenter.BasePresenter;
import com.example.dell.myapplication.ui.view.BaseView;
import com.example.dell.myapplication.utils.RxUtils;

import rx.subscriptions.CompositeSubscription;

public class BasePresenterImpl  implements BasePresenter {

    protected CompositeSubscription mSubscriptions = new CompositeSubscription();
    @Override
    public void onCreate() {
        mSubscriptions= RxUtils.getNewCompositeSubIfUnsubscribed(mSubscriptions);
    }

    @Override
    public void onDestory() {
        mSubscriptions.unsubscribe();
    }
}
