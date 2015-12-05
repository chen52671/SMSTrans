package com.chen.smstrans.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chen.smstrans.R;
import com.chen.smstrans.bean.User;
import com.chen.smstrans.util.AccountUtils;
import com.chen.smstrans.util.CommonUtils;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.SaveListener;

/**
 *
 */
public class SignUpActivity extends Activity implements View.OnClickListener {
    private ActionBar mActionBar;
    private TextView actionbarTitle;
    private EditText mUserName;
    private EditText mUserEmail;
    private EditText mPassword;
    private Button mSignUpButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化界面
        setupSignUpView();
        setupActionBarView();


    }

    private void setupActionBarView() {
        mActionBar = getActionBar();
        View view = LayoutInflater.from(this).inflate(
                R.layout.login_action_bar, null);
        actionbarTitle = (TextView) view.findViewById(R.id.contact_edit_title);
        actionbarTitle.setText(R.string.sign_up_title);
        mActionBar.setCustomView(view, new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private void setupSignUpView() {
        setContentView(R.layout.activity_sign_up);
        mUserName = (EditText) findViewById(R.id.account_name);
        mUserEmail = (EditText) findViewById(R.id.account_email);
        mPassword = (EditText) findViewById(R.id.account_password);
        mSignUpButton = (Button) findViewById(R.id.sign_up);
        mSignUpButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击注册，需要验证邮箱拼写正确性和密码非空
            case R.id.sign_up:
                String userName = mUserName.getText().toString();
                String userEmail = mUserEmail.getText().toString();
                String password = mPassword.getText().toString();
                if (TextUtils.isEmpty(userName)||TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(password)) {
                    CommonUtils.showToast(this, getResources().getString(R.string.name_or_email_or_pw_empty));
                    break;
                }
                if (!CommonUtils.isValidAddress(userEmail)) {
                    CommonUtils.showToast(this, getResources().getString(R.string.invalid_address));
                    break;
                }

                if (!CommonUtils.isValidUserName(userName)) {
                    CommonUtils.showToast(this, getResources().getString(R.string.invalid_user_name));
                    break;
                }
                AccountUtils.signUp(this, userName, userEmail, password, null, true, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        CommonUtils.showToast(getApplicationContext(), getResources().getString(R.string.sign_up_success));
                        finish();
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        CommonUtils.showToast(getApplicationContext(), getResources().getString(R.string.sign_up_failed)+s);
                    }
                });
                break;
        }
    }
}
