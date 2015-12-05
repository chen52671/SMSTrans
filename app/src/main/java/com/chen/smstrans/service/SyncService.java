package com.chen.smstrans.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Telephony.Sms;

import com.chen.smstrans.R;
import com.chen.smstrans.bean.ShortMessage;
import com.chen.smstrans.controller.MyHandler;
import com.chen.smstrans.ui.MainActivity;
import com.chen.smstrans.util.AccountUtils;
import com.chen.smstrans.util.LogUtils;
import com.chen.smstrans.util.SharePreferenceUtil;

import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 1,监听短息数据库Inbox的变化，获取收到的短信
 * 2，接收网络上推送的短信
 * 3，设置为开机自启动(设置界面关闭)和后台自启动(账号退出关闭)
 */
public class SyncService extends Service {
    private static String TAG = "SyncService";


    private static int threadFinishedCount = 0;//已完成任务的数量

    private int messageCount = 0;
    static Queue<SaveAndUploadThread> threadQueue = new ConcurrentLinkedQueue<SaveAndUploadThread>();
    public static Handler mHandler = new MyHandler(threadQueue);
    static final String[] SMS_PROJECTION = new String[]{
            Sms._ID, // 0
            Sms.TYPE, // 1
            Sms.ADDRESS,
            Sms.DATE,
            Sms.BODY

    };
    public static Uri smsUri = Uri.parse("content://sms");
    /**
     * 短信的type可以有一下类型
     * public static final int MESSAGE_TYPE_ALL    = 0;
     * public static final int MESSAGE_TYPE_INBOX  = 1;
     * public static final int MESSAGE_TYPE_SENT   = 2;
     * public static final int MESSAGE_TYPE_DRAFT  = 3;
     * public static final int MESSAGE_TYPE_OUTBOX = 4;
     * public static final int MESSAGE_TYPE_FAILED = 5; // for failed outgoing messages
     * public static final int MESSAGE_TYPE_QUEUED = 6; // for messages to send later
     */
    //这里使用sms的ID作为短信的唯一标识，value值为Message对象。
    private Hashtable<Long, ShortMessage> smsTable = new Hashtable<Long, ShortMessage>();

    private ContentObserver mmSmsDbChangeObserver = new ContentObserver(mHandler) {
        public void onChange(boolean paramBoolean) {
            if (SharePreferenceUtil.getInstance(SyncService.this).getUploadSetting()) {
                messageSaveAndSend();
            }
        }

        private void messageSaveAndSend() {
            //获取上次更新的时间
            SharePreferenceUtil sharePreference = SharePreferenceUtil.getInstance(SyncService.this);
            int lastId = sharePreference.getLastMessageId();
            //获取按照时间由近及远的收件箱短信
            String where = Sms.TYPE + "=" + Sms.MESSAGE_TYPE_INBOX + " AND " +
                    Sms._ID + ">" + lastId;
            Cursor c = getContentResolver().query(smsUri, SMS_PROJECTION, where, null,
                    "date DESC");
            if (c != null && c.moveToFirst()) {
                do {
                    if (c.isFirst()) {
                        //记录最近一个更新的ID
                        sharePreference.setLastMessageId(c.getInt(c.getColumnIndex(Sms._ID)));
                    }
                    if (messageCount >= 2) {
                        messageCount = 0;
                        break;
                    }
                    messageCount++;
                    ShortMessage newShortMessage = new ShortMessage();
                    newShortMessage.setUserId(AccountUtils.getCurrentUser(SyncService.this).getObjectId());
                    newShortMessage.setFromNumber(c.getString(c.getColumnIndex(Sms.ADDRESS)));
                    newShortMessage.setContent(c.getString(c.getColumnIndex(Sms.BODY)));
                    newShortMessage.setReceiveTime(c.getLong(c.getColumnIndex(Sms.DATE)));
                    LogUtils.d(newShortMessage.getContent());
                    //TODO newShortMessage.setUserId();
                    //TODO 1,Save,2,Update 做一个保存上传队列
                    addMessageUploadTaskToQueue(newShortMessage);
                } while (c.moveToNext());
            }

            closeCursor(c);
        }
    };

    private void addMessageUploadTaskToQueue(ShortMessage newShortMessage) {
        //1,添加到任务队列，通过Handler发送出去
        //2,接收到Message，让thread来处理任务
        threadQueue.add(new SaveAndUploadThread(this, newShortMessage, SaveAndUploadThread.UPLOAD_TASK, mHandler));
        Message message = Message.obtain();
        message.what = SaveAndUploadThread.SAVE_AND_UPLOAD_THREAD_BEGIN;
        mHandler.sendMessage(message);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // if (Build.VERSION.SDK_INT < 18) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        Notification notify;
        Notification.Builder notifyBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher) // 设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap
                        // icon)
                .setTicker(getResources().getString(R.string.service_notification_text))// 设置在status
                        // bar上显示的提示文字
                .setContentTitle(getResources().getString(R.string.service_notification_title))// 设置在下拉status
                        // bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
                .setContentText(getResources().getString(R.string.service_notification_detail))// TextView中显示的详细内容
                .setContentIntent(pendingIntent); // 关联PendingIntent
        //.setNumber(1); // 在TextView的右方显示的数字，可放大图片看，在最右侧。这个number同时也起到一个序列号的左右，如果多个触发多个通知（同一ID），可以指定显示哪一个。
        if (Build.VERSION.SDK_INT < 16) {
            notify = notifyBuilder.getNotification(); // 需要注意build()是在API level
        } else {
            notify = notifyBuilder.build();
        }
        startForeground(1120, notify);
        //  }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d("SyncSeevice", "SyncService create");
        getContentResolver().registerContentObserver(smsUri, true, mmSmsDbChangeObserver);
    }

    @Override
    public void onDestroy() {
        /*Intent intent = new Intent(this, SyncService.class);
        this.startService(intent);*/
        super.onDestroy();
        //getContentResolver().unregisterContentObserver(mmSmsDbChangeObserver);
        //TODO 如果当前账号有登陆，则在这里再重新启动Service

    }

    private static void closeCursor(Cursor c) {
        if (c != null && !c.isClosed()) {
            c.close();
            c = null;
        }
    }

}
