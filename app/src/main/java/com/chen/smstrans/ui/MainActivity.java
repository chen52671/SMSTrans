package com.chen.smstrans.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chen.smstrans.R;
import com.chen.smstrans.bean.User;
import com.chen.smstrans.config.Config;
import com.chen.smstrans.service.SyncService;
import com.chen.smstrans.util.AccountUtils;
import com.chen.smstrans.util.CommonUtils;
import com.chen.smstrans.util.LogUtils;
import com.chen.smstrans.util.PushUtils;
import com.chen.smstrans.util.SharePreferenceUtil;

import java.util.List;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 入口Activity，也就是登陆页面，如果判断已登录，则进入Listview
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private ActionBar mActionBar;
    private TextView actionbarTitle;
    private EditText mUserEmail;
    private EditText mPassword;
    private Button mSignInButton;
    private TextView mSignUp;
    private List<String> suscribedChannels;
    private String installationObjectID;
    private static final int SIGN_UP_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*初始化Bmob云服务*/
        Bmob.initialize(getApplicationContext(), Config.applicationId);

        //判断登陆情况，如果已保存有旧的账户，并且验证通过后，则直接载入主页面
        tryLogin();
        //初始化登陆界面
        setupLoginView();
        setupActionBarView();

/*        //启动后台同步服务
        Intent i = new Intent(this, SyncService.class);
        startService(i);*/

    }

    private void initPushService() {
        BmobInstallation installation = BmobInstallation.getCurrentInstallation(this);
        installation.save();

        BmobQuery<BmobInstallation> query = new BmobQuery<BmobInstallation>();
        query.addWhereEqualTo("installationId", installation.getInstallationId());
        query.findObjects(this, new FindListener<BmobInstallation>() {

                    @Override
                    public void onSuccess(List<BmobInstallation> list) {
                        if (list.size() > 0) {
                            BmobInstallation installation1 = (BmobInstallation) list.get(0);
                            suscribedChannels = installation1.getChannels();
                            installationObjectID = installation1.getObjectId();
                            PushUtils.getInstance(MainActivity.this).unsubscribe(suscribedChannels);
                        }
                    }

                    @Override
                    public void onError(int i, String s) {

                    }
                }

        );
    }

    private void tryLogin() {
        BmobUser bmobUser = BmobUser.getCurrentUser(this);
        if (bmobUser != null) {
            //转入主页面
            startMessageActivity();
            finish();
        } else {
            //进入注册界面
            //首先取消所有的订阅
            //初始化推送服务
            initPushService();
            return;
        }
    }

    private void setupActionBarView() {
        mActionBar = getActionBar();
        View view = LayoutInflater.from(this).inflate(
                R.layout.login_action_bar, null);
        actionbarTitle = (TextView) view.findViewById(R.id.contact_edit_title);
        mActionBar.setCustomView(view, new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private void setupLoginView() {
        setContentView(R.layout.activity_main);
        mUserEmail = (EditText) findViewById(R.id.account_email);
        mPassword = (EditText) findViewById(R.id.account_password);
        mSignInButton = (Button) findViewById(R.id.sign_in);
        mSignUp = (TextView) findViewById(R.id.sign_up_link);

        mSignInButton.setOnClickListener(this);
        mSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击登录，需要验证邮箱拼写正确性和密码非空
            case R.id.sign_in:
                final String userEmail = mUserEmail.getText().toString();
                String password = mPassword.getText().toString();
                if (TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(password)) {
                    CommonUtils.showToast(this, getResources().getString(R.string.user_or_pw_empty));
                    break;
                }
                if (!CommonUtils.isValidAddress(userEmail)) {
                    CommonUtils.showToast(this, getResources().getString(R.string.invalid_address));
                    break;
                }
                //TODO 将来设计为可邮箱登陆，也可以用户名登陆

                AccountUtils.loginByEmailPwd(this, userEmail, password, new LogInListener<User>() {
                    @Override
                    public void done(User user, BmobException e) {
                        if (user != null) {
                            subscribeByUpdate(userEmail);
                            saveUserInfo();
                            startMessageActivity();
                            CommonUtils.showToast(getApplicationContext(), getResources().getString(R.string.login_success));
                        } else {
                            CommonUtils.showToast(getApplicationContext(), getResources().getString(R.string.login_fail) + e.getMessage());
                        }
                    }
                });

                break;
            case R.id.sign_up_link:
                Intent intent = new Intent(this, SignUpActivity.class);
                startActivityForResult(intent, SIGN_UP_CODE);
                break;
        }
    }

    private void subscribeByUpdate(final String userEmail) {
        BmobInstallation installation = new BmobInstallation(MainActivity.this);
        installation.subscribe(userEmail);
        installation.update(MainActivity.this, installationObjectID, new UpdateListener() {

            @Override
            public void onSuccess() {
                LogUtils.d("installation 订阅成功");
            }

            @Override
            public void onFailure(int code, String msg) {
                LogUtils.d("installation 订阅失败");
                PushUtils.getInstance(MainActivity.this).subscribe(userEmail);
            }
        });

    }

    private void saveUserInfo() {
        BmobUser bmobUser = BmobUser.getCurrentUser(this);
        if (bmobUser != null) {
            SharePreferenceUtil.getInstance(this).setUserName(bmobUser.getUsername());
            SharePreferenceUtil.getInstance(this).setUserEmail(bmobUser.getEmail());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startMessageActivity() {
        Intent intent = new Intent(this, MessageActivity.class);
        startActivity(intent);
        finish();
    }
}
