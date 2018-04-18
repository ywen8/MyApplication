package com.example.dell.myapplication.ui.view;

public interface LoginView  extends BaseView{
    String getUserName();

    String getPassWord();

    void showPassWord();

    void hidePassWord();

    void toMain();

    void setUserNameWithPassWord(String user,String password);

    void tostart();

}
