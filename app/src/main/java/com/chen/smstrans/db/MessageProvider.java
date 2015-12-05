package com.chen.smstrans.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.chen.smstrans.util.LogUtils;
import com.chen.smstrans.util.MessageUtil;

/**
 * Created by pc on 2015/7/30.
 */
public class MessageProvider extends ContentProvider {
    DBHelper dBhelper;
    SQLiteDatabase db;
    private static final UriMatcher sMatcher;

    static {

        sMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sMatcher.addURI(MessageUtil.AUTOHORITY, MessageUtil.UPLOAD_TNAME, MessageUtil.UPLOAD_ITEM);
        sMatcher.addURI(MessageUtil.AUTOHORITY, MessageUtil.UPLOAD_TNAME + "/#", MessageUtil.UPLOAD_ITEM_ID);
        sMatcher.addURI(MessageUtil.AUTOHORITY, MessageUtil.DOWNLOAD_TNAME, MessageUtil.DOWNLOAD_ITEM);
        sMatcher.addURI(MessageUtil.AUTOHORITY, MessageUtil.DOWNLOAD_TNAME + "/#", MessageUtil.DOWNLOAD_ITEM_ID);


    }

    @Override
    public boolean onCreate() {
        this.dBhelper = new DBHelper(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        db = dBhelper.getReadableDatabase();
        long id;
        String where;
        int count = 0;

        switch (sMatcher.match(uri)) {

            case MessageUtil.UPLOAD_ITEM:

                return db.query(MessageUtil.UPLOAD_TNAME, projection, selection, selectionArgs, null, null, sortOrder);


            case MessageUtil.UPLOAD_ITEM_ID:
                id = ContentUris.parseId(uri);
                where = "_id=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = selection + " and " + where;
                }
                return db.query(MessageUtil.UPLOAD_TNAME, projection, where, selectionArgs, null,
                        null, sortOrder);

            case MessageUtil.DOWNLOAD_ITEM:
                return db.query(MessageUtil.DOWNLOAD_TNAME, projection, selection, selectionArgs, null, null, sortOrder);


            case MessageUtil.DOWNLOAD_ITEM_ID:
                id = ContentUris.parseId(uri);
                where = "_id=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = selection + " and " + where;
                }
                return db.query(MessageUtil.DOWNLOAD_TNAME, projection, where, selectionArgs, null,
                        null, sortOrder);
            default:

                throw new IllegalArgumentException("Unknown URI" + uri);

        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sMatcher.match(uri)) {
            case MessageUtil.UPLOAD_ITEM:
                return "vnd.android.cursor.dir/uploaded_message";

            case MessageUtil.UPLOAD_ITEM_ID:
                return "vnd.android.cursor.item/uploaded_message";
            case MessageUtil.DOWNLOAD_ITEM:
                return "vnd.android.cursor.dir/downloaded_message";
            case MessageUtil.DOWNLOAD_ITEM_ID:
                return "vnd.android.cursor.item/downloaded_message";
            default:
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = dBhelper.getWritableDatabase();
        long rowid;
        Uri insertUri;
        switch (sMatcher.match(uri)) {
            case MessageUtil.UPLOAD_ITEM:
                rowid = db.insert(MessageUtil.UPLOAD_TNAME, "name", values);
                insertUri = ContentUris.withAppendedId(uri, rowid);// 得到代表新增记录的Uri
                this.getContext().getContentResolver().notifyChange(uri, null);
                return insertUri;


            case MessageUtil.DOWNLOAD_ITEM:
                rowid = db.insert(MessageUtil.DOWNLOAD_TNAME, "name", values);
                insertUri = ContentUris.withAppendedId(uri, rowid);// 得到代表新增记录的Uri
                this.getContext().getContentResolver().notifyChange(uri, null);
                LogUtils.d("通知的URI：" + uri);
                return insertUri;
            default:
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db = dBhelper.getWritableDatabase();
        long id;
        String where;
        int count = 0;

        switch (sMatcher.match(uri)) {

            case MessageUtil.UPLOAD_ITEM:

                count = db.delete(MessageUtil.UPLOAD_TNAME, selection, selectionArgs);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return count;


            case MessageUtil.UPLOAD_ITEM_ID:
                id = ContentUris.parseId(uri);
                where = "_id=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = selection + " and " + where;
                }
                count = db.delete(MessageUtil.UPLOAD_TNAME, where, selectionArgs);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return count;

            case MessageUtil.DOWNLOAD_ITEM:

                count = db.delete(MessageUtil.DOWNLOAD_TNAME, selection, selectionArgs);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return count;


            case MessageUtil.DOWNLOAD_ITEM_ID:
                id = ContentUris.parseId(uri);
                where = "_id=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = selection + " and " + where;
                }
                count = db.delete(MessageUtil.DOWNLOAD_TNAME, where, selectionArgs);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return count;

            default:

                throw new IllegalArgumentException("Unknown URI" + uri);

        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dBhelper.getWritableDatabase();
        long id;
        String where;
        int count = 0;
        switch (sMatcher.match(uri)) {
            case MessageUtil.UPLOAD_ITEM:
                count = db.update(MessageUtil.UPLOAD_TNAME, values, selection, selectionArgs);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case MessageUtil.UPLOAD_ITEM_ID:
                id = ContentUris.parseId(uri);
                where = "_id=" + id;
                if (selection != null && !"".equals(selection)) {
                    where = selection + " and " + where;
                }
                count = db.update(MessageUtil.UPLOAD_TNAME, values, where, selectionArgs);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case MessageUtil.DOWNLOAD_ITEM:
                count = db.update(MessageUtil.DOWNLOAD_TNAME, values, selection, selectionArgs);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case MessageUtil.DOWNLOAD_ITEM_ID:
                id = ContentUris.parseId(uri);
                where = "_id=" + id;
                if (selection != null && !"".equals(selection)) {
                    where = selection + " and " + where;
                }
                count = db.update(MessageUtil.DOWNLOAD_TNAME, values, where, selectionArgs);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
    }

}
