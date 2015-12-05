package com.chen.smstrans.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.chen.smstrans.db.DBHelper.MessageTable;

import com.chen.smstrans.bean.ShortMessage;
import com.chen.smstrans.util.MessageUtil;
import com.chen.smstrans.util.PushUtils;

import cn.bmob.v3.listener.SaveListener;


public class DatabaseUtil {
    private static final String TAG = "DatabaseUtil";
    private static Context context;
    private static DatabaseUtil instance;

    /**
     * 数据库帮助类 *
     */
    private DBHelper dbHelper;

    public synchronized static DatabaseUtil getInstance(Context context) {
        DatabaseUtil.context = context;
        if (instance == null) {
            instance = new DatabaseUtil(context);
        }
        return instance;
    }

    /**
     * 初始化
     *
     * @param context
     */
    private DatabaseUtil(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * 销毁
     */
    public static void destory() {
        if (instance != null) {
            instance.onDestory();
        }
    }

    /**
     * 销毁
     */
    public void onDestory() {
        instance = null;
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
    }

    public interface UploadCallback {
        public void onSuccess();
        public void onFail();
    }
	/*TODO */
    /*删除Message*/

	/*添加Message*/

    public void saveUploadedMessage( final ShortMessage message, final String email, final UploadCallback callback) {

        message.save(context, new SaveListener() {
            @Override
            public void onSuccess() {
                //保存本地
                ContentValues cv = new ContentValues();

                cv.put(MessageTable.USER_ID, message.getUserId());
                cv.put(MessageTable.OBJECT_ID, message.getObjectId());
                cv.put(MessageTable.IS_DOWNLOAD, false);
                cv.put(MessageTable.IS_READ, false);
                cv.put(MessageTable.FROM_NUMBER, message.getFromNumber());
                cv.put(MessageTable.FROM_NAME, message.getFromName());
                cv.put(MessageTable.CONTENT, message.getContent());
                cv.put(MessageTable.TIME_STAMP, message.getReceiveTime());
                context.getContentResolver().insert(MessageUtil.UPLOAD_CONTENT_URI, cv);

                /*推送*/
                PushUtils.getInstance(context).pushChannelMessage(message.toJsonString(), email);
            }

            @Override
            public void onFailure(int i, String s) {
                callback.onFail();
            }
        });

    }

    /*获取上传的Message*/

    public Cursor getUploadedMessages(){
        Cursor cursor=null;
        String where = null;
        cursor=dbHelper.query(DBHelper.UPLOAD_MESSAGE_TABLE_NAME, null, where, null, null, null, null);
        return cursor;
    }

    public void saveDownloadedMessage(ShortMessage message) {
        ContentValues cv = new ContentValues();

        cv.put(MessageTable.USER_ID, message.getUserId());
        cv.put(MessageTable.OBJECT_ID, message.getObjectId());
        cv.put(MessageTable.IS_DOWNLOAD, true);
        cv.put(MessageTable.IS_READ, false);
        cv.put(MessageTable.FROM_NUMBER, message.getFromNumber());
        cv.put(MessageTable.FROM_NAME, message.getFromName());
        cv.put(MessageTable.CONTENT, message.getContent());
        cv.put(MessageTable.TIME_STAMP, message.getReceiveTime());
        context.getContentResolver().insert(MessageUtil.DOWNLOAD_CONTENT_URI,cv);

        //更新服务器的短信为已读--前提是得到那条短信的objectId
        //TODO 写一个专门的Model，来操作短信


    }
	/*标记Message为已读*/

}
