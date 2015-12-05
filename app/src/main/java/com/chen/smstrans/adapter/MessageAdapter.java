package com.chen.smstrans.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chen.smstrans.R;
import com.chen.smstrans.controller.MessageController;
import com.chen.smstrans.db.DBHelper;

/**
 * Created by pc on 2015/7/29.
 */
public class MessageAdapter extends CursorAdapter {

    private Context context;
    private MessageController mController;
    public MessageAdapter(Context context, Cursor c) {
        super(context, c);
        this.context = context;
    }

    public MessageAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        ViewHolder holder;
        holder = new ViewHolder();
        View convertView = mInflater.inflate(R.layout.message_item,
                null);
        holder.sender = (TextView) convertView
                .findViewById(R.id.senders);
        holder.content = (TextView) convertView
                .findViewById(R.id.content);
        holder.date = (TextView) convertView
                .findViewById(R.id.date);
        holder.unreadState = (ImageView) convertView
                .findViewById(R.id.unread_state);
        holder.messageSelection = (LinearLayout) convertView.findViewById(R.id.message_select_image_container);
        holder.messageSelecttionImage = (ImageView) convertView.findViewById(R.id.message_select_image);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String sender = cursor.getString(DBHelper.INDEX_FROM_NAME);
        if(TextUtils.isEmpty(sender)) {
            sender = cursor.getString(DBHelper.INDEX_FROM_NUMBER);
            //TODO 做一个异步任务，根据号码查询本地的联系人，然后根据本地联系人更新数据库
        }
        holder.sender.setText(sender);
        holder.content.setText(cursor.getString(DBHelper.INDEX_CONTENT));
        holder.date.setText(getDateShort(context, cursor.getLong(DBHelper.INDEX_TIMESTAMP)));
        holder.unreadState.setVisibility(cursor.getInt(DBHelper.INDEX_READ) == 1 ? View.GONE : View.VISIBLE);
        handleSelectionIcon(holder, cursor);
    }

    private void handleSelectionIcon(ViewHolder viewHolder, Cursor cursor) {
        int mode = mController.getMode();
        switch (mode) {
            case MessageController.MODE_CAB:
                viewHolder.messageSelection.setVisibility(View.VISIBLE);
                viewHolder.messageSelecttionImage.setImageResource(
                        mController.containsCheckedItem(cursor.getInt(DBHelper.INDEX_ID)) ?
                                R.drawable.header_icon_selected : R.drawable.header_icon_unselected);
                break;
            case MessageController.MODE_NORMAL:
                viewHolder.messageSelection.setVisibility(View.GONE);
                break;
            case MessageController.MODE_SELECT_ALL:
                viewHolder.messageSelection.setVisibility(View.VISIBLE);
                viewHolder.messageSelecttionImage.setImageResource(R.drawable.header_icon_selected);
                break;
            case MessageController.MODE_SELECT_CLEAR:
                viewHolder.messageSelection.setVisibility(View.VISIBLE);
                viewHolder.messageSelecttionImage.setImageResource(R.drawable.header_icon_unselected);
                break;
        }

    }

    private String getDateShort(Context context, long aLong) {
        return DateUtils.getRelativeTimeSpanString(context, aLong).toString();
    }

    public void setController(MessageController mController) {
        this.mController = mController;
        mController.setMode(MessageController.MODE_NORMAL);
    }
    public class ViewHolder {
        TextView sender;
        TextView content;
        TextView date;
        ImageView unreadState;
        LinearLayout messageSelection;
        ImageView messageSelecttionImage;
    }
}
