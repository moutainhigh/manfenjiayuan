package com.bingshanguxue.pda.utils;

import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.utils.ACache;
/**
 * Created by bingshanguxue on 5/5/16.
 */
public class ACacheHelper {
    public static String CACHE_NAME = "ACache";

    public static final String INVRECV_INSPECT_GOODS_TEMPDATA = "invRecv_inspect_goods_tempdata";

    public static void put(String key, String value){
        ACache.get(MfhApplication.getAppContext(), ACacheHelper.CACHE_NAME)
                .put(key, value);
    }

    public static String getAsString(String key){
        return ACache.get(MfhApplication.getAppContext(), ACacheHelper.CACHE_NAME)
                .getAsString(key);
    }

    public static boolean remove(String key){
        return ACache.get(MfhApplication.getAppContext(), ACacheHelper.CACHE_NAME)
                .remove(key);
    }

    public static void clear(){
        ACache.get(MfhApplication.getAppContext(), ACacheHelper.CACHE_NAME).clear();
    }


}
