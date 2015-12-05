package com.chen.smstrans.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATA_BASE_NAME = "message_db";
    public static final int DATA_BASE_VERSION = 1;
    public static final String UPLOAD_MESSAGE_TABLE_NAME = "uploaded_message";
    public static final String DOWNLOAD_MESSAGE_TABLE_NAME = "downloaded_message";

    private SQLiteDatabase mDb;

    public DBHelper(Context context) {
        super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        onCreateMessageTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    public interface MessageTable {
        String _ID = "_id";
        String USER_ID = "userid";//用户ID
        String OBJECT_ID = "objectid";//Message对象的ID
        String IS_DOWNLOAD = "download_status"; //是否已经被下载
        String IS_READ = "read_status";
        String FROM_NUMBER = "from_number";
        String FROM_NAME = "from_name";//短信发件人姓名（通过查询手机通讯录获取）
        String CONTENT = "content";//短信内容
        String TIME_STAMP = "timeStamp";
    }

    public static final String[] CONTENT_PROJECTION = new String[]{
            MessageTable._ID,
            MessageTable.USER_ID,
            MessageTable.OBJECT_ID,
            MessageTable.IS_DOWNLOAD,
            MessageTable.IS_READ,
            MessageTable.FROM_NUMBER,
            MessageTable.FROM_NAME,
            MessageTable.CONTENT,
            MessageTable.TIME_STAMP
    };
    public static final int INDEX_ID = 0;
    public static final int INDEX_USER_ID = 1;
    public static final int INDEX_OBJECT_ID = 2;
    public static final int INDEX_DOWNLOAD = 3;
    public static final int INDEX_READ = 4;
    public static final int INDEX_FROM_NUMBER = 5;
    public static final int INDEX_FROM_NAME = 6;
    public static final int INDEX_CONTENT = 7;
    public static final int INDEX_TIMESTAMP = 8;

    private String createUploadTableAction = "CREATE TABLE IF NOT EXISTS `" + DBHelper.UPLOAD_MESSAGE_TABLE_NAME + "` (\n" +
            "  `" + MessageTable._ID + "` integer primary key autoincrement,\n" +
            "  `" + MessageTable.USER_ID + "`  varchar(100),\n" +
            "  `" + MessageTable.OBJECT_ID + "` varchar(100),\n" +
            "  `" + MessageTable.IS_DOWNLOAD + "` INTEGER NOT NULL ,\n" +
            "  `" + MessageTable.IS_READ + "` INTEGER NOT NULL ,\n" +
            "  `" + MessageTable.FROM_NUMBER + "` TEXT DEFAULT NULL,\n" +
            "  `" + MessageTable.FROM_NAME + "` TEXT DEFAULT NULL,\n" +
            "  `" + MessageTable.CONTENT + "`  TEXT DEFAULT NULL,\n" +
            "  `" + MessageTable.TIME_STAMP + "` INTEGER not null\n" +
            ") ";

    private String createDownloadTableAction = "CREATE TABLE IF NOT EXISTS `" + DBHelper.DOWNLOAD_MESSAGE_TABLE_NAME + "` (\n" +
            "  `" + MessageTable._ID + "` integer primary key autoincrement,\n" +
            "  `" + MessageTable.USER_ID + "`  varchar(100),\n" +
            "  `" + MessageTable.OBJECT_ID + "` varchar(100),\n" +
            "  `" + MessageTable.IS_DOWNLOAD + "` INTEGER NOT NULL ,\n" +
            "  `" + MessageTable.IS_READ + "` INTEGER NOT NULL ,\n" +
            "  `" + MessageTable.FROM_NUMBER + "` TEXT DEFAULT NULL,\n" +
            "  `" + MessageTable.FROM_NAME + "` TEXT DEFAULT NULL,\n" +
            "  `" + MessageTable.CONTENT + "`  TEXT DEFAULT NULL,\n" +
            "  `" + MessageTable.TIME_STAMP + "` INTEGER not null\n" +
            ") ";

    private void onCreateMessageTable(SQLiteDatabase db) {
        db.execSQL(createUploadTableAction);
        db.execSQL(createDownloadTableAction);
    }


    /**
     * 获取数据库操作对象
     *
     * @param isWrite 是否可写
     * @return
     */
    public synchronized SQLiteDatabase getDatabase(boolean isWrite) {

        if (mDb == null || !mDb.isOpen()) {
            if (isWrite) {
                try {
                    mDb = getWritableDatabase();
                } catch (Exception e) {
                    // 当数据库不可写时
                    mDb = getReadableDatabase();
                    return mDb;
                }
            } else {
                mDb = getReadableDatabase();
            }
        }
        // } catch (SQLiteException e) {
        // // 当数据库不可写时
        // mDb = getReadableDatabase();
        // }
        return mDb;
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        getDatabase(true);
        return mDb.delete(table, whereClause, whereArgs);
    }

    public long insert(String table, String nullColumnHack, ContentValues values) {
        getDatabase(true);
        return mDb.insertOrThrow(table, nullColumnHack, values);
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        getDatabase(true);
        return mDb.update(table, values, whereClause, whereArgs);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        getDatabase(false);
        return mDb.rawQuery(sql, selectionArgs);
    }

    public void execSQL(String sql) {
        getDatabase(true);
        mDb.execSQL(sql);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
                        // final
                        String orderBy) {
        getDatabase(false);
        return mDb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

}
