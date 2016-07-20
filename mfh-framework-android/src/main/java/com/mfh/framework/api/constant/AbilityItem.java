package com.mfh.framework.api.constant;

/**
 * 能力(前后台统一)
 * Created by bingshanguxue on 16/3/2.
 */
public class AbilityItem {
    public final static Integer TENANT          = 1;    //门店
    public final static Integer CASCADE         = 8;    //生鲜批发商|市场
    public final static Integer PROVIDER        = 256;  //商超供应链|批发商

    public static String name(Integer value) {

        if (value.equals(TENANT)) {
            return "门店";
        }
        else if (value.equals(CASCADE)) {
            return "生鲜批发商|市场";
        }
        else if (value.equals(PROVIDER)) {
            return "商超供应链|批发商";
        }
        else{
            return "Unknown";
        }
    }
}
