package com.chen.smstrans.controller;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.CursorAdapter;
import android.widget.ListView;


import com.chen.smstrans.adapter.MessageAdapter;
import com.chen.smstrans.db.DBHelper;
import com.chen.smstrans.ui.MessageActivity;
import com.chen.smstrans.util.MessageUtil;

import java.util.*;
/**
 * MessageActivity的Controller
 */
public class MessageController {
    public final static int MODE_UNKNOWN = 0x100;
    public final static int MODE_NORMAL = 0x101;
    public final static int MODE_CAB = 0x102;
    public final static int MODE_SELECT_ALL = 0x103;
    public final static int MODE_SELECT_CLEAR = 0x104;
   
    // private Set<Long> mItems = new HashSet<Long>();
    private MessageActivity mActivity;
    private MessageAdapter mAdapter;
    private int mMode = MODE_NORMAL;
    private int messageType;
    private EmailModel emailModel = new EmailModel();


    private boolean isBottomBtnReadView;
    private ListView mListView;

    public MessageController(MessageActivity messageActivity) {
        this.mActivity = messageActivity;
    }


    public int getMode() {
        return mMode;
    }

    public void setMode(int mMode) {
        this.mMode = mMode;
    }


    public void onItemClick(int position, long id) {
        //只有有Item被点击，就切换为CAB状态
        switch (getMode()){
            case MODE_NORMAL:
                setMode(MODE_CAB);
                emailModel.mItems.add(id);
                break;
            case MODE_CAB:
            case MODE_SELECT_ALL:
            case MODE_SELECT_CLEAR:
                setMode(MODE_CAB);
                if (emailModel.mItems.contains(id)) {
                    emailModel.mItems.remove(id);
                } else {
                    emailModel.mItems.add(id);
                }
                mActivity.updateCABTitles();
                break;
        }
        //记录Star和Read状态
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        setEmailRead(id, cursor.getInt(DBHelper.INDEX_READ) == 1);
        //更新View
        mActivity.updateBottomBtnMode();
    }

