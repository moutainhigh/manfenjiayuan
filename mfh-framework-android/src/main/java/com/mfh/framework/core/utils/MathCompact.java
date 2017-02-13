package com.mfh.framework.core.utils;

/**
 * Created by bingshanguxue on 18/10/2016.
 */

public class MathCompact {

    /**
     * 减法运算
     * */
    public static Double sub(Double d1, Double d2){
        if (d1 == null){
            return null;
        }

        return d2 != null ? d1 - d2 : d1;
    }

    /**
     * 乘法运算
     * */
    public static Double mult(Double d1, Double d2){
        if (d1 == null || d2 == null){
            return null;
        }
        return d1 * d2;
    }

    /**
     * 除法运算
     * */
    public static Double div(Double d1, Double d2){
        if (d1 == null){
            return null;
        }

        if (d2 == null || d2 == 0) {
            return d1;
        } else {
            return d1 / d2;
        }
    }
}
