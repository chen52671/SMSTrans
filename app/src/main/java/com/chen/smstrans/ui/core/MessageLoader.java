package com.chen.smstrans.ui.core;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.chen.smstrans.bean.ShortMessage;
import com.chen.smstrans.db.DBHelper;
import com.chen.smstrans.util.MessageUtil;

/**
 * Created by pc on 2015/8/1.
 */
public class MessageLoader extends AsyncTaskLoader<ShortMessage> {
    String messageObjectId;

    public MessageLoader(Context context, String messageObjectId) {
        super(context);
        this.messageObjectId = messageObjectId;
    }


    /* Runs on a worker thread */
    @Override
    public ShortMessage loadInBackground() {
        ShortMessage shortMessage = null;

        Cursor cursor = getContext().getContentResolver().query(MessageUtil.DOWNLOAD_CONTENT_URI,
                DBHelper.CONTENT_PROJECTION, DBHelper.MessageTable.OBJECT_ID + "=?",
                new String[]{messageObjectId}, null);
        if (cursor != null && cursor.moveToFirst()) {
            try {
                // Ensure the cursor window is filled.
                shortMessage = getMessageByCursor(cursor);
                //TODO 更新服务器的已读状态+更新本地已读状态
                ContentValues messageValues = new ContentValues();
                messageValues.put(DBHelper.MessageTable.IS_READ, 1);
                getContext().getContentResolver().update(MessageUtil.DOWNLOAD_CONTENT_URI,
                        messageValues,
                        DBHelper.MessageTable.OBJECT_ID + "=?",
                        new String[]{shortMessage.getObjectId()});
            } catch (RuntimeException ex) {
                cursor.close();
                throw ex;
            } finally {
                cursor.close();
            }
        }
        return shortMessage;

    }

    private ShortMessage getMessageByCursor(Cursor cursor) {
        ShortMessage shortMessage = new ShortMessage();
        shortMessage.setObjectId(cursor.getString(DBHelper.INDEX_OBJECT_ID));
        shortMessage.setReceiveTime(cursor.getLong(DBHelper.INDEX_TIMESTAMP));
        shortMessage.setFromNumber(cursor.getString(DBHelper.INDEX_FROM_NUMBER));
        shortMessage.setContent(cursor.getString(DBHelper.INDEX_CONTENT));
        return shortMessage;
    }

}
