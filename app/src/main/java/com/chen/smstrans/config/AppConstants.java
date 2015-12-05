package com.chen.smstrans.config;

import android.annotation.SuppressLint;
import android.os.Environment;


/**
 *
 */
@SuppressLint("SdCardPath")
public class AppConstants {

    public static String BMOB_PICTURE_PATH = Environment.getExternalStorageDirectory() + "/bmobimdemo/image/";
    public static String MyAvatarDir = "/sdcard/bmobimdemo/avatar/";
    public static final String EXTRA_STRING = "extra_string";
    public static final String ACTION_REGISTER_SUCCESS_FINISH = "register.success.finish";
}
