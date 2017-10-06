package com.mfh.framework.rxapi.http;

/**
 * 自定义服务器api异常信息
 * Created by bingshanguxue on 16/12/27.
 */
public class ApiException extends RuntimeException {

    public static final String NULL = "NULL";
    private String code;


    public ApiException(String message, String code) {
        super(message);
        this.code = code;
    }

    public ApiException(String code) {
        this(getApiExceptionMessage(code), code);
        this.code = code;
    }

    @Override
    public String toString() {
//        String s = getClass().getName();
        String code = getCode();
        String message = getLocalizedMessage();
        return String.format("%s - %s", code, message);
    }

    public String getCode() {
        return code;
    }

    /**
     * 由于服务器传递过来的错误信息直接给用户看的话，用户未必能够理解
     * 需要根据错误码对错误信息进行一个转换，在显示给用户
     *
     * @param code
     * @return
     */
    private static String getApiExceptionMessage(String code) {
        String message;
        switch (code) {
            case ErrorCode.USER_NOT_EXIST:
                message = "该用户不存在";
                break;
            case ErrorCode.WRONG_PASSWORD:
                message = "密码错误";
                break;
            default:
                message = "未知错误";

        }
        return message;
    }


}

