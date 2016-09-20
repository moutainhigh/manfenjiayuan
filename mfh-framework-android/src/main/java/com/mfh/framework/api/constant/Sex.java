package com.mfh.framework.api.constant;

/**
 * 性别
 * Created by bingshanguxue on 9/20/16.
 */
public class Sex {
    public final static Integer MALE = 0;//男
    public final static Integer FEMALE = 1;//女
    public final static Integer UNKNOWN = -1;//未知

    public static String name(Integer value) {
        if (MALE.equals(value)) {
            return "男";
        } else if (FEMALE.equals(value)) {
            return "女";
        } else if (UNKNOWN.equals(value)) {
            return "未知";
        } else {
            return "Unknow";
        }
    }
    public static String formatName1(Integer value) {
        if (MALE.equals(value)) {
            return "先生";
        } else if (FEMALE.equals(value)) {
            return "女士";
        } else if (UNKNOWN.equals(value)) {
            return "未知";
        } else {
            return "Unknow";
        }
    }
}
