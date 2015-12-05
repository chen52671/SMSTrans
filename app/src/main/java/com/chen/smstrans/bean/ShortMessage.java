package com.chen.smstrans.bean;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.v3.BmobObject;

/**
 * 保存在本地和云端的消息的类型
 * 隐含的数据包括：
 * 1，ID
 * 2，创建时间
 * 3，更新时间
 * 4，ACL
 */
public class ShortMessage extends BmobObject {
    String fromNumber;//短信发件人号码
    String fromName;//短信发件人姓名（通过查询手机通讯录获取）
    String content;//短信内容
    Boolean isDownload=false; //是否已经被下载
    Boolean isRead=false;//是否已读
    String userId;
    Long  receiveTime;

    public static final String OBJECT_ID="objectid";
    public static final String FROM_NUMBER="from_number";
    public static final String CONTENT="content";
    public static final String FROM_NAME="from_name";
    public static final String RECEIVE_TIME="receive_time";
    public static final String USER_ID="user_id";


    public String toJsonString(){
            JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(OBJECT_ID,this.getObjectId());
            jsonObject.put(USER_ID,this.getUserId());
            jsonObject.put(FROM_NUMBER,this.getFromNumber());
            jsonObject.put(RECEIVE_TIME,this.getReceiveTime());
            jsonObject.put(CONTENT, this.getContent());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

       return jsonObject.toString();
    }

    public Long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Long receiveTime) {
        this.receiveTime = receiveTime;
    }




    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getFromNumber() {
        return fromNumber;
    }

    public void setFromNumber(String fromNumber) {
        this.fromNumber = fromNumber;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsDownload() {
        return isDownload;
    }

    public void setIsDownload(Boolean isDownload) {
        this.isDownload = isDownload;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}
