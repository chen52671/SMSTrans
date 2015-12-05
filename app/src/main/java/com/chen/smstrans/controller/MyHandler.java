package com.chen.smstrans.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.chen.smstrans.bean.ShortMessage;
import com.chen.smstrans.service.SaveAndUploadThread;
import com.chen.smstrans.service.SyncService;
import com.chen.smstrans.util.LogUtils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by pc on 2015/7/31.
 */
public class MyHandler extends Handler {
    Queue<SaveAndUploadThread> threadQueue;

    public MyHandler(Queue<SaveAndUploadThread> threadQueue) {
        this.threadQueue = threadQueue;
    }

    public void uploadMessageLater(Context context,ShortMessage message,Long time){
        threadQueue.add(new SaveAndUploadThread(context, message, SaveAndUploadThread.UPLOAD_TASK, this));
        this.sendEmptyMessageDelayed(SaveAndUploadThread.SAVE_AND_UPLOAD_THREAD_BEGIN,time);
    }

    @Override
    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case SaveAndUploadThread.SAVE_AND_UPLOAD_THREAD_BEGIN:
            case SaveAndUploadThread.SAVE_MESSAGE_DOWNLOAD_DB_BEGIN:
                if (!threadQueue.isEmpty()) {
                    if (!threadQueue.peek().isStarted()) {
                        //开始一个新的下载任务
                        LogUtils.d("SAVE_UPLOAD_TASK START");
                        threadQueue.poll().start();
                    }
                } else {

                }
                break;

            case SaveAndUploadThread.SAVE_AND_UPLOAD_THREAD_FINISHED:
            case SaveAndUploadThread.SAVE_MESSAGE_DOWNLOAD_DB_FINISHED:
                if (!threadQueue.isEmpty()) {
                    //开始下一个任务
                    Message message = Message.obtain();
                    message.what = SaveAndUploadThread.SAVE_AND_UPLOAD_THREAD_BEGIN;
                    sendMessage(message);
                }
                break;

        }
    }
}
