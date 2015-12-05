package com.chen.smstrans.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chen.smstrans.R;
import com.chen.smstrans.config.Config;
import com.chen.smstrans.util.CommonUtils;
import com.chen.smstrans.util.MessageUtil;
import com.chen.smstrans.util.PushUtils;
import com.chen.smstrans.util.SharePreferenceUtil;

import org.w3c.dom.Text;

import cn.bmob.v3.BmobUser;

/**
 *
 */
public class AboutActivity extends Activity implements View.OnClickListener{
    private ActionBar mActionBar;
    private TextView actionbarTitle;
    private RelativeLayout backButton;

    private View feedbackEntrance;
    private View shareEntrance;
    private View websiteEntrance;
    private TextView versionTv;

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
        actionbarTitle.setText(R.string.about);
        backButton = (RelativeLayout) view.findViewById(R.id.back_button_container);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(this);
        mActionBar.setCustomView(view, new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private void setupMessageView() {

        setContentView(R.layout.about_activity);

        feedbackEntrance = findViewById(R.id.feedback_container);
        shareEntrance = findViewById(R.id.share_container);
        websiteEntrance = findViewById(R.id.website_container);
        versionTv = (TextView) findViewById(R.id.version);

        feedbackEntrance.setOnClickListener(this);
        shareEntrance.setOnClickListener(this);
        websiteEntrance.setOnClickListener(this);
        versionTv.setText(Config.version);

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.back_button_container://点击返回
                finish();
                break;

            case R.id.feedback_container:
                sendEmail();
                break;
            case R.id.share_container:
                CommonUtils.showToast(this,R.string.share_sorry);
                //不加break，跳转到网站
            case R.id.website_container:
                viewWebsite();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void sendEmail(){
        Intent intent = new Intent();
        intent.setData(Uri.parse("mailto:"));
        String[] tos = { "chen1234zheng1234@126.com" };

        intent.putExtra(Intent.EXTRA_EMAIL, tos); //收件者
        /*设置邮件的标题*/
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.mail_title));
        /*设置邮件的内容*/
        intent.putExtra(Intent.EXTRA_TEXT, "");
        //开始调用
        startActivity(intent);
    }

    public void viewWebsite(){
        Uri uri = Uri.parse(getResources().getString(R.string.website_address));
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);

    }
}
