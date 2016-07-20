package com.mfh.framework.api.constant;

/**
 * 供应商类型(前后台统一)对应isPrivate/controlType字段
 *
 * Created by bingshanguxue on 16/3/2.
 */
public class IsPrivate {
    public final static Integer PLATFORM        = 0;//平台
    public final static Integer PRIVATE         = 1;//自采,controlType=1
    public final static Integer UNIFORM         = 3;//统采,controlType=0
    public final static Integer FRESH_SCHEDULE  = 5;//生鲜预定

    public static String name(Integer value) {
        if (value.equals(PLATFORM)) {
            return "平台";
        }
        else if (value.equals(PRIVATE)) {
            return "自采";
        }
        else if (value.equals(UNIFORM)) {
            return "统采";
        }
        else if (value.equals(FRESH_SCHEDULE)) {
            return "生鲜预定";
        }
        else{
            return "Unknow";
        }
    }
}
