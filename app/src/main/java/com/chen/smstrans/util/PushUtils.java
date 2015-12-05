package com.chen.smstrans.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;

/**
 * 推送工具类
 */
public class PushUtils {
    private  BmobPushManager<BmobInstallation> bmobPush;
    private static PushUtils instance = null;

    private static Context context;

    private PushUtils(Context context) {
        PushUtils.context = context;
        bmobPush = new BmobPushManager<BmobInstallation>(context);
    }

    public static PushUtils getInstance(Context context){
        if (instance == null) { // 首先判断是否已经创建实例，如果已经创建，直接返回，效率高
            synchronized (PushUtils.class) {// 如果没有创建，然后再同步，并在同步块内再初始化。注意要再次判断是否已实例化
                if (instance == null) {
                    instance = new PushUtils(context);
                }
            }
        }
        return instance;
    }
    /**
     * 订阅
     */
    public  void subscribe(String channel){
        BmobInstallation installation = BmobInstallation.getCurrentInstallation(context);
        installation.subscribe(channel);
        installation.save();
    }
    /**
     * 退订
     */
    public  void unsubscribe(List<String> channels){
        if(channels!=null &&channels.size()>0){
            BmobInstallation installation2 = BmobInstallation.getCurrentInstallation(context);
            for (String ch :channels){
                LogUtils.d("之前该设备上的订阅"+ch);
                installation2.unsubscribe(ch);
            }
            installation2.save();
        }
    }

    /**
     * 退订
     */
    public  void unsubscribe(String channel){
        BmobInstallation installation2 = BmobInstallation.getCurrentInstallation(context);
        installation2.unsubscribe(channel);
        installation2.save();
    }
    /**
     * 向所有终端推送
     */
    public  void pushMessageToAll(String message){
        bmobPush.pushMessageAll(message);
    }
    /**
     * 给指定Android用户推送消息
     * @param message
     * @param installId
     */
    public  void pushAndroidMessage(String message, String installId){
//		bmobPush.pushMessage(message, installId);
        BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
        query.addWhereEqualTo("installationId", installId);
        bmobPush.setQuery(query);
        bmobPush.pushMessage(message);
    }

    /**
     * 给指定IOS用户推送
     * @param message
     * @param deviceToken
     */
    public  void pushIOSMessage(String message, String deviceToken){
        BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
        query.addWhereEqualTo("deviceToken", deviceToken);
        bmobPush.setQuery(query);
        bmobPush.pushMessage(message);
    }

    /**
     * 给指定渠道推送消息
     * @param message
     * @param channel
     */
    public  void pushChannelMessage(String message, String channel){
        BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
        List<String> channels = new ArrayList<String>();
        channels.add(channel);
        query.addWhereContainedIn("channels", channels);
        bmobPush.setQuery(query);
        bmobPush.pushMessage(message);
    }


    /**
     * 给指定用户推送消息
     * @param message
     * @param channel
     */
    public  void pushUsrMessage(String message, String user){
        BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
        query.addWhereEqualTo("usrEmail", user);
        bmobPush.setQuery(query);
        bmobPush.pushMessage(message);
    }

    /**
     * 给android平台终端推送
     * @param message
     */
    public  void pushToAndroid(String message){
        BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
        query.addWhereEqualTo("deviceType", "android");
        bmobPush.setQuery(query);
        bmobPush.pushMessage(message);
    }

    /**
     * 给ios平台终端推送
     * @param message
     */
    public  void pushToIOS(String message){
        BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
        query.addWhereEqualTo("deviceType", "ios");
        bmobPush.setQuery(query);
        bmobPush.pushMessage(message);
    }
}
