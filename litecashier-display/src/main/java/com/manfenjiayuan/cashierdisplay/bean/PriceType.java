package com.manfenjiayuan.cashierdisplay.bean;

/**
 * 价格类型(前后台统一)
 * Created by bingshanguxue on 16/3/2.
 */
public class PriceType {
    public final static Integer PIECE       = 0;//计件
    public final static Integer WEIGHT      = 1;//计重

    public static String name(Integer value) {
        if (value.equals(PIECE)) {
            return "计件";
        }
        else if (value.equals(WEIGHT)) {
            return "计重";
        }
        else{
            return "Unknow";
        }
    }
}
