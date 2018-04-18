package com.example.dell.myapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telecom.Call;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.myapplication.R;
import com.example.dell.myapplication.entity.LoginStatus;
import com.example.dell.myapplication.presenter.LoginPresenter;
import com.example.dell.myapplication.presenter.impl.LoginPresenterImpl;
import com.example.dell.myapplication.service.CallService;
import com.example.dell.myapplication.ui.view.LoginView;
import com.example.dell.myapplication.utils.RxBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements LoginView {
    @BindView(R.id.act_login_username_edit)
    EditText login_usrname;
    @BindView(R.id.act_login_passwrod_edit)
    EditText login_pasword;
    @BindView(R.id.act_login_password_img_eye)
    ImageView login_password_eye;
    @BindView(R.id.act_login_progress_line)
    LinearLayout login_progress_line;
    @BindView(R.id.act_login_content_real)
    RelativeLayout login_content_real;
    private LoginPresenter presenter;

    private boolean isShow = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getSupportActionBar()) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loginStatus();
        presenter=new LoginPresenterImpl(this,this);
        presenter.onCreate();
    }

    @OnClick(R.id.act_login_password_img_eye)
    void showEye() {
        if(!isShow){
            showPassWord();
        }else{
            hidePassWord();
        }
        isShow=!isShow;
    }

    @OnClick(R.id.act_login_on)
    void login(){
        presenter.login();
    }

    @Override
    public String getUserName() {
        return login_usrname.getText().toString().trim();
    }

    @Override
    public String getPassWord() {
        return login_pasword.getText().toString().trim();
    }

    @Override
    public void showProgress() {
        login_content_real.setVisibility(View.GONE);
        login_progress_line.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        login_content_real.setVisibility(View.VISIBLE);
        login_progress_line.setVisibility(View.GONE);
    }

    @Override
    public void showPassWord() {
        login_password_eye.setImageResource(R.mipmap.eye_check);
        String password = getPassWord();
        login_pasword.setInputType(InputType.TYPE_NULL);
        login_pasword.setText(password);
    }

    @Override
    public void hidePassWord() {
        login_password_eye.setImageResource(R.mipmap.eye);
        String password = getPassWord();
        login_pasword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        login_pasword.setText(password);
        login_pasword.setSelection(password.length());

    }

    @Override
    public void toMain() {
        Intent intent=new Intent(this,LinkmanActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void setUserNameWithPassWord(String user, String password) {
        login_usrname.setText(user);
        login_usrname.setSelection(user.length());
        login_pasword.setText(password);
        login_pasword.setSelection(password.length());
    }

    @Override
    public void tostart() {
        Intent intent=new Intent(this, CallService.class);
        startService(intent);
    }

    private void loginStatus(){
        RxBus.getInstance().toObserverable(LoginStatus.class).subscribe(new Action1<LoginStatus>() {
            @Override
            public void call(LoginStatus loginStatus) {
                if(loginStatus instanceof LoginStatus){
                    if(loginStatus.status==1){
                        Toast.makeText(MainActivity.this, "Login Success:", Toast.LENGTH_SHORT).show();
                        toMain();
                    }else{
                        Toast.makeText(MainActivity.this, "Login failed:", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

}
