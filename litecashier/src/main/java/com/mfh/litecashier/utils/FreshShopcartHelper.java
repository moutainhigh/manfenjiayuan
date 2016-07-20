package com.mfh.litecashier.utils;

/**
 * Created by bingshanguxue on 5/5/16.
 */
public class FreshShopcartHelper extends ShopcartHelper {
    private static FreshShopcartHelper instance;

    public static FreshShopcartHelper getInstance() {
        if (instance == null) {
            synchronized (FreshShopcartHelper.class) {
                if (instance == null) {
                    instance = new FreshShopcartHelper();
                }
            }
        }
        return instance;
    }
}
