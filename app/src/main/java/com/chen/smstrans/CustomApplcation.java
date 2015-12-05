package com.chen.smstrans;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;


import com.chen.smstrans.service.BackgroundBroadcastReceiver;
import com.chen.smstrans.service.SyncService;
import com.chen.smstrans.util.SharePreferenceUtil;



import cn.bmob.v3.datatype.BmobGeoPoint;

/**
 * Created by pc on 2015/7/26.
 */
public class CustomApplcation extends Application {

    public static final String PREFERENCE_NAME = "_sharedinfo";
    public static BmobGeoPoint lastPoint = null;// 上一次定位到的经纬度
    public final String PREF_LONGTITUDE = "longtitude";// 经度
    public static CustomApplcation mInstance;
    BackgroundBroadcastReceiver receiver;
    IntentFilter filter;
    NotificationManager mNotificationManager;
    SharePreferenceUtil mSpUtil;
    MediaPlayer mMediaPlayer;
    private String longtitude = "";

    @Override
    public void onCreate() {

        super.onCreate();
        // 是否开启debug模式--默认开启状态
        mInstance = this;
        init();

        filter = new IntentFilter(Intent.ACTION_TIME_TICK);
         receiver = new BackgroundBroadcastReceiver();
    }

    private void init() {
        mMediaPlayer = MediaPlayer.create(this, R.raw.notify);
        mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);

    }


    public  void stopService(){
        unregisterReceiver(receiver);
        Intent i = new Intent(this, SyncService.class);
        stopService(i);
    }

    public void startService(){
        registerReceiver(receiver, filter);
        Intent i = new Intent(this, SyncService.class);
        startService(i);
    }

    public static CustomApplcation getInstance() {
        return mInstance;
    }




    public NotificationManager getNotificationManager() {
        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        return mNotificationManager;
    }

    /**
     * 播放提示音
     * @return MediaPlayer
     */
    public synchronized MediaPlayer getMediaPlayer() {
        if (mMediaPlayer == null)
            mMediaPlayer = MediaPlayer.create(this, R.raw.notify);
        return mMediaPlayer;
    }


    /**
     * 退出登录,清空缓存数据
     */
    public void logout() {
    }
}
