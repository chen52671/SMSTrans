package com.chen.smstrans.util;

import android.net.Uri;

/**
 * Created by pc on 2015/7/30.
 */
public class MessageUtil {
    public static final String DBNAME = "message_db";
    public static final String UPLOAD_TNAME = "uploaded_message";
    public static final String DOWNLOAD_TNAME = "downloaded_message";

    public static final String AUTOHORITY = "com.chen.smstrans";

    public static final int UPLOAD_ITEM = 1;

    public static final int UPLOAD_ITEM_ID = 2;

    public static final int DOWNLOAD_ITEM = 3;

    public static final int DOWNLOAD_ITEM_ID = 4;




    public static final Uri UPLOAD_CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/uploaded_message");
    public static final Uri DOWNLOAD_CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/downloaded_message");
}
