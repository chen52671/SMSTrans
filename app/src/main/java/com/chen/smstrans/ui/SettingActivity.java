package com.chen.smstrans.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chen.smstrans.CustomApplcation;
import com.chen.smstrans.R;
import com.chen.smstrans.db.DBHelper;
import com.chen.smstrans.util.MessageUtil;
import com.chen.smstrans.util.PushUtils;
import com.chen.smstrans.util.SharePreferenceUtil;

import cn.bmob.v3.BmobUser;

/**
 *
 */
public class SettingActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private ActionBar mActionBar;
    private TextView actionbarTitle;
    private RelativeLayout backButton;
    private TextView userNameTV;
    private TextView userEmailTV;
    private CheckBox uploadSwitch;
    private CheckBox downloadSwitch;
    private TextView logOut;
    private View messageInServer;
    private View messageUploaded;
    private View aboutEntrance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化登陆界面
        setupMessageView();
        setupActionBarView();
    }


    private void setupActionBarView() {
        mActionBar = getActionBar();
        View view = LayoutInflater.from(this).inflate(
                R.layout.login_action_bar, null);
        actionbarTitle = (TextView) view.findViewById(R.id.contact_edit_title);
        actionbarTitle.setText(R.string.setting);
        backButton = (RelativeLayout) view.findViewById(R.id.back_button_container);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(this);
        mActionBar.setCustomView(view, new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private void setupMessageView() {

        setContentView(R.layout.setting_activity);
        userNameTV = (TextView) findViewById(R.id.account_name_text);
        userEmailTV = (TextView) findViewById(R.id.account_email_text);
        uploadSwitch = (CheckBox) findViewById(R.id.upload_switch);
        downloadSwitch = (CheckBox) findViewById(R.id.download_switch);
        logOut = (TextView) findViewById(R.id.user_logout);
        messageInServer = findViewById(R.id.server_message_location);
        messageUploaded = findViewById(R.id.message_uploaded);
        aboutEntrance = findViewById(R.id.about_container);

        uploadSwitch.setChecked(SharePreferenceUtil.getInstance(this).getUploadSetting());
        downloadSwitch.setChecked(SharePreferenceUtil.getInstance(this).getDownloadSetting());
        uploadSwitch.setOnCheckedChangeListener(this);
        downloadSwitch.setOnCheckedChangeListener(this);
        logOut.setOnClickListener(this);
        //TODO 暂时封闭服务器短信和上传短信的入口
        messageInServer.setVisibility(View.GONE);
        messageUploaded.setVisibility(View.GONE);
//        messageInServer.setOnClickListener(this);
//        messageUploaded.setOnClickListener(this);


        aboutEntrance.setOnClickListener(this);

        fillUserInfo();


    }

    private void fillUserInfo() {
        String userName;
        String userEmail;
        BmobUser bmobUser = BmobUser.getCurrentUser(this);
        if (bmobUser != null) {
            userName = bmobUser.getUsername();
            userEmail = bmobUser.getEmail();
        } else {
            userName = SharePreferenceUtil.getInstance(this).getUserName();
            userEmail = SharePreferenceUtil.getInstance(this).getUserEmail();
        }

        userNameTV.setText(userName);
        userEmailTV.setText(userEmail);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.back_button_container://点击返回
                finish();
                break;

            case R.id.user_logout://退出登录
                String subscribedChannel = BmobUser.getCurrentUser(this).getEmail();
                //

                PushUtils.getInstance(this).unsubscribe(subscribedChannel);
                //TODO 弹出Dialog
                BmobUser.logOut(this);
                //TODO 还需要做一些清理工作，比如关闭后台服务,取消推送订阅
                CustomApplcation.getInstance().stopService();
                //清除数据库
                getContentResolver().delete(MessageUtil.DOWNLOAD_CONTENT_URI, null, null);
                getContentResolver().delete(MessageUtil.UPLOAD_CONTENT_URI, null, null);
                //关闭界面
                setResult(MessageActivity.RESULT_LOG_OUT);
                finish();
                break;
            case R.id.server_message_location: //服务器端的信息 入口

                break;

            case R.id.message_uploaded: //本手机已上传信息 入口

                break;
            case R.id.about_container:
                startAboutActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.upload_switch:
                if (isChecked) {
                    // 打开上传
                    SharePreferenceUtil.getInstance(this).setUploadSetting(true);
                    CustomApplcation.getInstance().startService();
                } else {
                    // 关闭上传
                    SharePreferenceUtil.getInstance(this).setUploadSetting(false);
                    if (!SharePreferenceUtil.getInstance(this).getDownloadSetting()) {
                        CustomApplcation.getInstance().stopService();
                    }
                }

                break;
            case R.id.download_switch:
                if (isChecked) {
                    // 打开下载
                    SharePreferenceUtil.getInstance(this).setDownloadSetting(true);
                    CustomApplcation.getInstance().startService();
                } else {
                    //关闭下载
                    SharePreferenceUtil.getInstance(this).setDownloadSetting(false);
                    if (!SharePreferenceUtil.getInstance(this).getUploadSetting()) {
                        CustomApplcation.getInstance().stopService();
                    }

                }
                break;


            default:
                break;
        }
    }

    private void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
