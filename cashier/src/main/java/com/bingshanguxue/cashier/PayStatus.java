package com.bingshanguxue.cashier;

/**
 * 支付状态
 * Created by bingshanguxue on 8/8/16.
 */
public interface PayStatus {
    int INIT     = 0;//初始状态
    int STAY_PAY = 1;//等待支付
    int PROCESS  = 2;//支付处理中
    int EXCEPTION= 3;//支付异常
    int FINISH   = 4;//支付完成
    int FAILED   = 5;//支付失败
    int CANCELED = 6;//交易取消
    int REFUND   = 7;//退款
}