    public void setMessageType(int infoType) {
        this.messageType = infoType;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setAdapter(MessageAdapter adapter) {
        this.mAdapter = adapter;
    }

    public CursorAdapter getAdapter() {
        return mAdapter;
    }

    public void clearCheckedItem() {
        emailModel.mItems.clear();
    }

    public void onActionBarChecked() {
        if (emailModel.mItems.size() == mListView.getCount()) {
            emailModel.mItems.clear();
            emailModel.readTable.clear();
            this.setMode(MODE_SELECT_CLEAR);
        } else {
            for (int i = 0; i < mListView.getCount(); i++) {
                long itemId = mListView.getItemIdAtPosition(i);
                emailModel.mItems.add(itemId);
            }
            this.setMode(MODE_SELECT_ALL);
        }
    }

    public void setListView(ListView listView) {
        this.mListView = listView;
    }

    public ListView getListView() {
        return mListView;
    }

    public int getCheckedItemSize() {
        return emailModel.mItems.size();
    }

    public boolean isAllItemSelected() {
        boolean allSelected = false;
        int selectedItems = emailModel.mItems.size();
        if (selectedItems > 0 && selectedItems == mListView.getCount()) {
            allSelected = true;
        }
        return allSelected;
    }




    public void setBottomBtnReadView(boolean isBottomBtnReadView) {
        this.isBottomBtnReadView = isBottomBtnReadView;
    }



    /**
     * 获取当前下边栏的Read按钮样式
     *
     * @return true 已读打开样式 false 未读样式
     */
    public boolean isReadView() {
        return isBottomBtnReadView;
    }

    public boolean containsCheckedItem(long id) {
        return emailModel.mItems.contains(id);
    }

    public void refreshBottomBtnMode() {
        isBottomBtnReadView = false;
        if (emailModel.mItems.size() == 0) {
            isBottomBtnReadView = true;
            return;
        }

        for (Long emailId : emailModel.mItems) {
            if (!isBottomBtnReadView && emailModel.isUnRead(emailId)) {//只要有一封是未读，就直接设为打开图标
                isBottomBtnReadView = true;
                return;
            }
        }
    }

    public void setEmailRead(long id, boolean b) {
        emailModel.readTable.put(id, b);
    }


    private class EmailModel {

        public Set<Long> mItems = new HashSet<Long>();

        public Hashtable<Long, Boolean> readTable = new Hashtable<Long, Boolean>();

        public boolean isUnRead(Long emailId) {
            Boolean isRead = readTable.get(emailId);
            if (isRead != null) {
                if (isRead) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }

    }

    public void changeSelectedItemsReadStatus() {
        EmailUpdateTask task = new EmailUpdateTask(mActivity, EmailUpdateTask.MARK_READ_TASK);
        task.execute();
    }



    public void changeSelectedItemsStarStatus() {
        EmailUpdateTask task = new EmailUpdateTask(mActivity, EmailUpdateTask.MARK_STAR_TASK);
        task.execute();

    }
    /**
     * 将所选的邮件从收件箱列表删除，
     */
    public void deleteSelectedItems() {
        EmailUpdateTask task = new EmailUpdateTask(mActivity, EmailUpdateTask.MARK_DELERE_TASK);
        task.execute();
    }

    /**
     * 在后台更新所选邮件的状态
     * 返回Cursor是为以后可能有查询操作留的
     */
    private class EmailUpdateTask extends AsyncTask<Void, Void, Cursor>{
        public static final int MARK_STAR_TASK = 0;
        public static final int MARK_READ_TASK = 1;
        public static final int MARK_DELERE_TASK = 2;

        private Context mContext;
        private int mType;

        public EmailUpdateTask(Context mContext, int mType) {
            this.mContext = mContext;
            this.mType = mType;
        }

        @Override
        protected Cursor doInBackground(Void... params) {
            switch (mType){

                case MARK_READ_TASK:
                    changeReadStatusTask();
                    break;
                case MARK_DELERE_TASK:
                    //删除操作是将邮件的MailBoxKey设为 已删除的那个文件夹
                    //首先找到该账户所属的已删除文件夹，然后再删除。
                    deleteEmailTask();
                    break;
            }
            return null;
        }


        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            //任务结束后，再通知Activity结束ActionMode
            mActivity.finishActionMode();
        }


        private void changeReadStatusTask(){
            final ArrayList<ContentProviderOperation> updateTask = new ArrayList<ContentProviderOperation>();
            updateReadStatus(updateTask, isReadView());
            try {
                mActivity.getContentResolver().applyBatch(MessageUtil.AUTOHORITY, updateTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void updateReadStatus(ArrayList<ContentProviderOperation> updateTask, boolean isRead) {
            ContentProviderOperation updateMessage;
            for (Long emailID : emailModel.mItems) {
                ContentValues emailValues = new ContentValues();
                emailValues.put(DBHelper.MessageTable.IS_READ, isRead ? 1 : 0);
                updateMessage = ContentProviderOperation.newUpdate(MessageUtil.DOWNLOAD_CONTENT_URI).withValues(emailValues)
                        .withSelection(DBHelper.MessageTable._ID + "=?", new String[]{Long.toString(emailID)})
                        .build();
                updateTask.add(updateMessage);
            }
        }


        private void deleteEmailTask(){
            final ArrayList<ContentProviderOperation> updateTask = new ArrayList<ContentProviderOperation>();
            deleteSelectedEmails(updateTask);
            try {
                mActivity.getContentResolver().applyBatch(MessageUtil.AUTOHORITY, updateTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void deleteSelectedEmails(ArrayList<ContentProviderOperation> updateTask) {
            ContentProviderOperation updateMessage;
            for (Long emailID : emailModel.mItems) {
                updateMessage = ContentProviderOperation.newDelete(MessageUtil.DOWNLOAD_CONTENT_URI)
                        .withSelection(DBHelper.MessageTable._ID + "=?", new String[]{Long.toString(emailID)})
                        .build();
                updateTask.add(updateMessage);
            }
        }
    }


}
