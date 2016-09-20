package com.mfh.framework.login.logic;


import com.mfh.framework.api.account.UserMixInfo;

/**
 * Created by Administrator on 14-5-6.
 */
public interface LoginCallback {
    /**
     * 登录成功后的回调函数
     * @param user
     */
    void loginSuccess(UserMixInfo user);

    /**
     * 登录失败
     * @param errMsg 错误信息
     * */
    void loginFailed(String errMsg);
}
