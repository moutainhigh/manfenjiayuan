package com.mfh.framework.api.constant;

/**
 * 认证标志 1-个人认证  2-企业认证  3-全是
 * Created by bingshanguxue on 9/20/16.
 */
public class HumanAuthFlag {
    public final static Integer NA = 1;//未认证
    public final static Integer INDIVIDUAL = 1;//个人认证
    public final static Integer ENTERPRISE = 2;//企业认证
    public final static Integer FULL =3;//全是

    public static String name(Integer value) {
        if (NA.equals(value)) {
            return "未认证";
        }
        else if (INDIVIDUAL.equals(value)) {
            return "个人认证";
        } else if (ENTERPRISE.equals(value)) {
            return "企业认证";
        } else if (FULL.equals(value)) {
            return "个人认证&企业认证";
        } else {
            return "Unknow";
        }
    }
}
