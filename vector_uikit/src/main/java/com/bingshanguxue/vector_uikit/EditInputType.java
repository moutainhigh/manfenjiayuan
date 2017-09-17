package com.bingshanguxue.vector_uikit;

/**
 *
 * Created by bingshanguxue on 8/3/16.
 */
public interface EditInputType {
    int TEXT = 0;//text
    int NUMBER_DECIMAL = 1;//可以带小数点的浮点格式, numberDecimal
    int BARCODE = 2;//数字字符串
    int PRICE = 3;//金额，且最多保留两位有效数字
    int WEIGHT = 4;//重量，且最多保留三位有效数字
    int TEXT_PASSWORD = 5;//密码
    int PHONE = 6;//电话号码
    int NUMBER = 7;//数字，number
    int NUMBER_PASSWORD = 8;//数字，number
}
