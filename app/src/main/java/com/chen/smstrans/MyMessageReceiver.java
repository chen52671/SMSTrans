package com.chen.smstrans;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;


import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;


import com.chen.smstrans.bean.ShortMessage;
import com.chen.smstrans.controller.MyHandler;
import com.chen.smstrans.db.DBHelper;
import com.chen.smstrans.service.SaveAndUploadThread;
import com.chen.smstrans.ui.MainActivity;
import com.chen.smstrans.ui.MessageActivity;
import com.chen.smstrans.ui.MessageDetailActivity;
import com.chen.smstrans.util.AccountUtils;
import com.chen.smstrans.util.CommonUtils;
import com.chen.smstrans.util.LogUtils;
import com.chen.smstrans.util.SharePreferenceUtil;

/**
 * 推送消息接收器
 *
 * @author smile
 * @ClassName: MyMessageReceiver
 * @Description:
 * @date 2014-5-30 下午4:01:13
 */
public class MyMessageReceiver extends BroadcastReceiver {
    private static String TAG = "MyMessageReceiver";
    // 事件监听
    public static ArrayList<EventListener> ehList = new ArrayList<EventListener>();
    private Context context;
    public static final int NOTIFY_ID = 0x000;
    public static int mNewNum = 0;//
    static Queue<SaveAndUploadThread> threadQueue = new ConcurrentLinkedQueue<SaveAndUploadThread>();
    public static Handler mHandler = new MyHandler(threadQueue);

    //如果你想发送自定义格式的消息，请使用sendJsonMessage方法来发送Json格式的字符串，然后你按照格式自己解析并处理

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (SharePreferenceUtil.getInstance(context).getDownloadSetting()) {
            String json = intent.getStringExtra("msg");
            LogUtils.d("接收到推送消息：" + json);
            //CommonUtils.showToast(context, json);


            String objectId;
            String usrId;
            String content;
            long receiveTime;
            String fromNumber;
            String result = "";
            ShortMessage message = new ShortMessage();
            try {
                JSONObject object = new JSONObject(json);
                result = object.getString("alert");
                object = new JSONObject(result);
                objectId = object.optString(ShortMessage.OBJECT_ID);
                usrId = object.optString(ShortMessage.USER_ID);
                content = object.getString(ShortMessage.CONTENT);
                receiveTime = object.getLong(ShortMessage.RECEIVE_TIME);
                fromNumber = object.getString(ShortMessage.FROM_NUMBER);

                // 根据usrID判断是不是发给自己的信息
                if (usrId.equals(AccountUtils.getCurrentUser(context).getObjectId())) {
                    //根据objectId去服务器查询对象
                    getMessageByObjectId(objectId);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getMessageByObjectId(String objectId) {
        BmobQuery<ShortMessage> query = new BmobQuery<ShortMessage>();
        query.getObject(context, objectId, new GetListener<ShortMessage>() {
            @Override
            public void onSuccess(ShortMessage message) {
                popupNotification(message);
                addMessageDownloadTaskToQueue(message);
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });


    }

    private void popupNotification(ShortMessage message) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent messageIntent = new Intent(context, MessageDetailActivity.class);
        messageIntent.putExtra(MessageActivity.MESSAGE_OBJECT_ID, message.getObjectId());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, messageIntent, PendingIntent.FLAG_ONE_SHOT);
        Notification notify;
        Notification.Builder notifyBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher) // 设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap
                        // icon)
                .setTicker(context.getResources().getString(R.string.notification_new_text) + message.getContent())// 设置在status
                        // bar上显示的提示文字
                .setContentTitle(message.getFromNumber())// 设置在下拉status
                        // bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
                .setContentText(message.getContent())// TextView中显示的详细内容
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL); // 关联PendingIntent
        //.setNumber(1); // 在TextView的右方显示的数字，可放大图片看，在最右侧。这个number同时也起到一个序列号的左右，如果多个触发多个通知（同一ID），可以指定显示哪一个。
        if (Build.VERSION.SDK_INT < 16) {
            notify = notifyBuilder.getNotification(); // 需要注意build()是在API level
        } else {
            notify = notifyBuilder.build();
        }
        nm.notify(1, notify);
    }

    public void addMessageDownloadTaskToQueue(ShortMessage newShortMessage) {
        threadQueue.add(new SaveAndUploadThread(context, newShortMessage, SaveAndUploadThread.DOWNLOAD_TASK, mHandler));
        Message message = Message.obtain();
        message.what = SaveAndUploadThread.SAVE_MESSAGE_DOWNLOAD_DB_BEGIN;
        mHandler.sendMessage(message);
    }

}
