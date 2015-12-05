package com.chen.smstrans.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.chen.smstrans.R;
import com.chen.smstrans.bean.User;

import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.EmailVerifyListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.ResetPasswordByCodeListener;
import cn.bmob.v3.listener.ResetPasswordByEmailListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by pc on 2015/7/26.
 */
public class AccountUtils {

    /**
     * 注册用户
     */
    public static void signUp(final Context context, String userName,String emailAddress, String password ,String nickName,boolean sex, SaveListener saveListener) {
        if(TextUtils.isEmpty(userName)||TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(password)) {
            CommonUtils.showToast(context,context.getResources().getString(R.string.input_empty));
        }
        final User user = new User();
        user.setUsername(userName);
        user.setEmail(emailAddress);
        user.setPassword(password);
        if(!TextUtils.isEmpty(nickName)){
            user.setNickName(nickName);
        }
            user.setSex(sex);

        user.signUp(context, saveListener);
    }

    /**
     * 登陆用户
     */
    public static void loginbyName(Context context,String userName,String password,SaveListener saveListener) {
        final BmobUser bu2 = new BmobUser();
        bu2.setUsername(userName);
        bu2.setPassword(password);
        bu2.login(context, saveListener);
    }

    /**
     * 获取本地用户
     * @param context
     * @return 本地用户，如果不存在则返回空
     */
    public static User getCurrentUser(Context context) {
        return BmobUser.getCurrentUser(context, User.class);
    }

    /**
     * 清除本地用户
     */
    public static void logOut(Context context) {
        BmobUser.logOut(context);
    }
/*
    *//**
     * 更新用户
     *//*
    private void updateUser() {
        final MyUser bmobUser = BmobUser.getCurrentUser(this, MyUser.class);
        if (bmobUser != null) {
            Log.d("bmob", "getObjectId = " + bmobUser.getObjectId());
            Log.d("bmob", "getUsername = " + bmobUser.getUsername());
            Log.d("bmob", "getPassword = " + bmobUser.getPassword());
            Log.d("bmob", "getEmail = "    + bmobUser.getEmail());
            Log.d("bmob", "getCreatedAt = " + bmobUser.getCreatedAt());
            Log.d("bmob", "getUpdatedAt = " + bmobUser.getUpdatedAt());
            MyUser newUser = new MyUser();
            newUser.setAge(25);
            newUser.update(this,bmobUser.getObjectId(),new UpdateListener() {

                @Override
                public void onSuccess() {
                    // TODO Auto-generated method stub
                    testGetCurrentUser();
                }

                @Override
                public void onFailure(int code, String msg) {
                    // TODO Auto-generated method stub
                    toast("更新用户信息失败:" + msg);
                }
            });
        } else {
            toast("本地用户为null,请登录。");
        }
    }

    *//**
     * 验证旧密码是否正确
     * @Title: updatePassword
     * @Description: TODO
     * @param
     * @return void
     * @throws
     *//*
    private void checkPassword() {
        BmobQuery<MyUser> query = new BmobQuery<MyUser>();
        final MyUser bmobUser = BmobUser.getCurrentUser(this, MyUser.class);
        // 如果你传的密码是正确的，那么arg0.size()的大小是1，这个就代表你输入的旧密码是正确的，否则是失败的
        query.addWhereEqualTo("password", "123456");
        query.addWhereEqualTo("username", bmobUser.getUsername());
        query.findObjects(this, new FindListener<MyUser>() {

            @Override
            public void onError(int arg0, String arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSuccess(List<MyUser> arg0) {
                // TODO Auto-generated method stub
                toast("查询密码成功:" + arg0.size());
            }
        });
    }*/

    /**
     * 重置密码
     */
    public static void resetPasswrod(Context context,String email,ResetPasswordByEmailListener resetPasswordByEmailListener) {
        BmobUser.resetPasswordByEmail(context, email, resetPasswordByEmailListener);
    }

/*    *//**
     * 查询用户
     *//*
    private void findBmobUser() {
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("username", "lucky");
        query.findObjects(this, new FindListener<MyUser>() {

            @Override
            public void onSuccess(List<MyUser> object) {
                // TODO Auto-generated method stub
                toast("查询用户成功：" + object.size());

            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                toast("查询用户失败：" + msg);
            }
        });
    }*/

    /**
     * 验证邮件
     */
    public static void emailVerify(final Context context, final String email) {

        BmobUser.requestEmailVerify(context, email, new EmailVerifyListener() {

            @Override
            public void onSuccess() {
                CommonUtils.showToast(context, "请求验证邮件成功，请到" + email + "邮箱中进行激活账户。");
            }

            @Override
            public void onFailure(int code, String e) {
                CommonUtils.showToast(context, "请求验证邮件失败:" + e);
            }
        });
    }

