package com.chen.smstrans.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CommonUtils {

    private static final String DOMAIN_PART_PART = "[[\\w][\\d]\\-\\(\\)\\[\\]]+";
    private static final String LOCAL_PART = "[^@]+";
    private static final String DOMAIN_PART =
            "(" + DOMAIN_PART_PART + "\\.)+" + DOMAIN_PART_PART;
    /**
     * Pattern to check if an email address is valid.
     */
    private static final Pattern EMAIL_ADDRESS =
            Pattern.compile("\\A" + LOCAL_PART + "@" + DOMAIN_PART + "\\z");

    private static final Pattern EMAIL_ADDRESS_HALF =
            Pattern.compile("\\A" + LOCAL_PART + "@" + "(.*?)");
    private static Toast mToast;
    private static Handler sMainThreadHandler;


    /**
     * 检查是否有网络
     */
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            return info.isAvailable();
        }
        return false;
    }

    /**
     * 检查是否是WIFI
     */
    public static boolean isWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI)
                return true;
        }
        return false;
    }

    /**
     * 检查是否是移动网络
     */
    public static boolean isMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }

    private static NetworkInfo getNetworkInfo(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * 检查SD卡是否存在
     */
    public static boolean checkSdCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }


    public static boolean isValidAddress(final String address) {
        boolean isValid = EMAIL_ADDRESS.matcher(address).find();
        if (-1 != address.indexOf(" ")) {
            isValid = false;
        }
        return isValid;
    }

    public static void showToast(Context context, int resId) {
        showToast(context, context.getResources().getString(resId));
    }

    public static void showToast(Context context, int resId, final int duration) {
        showToast(context, context.getResources().getString(resId), duration);
    }

    public static void showToast(final Context context, final String message) {
        showToast(context, message, Toast.LENGTH_LONG);
    }

    public static void showToast(final Context context, final String message, final int duration) {
        if (mToast != null) {
            mToast.cancel();
        }

        getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mToast = Toast.makeText(context, message, duration);
                mToast.show();
            }
        });
    }

    public static Handler getMainThreadHandler() {
        if (sMainThreadHandler == null) {
            // No need to synchronize -- it's okay to create an extra Handler, which will be used
            // only once and then thrown away.
            sMainThreadHandler = new Handler(Looper.getMainLooper());
        }
        return sMainThreadHandler;
    }

    public static boolean isValidUserName(String userName) {
        if(!TextUtils.isEmpty(userName)){
            String str = stringFilter(userName);
            if (userName.equals(str)){
                return true;
            }
        }
        return false;
    }

    public static String stringFilter(String str)throws PatternSyntaxException {
        String regEx = "[/\\:*?@<>|\"\n\t]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("");
    }

    public static String formatPlural(Context context, int resource, int count) {
        final CharSequence formatString = context.getResources().getQuantityText(resource, count);
        return String.format(formatString.toString(), count);
    }
}
