package com.mfh.framework.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式
 *
 * Created by bingshanguxue on 8/3/16.
 */
public class RegularUtils {
    /**验证手机号*/
    private static final String MOBILE = "^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$";


    /**
     * 是不是手机号
     * 10690327313684962716 通知
     * */
    public static boolean isMobile(String text) {
        if(text != null) {
            Pattern p = Pattern.compile(MOBILE);
            Matcher m = p.matcher(text);
            if (m == null) {
                return false;
            }
            else {
                return m.matches();
            }
        }
        return false;
    }

}