    public static void loginByEmailPwd(Context context,String emailAddress,String password , LogInListener logInListener){
        BmobUser.loginByAccount(context, emailAddress, password, logInListener);
    }
/*
    private void loginByPhonePwd(){
        String number = et_number.getText().toString();
        if(!TextUtils.isEmpty(number)){
            BmobUser.loginByAccount(this, number, "123456", new LogInListener<MyUser>() {

                @Override
                public void done(MyUser user, BmobException e) {
                    // TODO Auto-generated method stub
                    if(user!=null){
                        toast("登录成功");
                        Log.i("smile", ""+user.getUsername()+"-"+user.getAge()+"-"+user.getObjectId()+"-"+user.getEmail());
                    }else{
                        toast("错误码："+e.getErrorCode()+",错误原因："+e.getLocalizedMessage());
                    }
                }
            });
        }else{
            toast("请输入手机号");
        }
    }

    private void loginByPhoneCode(){
        //1、调用请求验证码接口
//		BmobSMS.requestSMSCode(this, "手机号码", "模板名称",new RequestSMSCodeListener() {
//
//			@Override
//			public void done(String smsId,BmobException ex) {
//				// TODO Auto-generated method stub
//				if(ex==null){//验证码发送成功
//					Log.i("smile", "短信id："+smsId);
//				}
//			}
//		});
        String number = et_number.getText().toString();
        String code = et_code.getText().toString();
        if(!TextUtils.isEmpty(number)&&!TextUtils.isEmpty(code)){
            //2、使用验证码进行登陆
            BmobUser.loginBySMSCode(this, number, code, new LogInListener<MyUser>() {

                @Override
                public void done(MyUser user, BmobException e) {
                    // TODO Auto-generated method stub
                    if(user!=null){
                        toast("登录成功");
                        Log.i("smile", ""+user.getUsername()+"-"+user.getAge()+"-"+user.getObjectId()+"-"+user.getEmail());
                    }else{
                        toast("错误码："+e.getErrorCode()+",错误原因："+e.getLocalizedMessage());
                    }
                }
            });
        }else{
            toast("请输入手机号和验证码");
        }
    }
    private void signOrLoginByPhoneNumber(){
        //1、调用请求验证码接口
//		BmobSMS.requestSMSCode(this, "手机号码", "模板名称",new RequestSMSCodeListener() {
//
//			@Override
//			public void done(String smsId,BmobException ex) {
//				// TODO Auto-generated method stub
//				if(ex==null){//验证码发送成功
//					Log.i("smile", "短信id："+smsId);
//				}
//			}
//		});
        String number = et_number.getText().toString();
        String code = et_code.getText().toString();
        if(!TextUtils.isEmpty(number)&&!TextUtils.isEmpty(code)){
            //2、使用手机号和短信验证码进行一键注册登录
            BmobUser.signOrLoginByMobilePhone(this, number, code, new LogInListener<MyUser>() {

                @Override
                public void done(MyUser user, BmobException e) {
                    // TODO Auto-generated method stub
                    if(user!=null){
                        toast("登录成功");
                        Log.i("smile", ""+user.getUsername()+"-"+user.getAge()+"-"+user.getObjectId()+"-"+user.getEmail());
                    }else{
                        toast("错误码："+e.getErrorCode()+",错误原因："+e.getLocalizedMessage());
                    }
                }
            });
        }else{
            toast("请输入手机号和验证码");
        }
    }

    *//** 通过短信验证码来重置用户密码
     * @method requestSmsCode
     * @return void
     * 注：整体流程是先调用请求验证码的接口获取短信验证码，随后调用短信验证码重置密码接口来重置该手机号对应的用户的密码
     *//*
    private void resetPasswordBySMS(){
        //1、请求短信验证码
//		BmobSMS.requestSMSCode(this, "手机号码", "模板名称",new RequestSMSCodeListener() {
//
//			@Override
//			public void done(String smsId,BmobException ex) {
//				// TODO Auto-generated method stub
//				if(ex==null){//验证码发送成功
//					Log.i("smile", "短信id："+smsId);
//				}
//			}
//		});
        String code = et_code.getText().toString();
        if(!TextUtils.isEmpty(code)){
            //2、重置的是绑定了该手机号的账户的密码
            BmobUser.resetPasswordBySMSCode(this, code,"1234567", new ResetPasswordByCodeListener() {

                @Override
                public void done(BmobException e) {
                    // TODO Auto-generated method stub
                    if(e==null){
                        toast("密码重置成功");
                    }else{
                        toast("错误码："+e.getErrorCode()+",错误原因："+e.getLocalizedMessage());
                    }
                }
            });
        }else{
            toast("请填写验证码");
        }
    }*/

}
