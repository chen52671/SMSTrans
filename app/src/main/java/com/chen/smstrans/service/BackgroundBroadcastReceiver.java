package com.chen.smstrans.service;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.chen.smstrans.CustomApplcation;

/**
 * Created by pc on 2015/7/30.
 */
public class BackgroundBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /*保证后台Service不被杀掉*/
        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
            //检查Service状态
            boolean isServiceRunning = false;
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service :manager.getRunningServices(Integer.MAX_VALUE)) {
                if("com.chen.smstrans.service.SyncService".equals(service.service.getClassName()))
                {
                    isServiceRunning = true;
                }
            }
            if (!isServiceRunning) {
                Intent i = new Intent(context, SyncService.class);
                context.startService(i);
            }
        }
    }
}
