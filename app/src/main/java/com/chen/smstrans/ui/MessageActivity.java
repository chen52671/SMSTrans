package com.chen.smstrans.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chen.smstrans.CustomApplcation;
import com.chen.smstrans.R;
import com.chen.smstrans.adapter.MessageAdapter;
import com.chen.smstrans.config.Config;
import com.chen.smstrans.controller.MessageController;
import com.chen.smstrans.db.DBHelper;
import com.chen.smstrans.util.CommonUtils;
import com.chen.smstrans.util.LogUtils;
import com.chen.smstrans.util.MessageUtil;
import com.chen.smstrans.util.SharePreferenceUtil;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.BmobUser;

/**
 *
 */
public class MessageActivity extends Activity implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    //TODO 抽出一个Fragment来，然后可以给上传的短信，服务器短信这块复用。
    public static final int CODE_SETTING = 11;
    public static final int CODE_MESSAGE_DETAIL = 12;
    public static final int RESULT_LOG_OUT = 22;

    public static final String MESSAGE_OBJECT_ID = "message_object_id";

    public static final String BUNDLE_MESSAGE_TYPE = "message_type";

    public static final int MESSAGE_TYPE_DOWNLOAD = 1;
    public static final int MESSAGE_TYPE_UPLOAD = 2;
    public static final int MESSAGE_TYPE_SERVER = 3;

    private int messageType;
    private ActionBar mActionBar;
    private TextView mSelectTitle;
    private TextView actionbarTitle;
    private RelativeLayout settingButton;
    private ListView messageListView;
    private MessageAdapter messageAdapter;
    private static final int LOADER_ID_UPLOAD_MESSAGE = 1;
    private static final int LOADER_ID_DOWNLOAD_MESSAGE = 2;
    Bundle bundle = new Bundle();
    private MessageController mController;
    private ActionMode mActionMode;
    private CheckBox mSelectButton;
    private LinearLayout mBottomBtnPanel;
    private boolean isReturn = false;
    View searchButton;
    View selectButton;
    View readButton;
    View deleteButton;

    private ContentObserver mDbChangeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean paramBoolean) {
            LogUtils.d("更新UI");
            getLoaderManager().restartLoader(LOADER_ID_UPLOAD_MESSAGE, bundle, MessageActivity.this);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if(SharePreferenceUtil.getInstance(this).getDownloadSetting() && SharePreferenceUtil.getInstance(this).getUploadSetting()){
            CustomApplcation.getInstance().startService();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        messageType = i.getIntExtra(BUNDLE_MESSAGE_TYPE, MESSAGE_TYPE_DOWNLOAD);

        mController = new MessageController(this);
        //TODO 把Type分为本地上传，下载，服务器三类
        mController.setMessageType(messageType);

        ensureUser();
        //初始化登陆界面
        setupMessageView();
        setupActionBarView();
        getLoaderManager().initLoader(LOADER_ID_UPLOAD_MESSAGE, bundle, this);
        initPush();
        getContentResolver().registerContentObserver(MessageUtil.DOWNLOAD_CONTENT_URI, true, mDbChangeObserver);
    }

    private void ensureUser() {
        BmobUser currentUser = BmobUser.getCurrentUser(this);
        if (currentUser == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initPush() {
        BmobPush.startWork(this, Config.applicationId);
    }


    private void setupActionBarView() {
        mActionBar = getActionBar();
        View view = LayoutInflater.from(this).inflate(
                R.layout.login_action_bar, null);
        actionbarTitle = (TextView) view.findViewById(R.id.contact_edit_title);
        actionbarTitle.setText(R.string.message_list);
        settingButton = (RelativeLayout) view.findViewById(R.id.action_settings_container);
        settingButton.setVisibility(View.VISIBLE);
        settingButton.setOnClickListener(this);
        mActionBar.setCustomView(view, new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private void setupMessageView() {
        setContentView(R.layout.message_activity);
        searchButton = findViewById(R.id.bottom_search_button);
        selectButton = findViewById(R.id.bottom_select_button);
        readButton = findViewById(R.id.bottom_read_button);
        deleteButton = findViewById(R.id.bottom_delete_button);

        messageListView = (ListView) findViewById(R.id.message_listview);
        searchButton.setOnClickListener(this);
        selectButton.setOnClickListener(this);
        readButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        mBottomBtnPanel = (LinearLayout) findViewById(R.id.buttonPanel);

        messageListView.setOnItemClickListener(this);
        messageListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                if (mController.getMode() == MessageController.MODE_NORMAL) {
                    mController.onItemClick(position, id);
                    mActionMode = startActionMode(mChoiceModeListener);
                    customizeActionModeCloseButton();
                    mController.getAdapter().notifyDataSetChanged();
                }
                return true;
            }

        });
        messageListView.setClickable(true);
        mController.setListView(messageListView);

        setBottomBarCabMode(false);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //点击进入设置Activity
            case R.id.action_settings_container:
                gotoSettingActivity();
                break;
            /*TODO 点击搜索*/
            case R.id.bottom_search_button:
                break;

            //点击进入ActionMode, 信息多选
            case R.id.bottom_select_button:
                if(messageListView.getAdapter()!=null){
                    mActionMode = startActionMode(mChoiceModeListener);
                    customizeActionModeCloseButton();
                    mController.getAdapter().notifyDataSetChanged();
                }
                break;
            case R.id.bottom_read_button:
                mController.changeSelectedItemsReadStatus();
                break;
            case R.id.bottom_delete_button:
                mController.deleteSelectedItems();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_SETTING) {
            switch (resultCode) {
                case RESULT_LOG_OUT:
                    ensureUser();
                    break;
            }
        }

    }

    private void gotoSettingActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, CODE_SETTING);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //TODO 暂时获取所有上传文件夹的Message
        return new CursorLoader(this, MessageUtil.DOWNLOAD_CONTENT_URI, DBHelper.CONTENT_PROJECTION,
                null, null, DBHelper.MessageTable.TIME_STAMP + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor cursor = data;
        if (cursor != null && cursor.moveToFirst()) {
            showBottomBtn(true);
            if (messageAdapter == null) {
                messageAdapter = new MessageAdapter(this, cursor);
                messageListView.setAdapter(messageAdapter);
                mController.setAdapter(messageAdapter);
                messageAdapter.setController(mController);

            } else {
                messageAdapter.swapCursor(cursor);
                messageAdapter.notifyDataSetChanged();
            }
        } else {
            showBottomBtn(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (messageAdapter != null) {
            messageAdapter.swapCursor(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) messageListView.getAdapter().getItem(position);
        String messageObjectId = cursor.getString(DBHelper.INDEX_OBJECT_ID);

        if (mController.getMode() == MessageController.MODE_NORMAL) {

            Intent intent = new Intent(this, MessageDetailActivity.class);
            intent.putExtra(MESSAGE_OBJECT_ID, messageObjectId);
            startActivityForResult(intent, CODE_MESSAGE_DETAIL);
        } else {
            mController.onItemClick(position,id);
            messageAdapter.notifyDataSetChanged();
        }

    }

    AbsListView.MultiChoiceModeListener mChoiceModeListener = new AbsListView.MultiChoiceModeListener() {

        @Override
        public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mController.setMode(MessageController.MODE_NORMAL);
            mController.clearCheckedItem();

            mController.getAdapter().notifyDataSetChanged();
            setBottomBarCabMode(false);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            mController.setMode(MessageController.MODE_CAB);

            enterCAB();
            mController.getAdapter().notifyDataSetChanged();
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            /*下拉式菜单，不响应*/
            return false;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int pos, long id, boolean checked) {
        }
    };

    private void setBottomBarCabMode(boolean cabMode) {
        //TODO 暂时封闭搜素入口
        if (cabMode) {
            searchButton.setVisibility(View.GONE);
            selectButton.setVisibility(View.GONE);
            readButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            mBottomBtnPanel.setBackgroundResource(R.drawable.multi_select_action_bar_bac);
        } else {
            searchButton.setVisibility(View.GONE);
            selectButton.setVisibility(View.VISIBLE);
            readButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            mBottomBtnPanel.setBackgroundResource(R.color.white);
        }

    }

    /**
     * 自定义ActionMode的Actionbar的View
     */
    private void customizeActionModeCloseButton() {
        int buttonId = Resources.getSystem().getIdentifier("action_mode_close_button", "id",
                "android");
        View v = findViewById(buttonId);
        if (v == null)
            return;
        LinearLayout action_mode_close_button = (LinearLayout) v;

        if (action_mode_close_button != null) {
            action_mode_close_button.removeAllViews();
            action_mode_close_button.setClickable(false);
            action_mode_close_button.setOrientation(LinearLayout.HORIZONTAL);
            ViewGroup.LayoutParams lp = action_mode_close_button.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            action_mode_close_button.setLayoutParams(lp);
            action_mode_close_button.setBackground(null);
            View actionMode = (View) mActionMode.getCustomView().getParent();
            actionMode.setBackgroundResource(R.drawable.multi_select_action_bar_bac);

            View.OnClickListener listener = new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (mActionMode != null) {
                        mActionMode.finish();
                    }
                }
            };

            ImageView iv = new ImageView(this);
            iv.setImageDrawable(this.getResources().getDrawable(
                    R.drawable.multi_select_back_btn));
            iv.setBackgroundResource(R.drawable.common_btn_background);
            iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setVisibility(View.VISIBLE);
            iv.setOnClickListener(listener);
            action_mode_close_button.addView(iv);

            TextView tv = new TextView(this);
            tv.setText(CommonUtils.formatPlural(this, R.plurals.num_selected, mController.getCheckedItemSize()));
            tv.setTextColor(this.getResources().getColor(
                    R.color.white_list_image_char_color));
            tv.setGravity(Gravity.CENTER);
            tv.setOnClickListener(listener);
            tv.setTextSize(16);
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            tv.setPadding(0, 0, 35, 0);
            mSelectTitle = tv;
            action_mode_close_button.addView(tv);
            action_mode_close_button.setPadding(0, 0, 0, 0);
        }
    }

    /**
     * Update action bar and bottom panel
     */
    private void enterCAB() {
        initActionMode();
        updateCABTitles();
        customizeActionModeCloseButton();
        setBottomBarCabMode(true);
        enableBottomBtn(0 != mController.getCheckedItemSize());
        updateBottomBtnMode();
    }

    /**
     * 根据选中的Item的状态，来决定下边栏按钮的样式和动作
     */
    public void updateBottomBtnMode() {
        enableBottomBtn(0 != mController.getCheckedItemSize());
        mController.refreshBottomBtnMode();
    }

    @SuppressLint("InflateParams")
    private void initActionMode() {
        if (mActionMode == null)
            return;

        final LayoutInflater inflater = LayoutInflater.from(this);
        View ll = inflater.inflate(R.layout.actionmode_view, null);
        ViewGroup.LayoutParams alp = ll.getLayoutParams();
        if (alp == null) {
            alp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            alp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            alp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        ll.setLayoutParams(alp);
        mActionMode.setCustomView(ll);
        mSelectButton = (CheckBox) ll.findViewById(R.id.select_btn);
        if (mSelectButton != null) {
            mSelectButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mController.onActionBarChecked();
                    mController.getAdapter().notifyDataSetChanged();
                    updateCABTitles();
                    enableBottomBtn(0 != mController.getCheckedItemSize());
                    updateBottomBtnMode();
                }
            });
        }
    }

    /**
     * 只有在有Item被选中的情况下，使能下边栏的按键
     *
     * @param enableAll
     */
    private void enableBottomBtn(boolean enableAll) {
        if (mBottomBtnPanel == null) {
            return;
        }
        for (int i = 0; i < mBottomBtnPanel.getChildCount(); i++) {
            if (mBottomBtnPanel.getChildAt(i).getVisibility() == View.VISIBLE) {
                mBottomBtnPanel.getChildAt(i).setEnabled(enableAll);
            }
        }
    }

    private void showBottomBtn(boolean enableAll) {
        if (mBottomBtnPanel == null) {
            return;
        }
        mBottomBtnPanel.setVisibility(enableAll?View.VISIBLE:View.GONE);
    }
    /**
     * Update the title for contextual action bar.
     */
    public void updateCABTitles() {
        if (mSelectTitle == null) {
            mActionMode.setTitle(CommonUtils.formatPlural(this, R.plurals.num_selected,
                    mController.getCheckedItemSize()));
        } else {
            mSelectTitle.setText(CommonUtils.formatPlural(this, R.plurals.num_selected, mController.getCheckedItemSize()));
        }

        mSelectButton.setChecked(mController.isAllItemSelected());
        mSelectButton.setEnabled(0 != messageListView.getCount());
    }



    public void finishActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }

    }
}
