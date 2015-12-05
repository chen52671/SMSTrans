package com.chen.smstrans.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chen.smstrans.R;
import com.chen.smstrans.bean.ShortMessage;
import com.chen.smstrans.db.DBHelper;
import com.chen.smstrans.ui.core.MessageLoader;
import com.chen.smstrans.util.MessageUtil;
import com.chen.smstrans.util.SharePreferenceUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import cn.bmob.v3.BmobUser;

/**
 *
 */
public class MessageDetailActivity extends Activity implements View.OnClickListener, LoaderManager.LoaderCallbacks<ShortMessage> {
    public static final String KEY_MESSAGE_OBJECT_ID = "key_message_id";

    private ActionBar mActionBar;
    private TextView actionbarTitle;
    private RelativeLayout backButton;
    private RelativeLayout callButton;
    private TextView messageTV;
    private TextView dateTV;
    private String messageObjectId;
    private ShortMessage shortMessage = null;
    Bundle bundle = new Bundle();
    private static final int LOADER_ID_MESSAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //通过intent拿到Message的详细数据，不再通过数据库
        Intent intent = getIntent();
        String messageObjectId = intent.getStringExtra(MessageActivity.MESSAGE_OBJECT_ID);
        setupMessageView();
        setupActionBarView();
        bundle.putString(KEY_MESSAGE_OBJECT_ID, messageObjectId);
        getLoaderManager().initLoader(LOADER_ID_MESSAGE, bundle, this).forceLoad();
    }


    private void setupActionBarView() {
        mActionBar = getActionBar();
        View view = LayoutInflater.from(this).inflate(
                R.layout.login_action_bar, null);
        actionbarTitle = (TextView) view.findViewById(R.id.contact_edit_title);
        backButton = (RelativeLayout) view.findViewById(R.id.back_button_container);
        callButton = (RelativeLayout) view.findViewById(R.id.action_settings_container);
        ImageView callIcon = (ImageView) view.findViewById(R.id.action_settings);
        callIcon.setImageResource(R.drawable.call_icon);
        callButton.setVisibility(View.VISIBLE);
        callButton.setOnClickListener(this);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(this);
        mActionBar.setCustomView(view, new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private void setupMessageView() {

        setContentView(R.layout.message_detail);
        findViewById(R.id.bottom_delete_button).setOnClickListener(this);
        findViewById(R.id.bottom_forward_button).setOnClickListener(this);
        findViewById(R.id.bottom_reply_button).setOnClickListener(this);
        messageTV = (TextView) findViewById(R.id.message_cotent);
        dateTV = (TextView) findViewById(R.id.message_date);
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.back_button_container:
                finish();
                break;
            case R.id.action_settings_container://拨号
                intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + shortMessage.getFromNumber()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.bottom_delete_button: //删除该条消息
                getContentResolver().delete(MessageUtil.DOWNLOAD_CONTENT_URI,
                        DBHelper.MessageTable.OBJECT_ID + "=?", new String[]{String.valueOf(shortMessage.getObjectId())});
                finish();
                break;
            case R.id.bottom_forward_button: //转发该短信
                intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", "", null));
                intent.putExtra("sms_body", shortMessage.getContent());
                startActivity(intent);
                break;
            case R.id.bottom_reply_button:
                intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", shortMessage.getFromNumber(), null));
                intent.putExtra("sms_body", shortMessage.getContent());
                startActivity(intent);
                break;


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public Loader<ShortMessage> onCreateLoader(int id, Bundle args) {
        //做一个自定义的AsyncTaskLoader，后台查询到该Message，并将将其置为已读，并更新服务器中的该信息的状态
        String mId = (String) args.get(KEY_MESSAGE_OBJECT_ID);
       /* return new CursorLoader(this, MessageUtil.DOWNLOAD_CONTENT_URI, DBHelper.CONTENT_PROJECTION,
                DBHelper.MessageTable.OBJECT_ID + "=" + messageObjectId, null, DBHelper.MessageTable.timeStamp + " DESC");*/
        return new MessageLoader(this, mId);
    }

    @Override
    public void onLoadFinished(Loader<ShortMessage> loader, ShortMessage data) {
        if (data != null) {
            shortMessage = data;
            refreshView(data);
        }
    }

    private void refreshView(ShortMessage data) {
        actionbarTitle.setText(data.getFromNumber());
        messageTV.setText(data.getContent());

        dateTV.setText(getFormatTime(data.getReceiveTime()));
    }

    private String getFormatTime(Long receiveTime) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(receiveTime);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(c.getTime());
    }

    @Override
    public void onLoaderReset(Loader<ShortMessage> loader) {

    }
}
