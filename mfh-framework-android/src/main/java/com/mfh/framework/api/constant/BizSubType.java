package com.mfh.framework.api.constant;

/**
 * 业务类型子类型(前后台统一)
 * Created by bingshanguxue on 16/3/2.
 */
public class BizSubType {
    public final static Integer POS_STANDARD    = 0;//线下商超－标品
    public final static Integer POS_FRESH       = 2;//线下商超－生鲜
    public final static Integer POS_SMOKE       = 3;//线下商超－香烟
    public final static Integer POS_BAKING      = 4;//线下商超－烘培

    public static String name(Integer value) {
        if (value == null){
            return "";
        }
        if (value.equals(POS_STANDARD)) {
            return "社区超市－标品";
        }
        else if (value.equals(POS_FRESH)) {
            return "社区超市－生鲜";
        }
        else if (value.equals(POS_SMOKE)) {
            return "社区超市－香烟";
        }
        else if (value.equals(POS_BAKING)) {
            return "社区超市－烘培";
        }
        else{
            return "Unknow";
        }
    }
}
