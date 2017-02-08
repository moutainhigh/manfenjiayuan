package com.mfh.framework.rxapi.http;

/**
 * 自定义服务器api异常信息
 * Created by bingshanguxue on 16/12/27.
 */
public class ApiException extends RuntimeException {

    public static final int USER_NOT_EXIST = 100;
    public static final int WRONG_PASSWORD = 101;

    private int code;

    public ApiException(String message, int code) {
        super(message);
        this.code = code;
    }

    public ApiException(int code) {
        this(getApiExceptionMessage(code), code);
        this.code = code;
    }

    @Override
    public String toString() {
//        String s = getClass().getName();
        int code = getCode();
        String message = getLocalizedMessage();
        return String.format("%d - %s", code, message);
    }

    public int getCode() {
        return code;
    }

    /**
     * 由于服务器传递过来的错误信息直接给用户看的话，用户未必能够理解
     * 需要根据错误码对错误信息进行一个转换，在显示给用户
     * @param code
     * @return
     */
    private static String getApiExceptionMessage(int code){
        String message;
        switch (code) {
            case USER_NOT_EXIST:
                message = "该用户不存在";
                break;
            case WRONG_PASSWORD:
                message = "密码错误";
                break;
            default:
                message = "未知错误";

        }
        return message;
    }
}

