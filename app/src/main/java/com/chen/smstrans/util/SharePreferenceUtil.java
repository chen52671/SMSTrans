package com.chen.smstrans.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 首选项管理
 *
 * @author smile
 * @ClassName: SharePreferenceUtil
 * @date 2014-6-10 下午4:20:14
 */
@SuppressLint("CommitPrefEdits")
public class SharePreferenceUtil {
    private volatile static SharePreferenceUtil mInstance;
    private SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor editor;
    private String commonSetting = "common_setting";


    private SharePreferenceUtil(Context context) {
        mSharedPreferences = context.getSharedPreferences(commonSetting, Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
    }

    public static synchronized SharePreferenceUtil getInstance(Context context) {
        if (mInstance == null) { // 首先判断是否已经创建实例，如果已经创建，直接返回，效率高
            synchronized (SharePreferenceUtil.class) {// 如果没有创建，然后再同步，并在同步块内再初始化。注意要再次判断是否已实例化
                if (mInstance == null) {
                    mInstance = new SharePreferenceUtil(context);
                }
            }
        }
        return mInstance;
    }

    private String SHARED_KEY_NOTIFY = "shared_key_notify";
    private String SHARED_KEY_UPLOAD = "shared_key_upload";
    private String SHARED_KEY_MESSAGE_READ_TIME = "shared_key_message_read_time";
    private String SHARED_KEY_MESSAGE_ID = "shared_key_message_id";
    private String SHARED_KEY_VOICE = "shared_key_sound";
    private String SHARED_KEY_VIBRATE = "shared_key_vibrate";
    private String SHARED_KEY_USER_NAME = "shared_user_name";
    private String SHARED_KEY_USER_EMAIL = "shared_user_email";
    private String SHARED_KEY_UPLOAD_SETTING = "shared_upload_setting";
    private String SHARED_KEY_DOWNLOAD_SETTING = "shared_download_setting";

    // 用户邮箱
    public String getUserEmail() {
        return mSharedPreferences.getString(SHARED_KEY_USER_EMAIL, "None");
    }

    public void setUserEmail(String email) {
        editor.putString(SHARED_KEY_USER_EMAIL, email);
        editor.commit();
    }

    // 用户名
    public String getUserName() {
        return mSharedPreferences.getString(SHARED_KEY_USER_NAME, "xx");
    }

    public void setUserName(String userName) {
        editor.putString(SHARED_KEY_USER_NAME, userName);
        editor.commit();
    }

    public boolean getUploadSetting() {
        return mSharedPreferences.getBoolean(SHARED_KEY_UPLOAD_SETTING, true);
    }

    public void setUploadSetting(boolean allowUpload) {
        editor.putBoolean(SHARED_KEY_UPLOAD_SETTING, allowUpload);
        editor.commit();
    }

    public boolean getDownloadSetting() {
        return mSharedPreferences.getBoolean(SHARED_KEY_DOWNLOAD_SETTING, true);
    }

    public void setDownloadSetting(boolean allowDownload) {
        editor.putBoolean(SHARED_KEY_DOWNLOAD_SETTING, allowDownload);
        editor.commit();
    }

    // 是否允许接收推送通知
    public boolean isAllowPushNotify() {
        return mSharedPreferences.getBoolean(SHARED_KEY_NOTIFY, true);
    }

    public void setPushNotifyEnable(boolean isChecked) {
        editor.putBoolean(SHARED_KEY_NOTIFY, isChecked);
        editor.commit();
    }

    // 上次读取短信数据库的时间
    public long getLastMessageReadTime() {
        return mSharedPreferences.getLong(SHARED_KEY_MESSAGE_READ_TIME, 0);
    }

    public void setLastMessageReadTime(long readTime) {
        editor.putLong(SHARED_KEY_MESSAGE_READ_TIME, readTime);
        editor.commit();
    }

    // 上次读取短信的ID
    public int getLastMessageId() {
        return mSharedPreferences.getInt(SHARED_KEY_MESSAGE_ID, 0);
    }

    public void setLastMessageId(int id) {
        editor.putInt(SHARED_KEY_MESSAGE_ID, id);
        editor.commit();
    }


    // 允许声音
    public boolean isAllowVoice() {
        return mSharedPreferences.getBoolean(SHARED_KEY_VOICE, true);
    }

    public void setAllowVoiceEnable(boolean isChecked) {
        editor.putBoolean(SHARED_KEY_VOICE, isChecked);
        editor.commit();
    }

    // 允许震动
    public boolean isAllowVibrate() {
        return mSharedPreferences.getBoolean(SHARED_KEY_VIBRATE, true);
    }

    public void setAllowVibrateEnable(boolean isChecked) {
        editor.putBoolean(SHARED_KEY_VIBRATE, isChecked);
        editor.commit();
    }

}
