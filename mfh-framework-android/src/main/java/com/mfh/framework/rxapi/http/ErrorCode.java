package com.mfh.framework.rxapi.http;

public class ErrorCode {
    public static final String SUCCESS = "0";
    public static final String ERROR = "1";//系统异常
    public static final String BIZ_ERROR = "2";//业务异常
    public static final String LOGIN_ERROR = "3";//登录异常
    public static final String PRIV_ERROR = "4";//权限异常
    public static final String CONTENT_ERROR = "5";//内容异常
    public static final String SUCCESS_NEW = "9";//登录成功且是新注册用户

    public static final String USER_NOT_EXIST = "100";
    public static final String WRONG_PASSWORD = "101";

    public static final String PAY_ERROR = "-1";//支付异常，支付结果不明确
    public static final String PAY_PASSWORD = "1";//下单成功等待用户输入密码

    public static class PAY {
        public static final String SUCCESS = "0";
        public static final String ERROR = "-1";//支付异常，支付结果不明确
        public static final String PASSWORD = "1";//下单成功等待用户输入密码
    }
}