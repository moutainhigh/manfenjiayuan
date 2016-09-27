package com.bingshanguxue.vector_uikit;

/**
 *
 * Created by bingshanguxue on 8/3/16.
 */
public interface EditInputType {
    int TEXT = 0;//text
    int NUMBER_DECIMAL = 1;//numberDecimal
    int BARCODE = 2;//数字字符串
    int PRICE = 3;//金额，且最多保留两位有效数字
    int WEIGHT = 4;//重量，且最多保留两位有效数字
    int TEXT_PASSWORD = 5;//密码

}
