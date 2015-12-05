package com.chen.smstrans.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.chen.smstrans.bean.ShortMessage;
import com.chen.smstrans.controller.MyHandler;
import com.chen.smstrans.db.DatabaseUtil;
import com.chen.smstrans.util.LogUtils;

import cn.bmob.v3.BmobUser;

/**
 * 将shortMessage保存在本地数据库，并推送到服务器
 */
public class SaveAndUploadThread extends Thread implements DatabaseUtil.UploadCallback {
    public static final int SAVE_AND_UPLOAD_THREAD_BEGIN = 101;
    public static final int SAVE_AND_UPLOAD_THREAD_FINISHED = 102;
    public static final int SAVE_MESSAGE_DOWNLOAD_DB_BEGIN = 201;
    public static final int SAVE_MESSAGE_DOWNLOAD_DB_FINISHED = 202;

    public static final int UPLOAD_TASK = 1;
    public static final int DOWNLOAD_TASK = 2;
    private ShortMessage shortMessage;
    private Handler mHandler;
    private Context context;
    private int taskType;
    private boolean isStarted = false;

    public SaveAndUploadThread(Context context, ShortMessage newShortMessage, int taskType, Handler mHandler) {
        this.shortMessage = newShortMessage;
        this.mHandler = mHandler;
        this.taskType = taskType;
        this.context = context;
    }

    @Override
    public void run() {
        isStarted = true;

        switch (this.taskType) {
            case UPLOAD_TASK:
                BmobUser bmobUser = BmobUser.getCurrentUser(context);
                String email = "";
                if (bmobUser != null) {
                    email = bmobUser.getEmail();
                    LogUtils.d("USR email:" + email);
                }

                LogUtils.d("上传接收到的短信");
                DatabaseUtil.getInstance(context).saveUploadedMessage(shortMessage, email, this);


                break;
            case DOWNLOAD_TASK:
                LogUtils.d("保存接收到的短信");
                DatabaseUtil.getInstance(context).saveDownloadedMessage(shortMessage);
                Message message = Message.obtain();
                message.what = SAVE_AND_UPLOAD_THREAD_FINISHED;
                mHandler.sendMessage(message);
                break;
        }


    }


    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public void onSuccess() {
        //任务完成
        //开始下一个任务
        Message message = Message.obtain();
        message.what = SAVE_AND_UPLOAD_THREAD_FINISHED;
        mHandler.sendMessage(message);
    }

    @Override
    public void onFail() {
        LogUtils.e("上传失败，1分钟后重传");
        MyHandler myHandler = (MyHandler) this.mHandler;
        myHandler.uploadMessageLater(context,shortMessage, 1000L * 60);
    }
}
