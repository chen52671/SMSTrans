package com.chen.smstrans.bean;

import cn.bmob.v3.BmobUser;


public class User extends BmobUser {


    private static final long serialVersionUID = 1L;



    private Boolean sex;

    private String nickName;

    public Boolean getSex() {
        return sex;
    }

    public void setSex(Boolean sex) {
        this.sex = sex;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
