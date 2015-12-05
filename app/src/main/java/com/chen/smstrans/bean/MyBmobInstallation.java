package com.chen.smstrans.bean;

import android.content.Context;

import cn.bmob.v3.BmobInstallation;

/**
 * Created by pc on 2015/7/31.
 */
public class MyBmobInstallation extends BmobInstallation{
    private String usrEmail;
    public MyBmobInstallation(Context context) {
        super(context);
    }

    public String getUsrEmail() {
        return usrEmail;
    }

    public void setUsrEmail(String usrEmail) {
        this.usrEmail = usrEmail;
    }


}
