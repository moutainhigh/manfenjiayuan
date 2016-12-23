package com.bingshanguxue.cashier.hardware.printer;

/**
 * Created by bingshanguxue on 24/12/2016.
 */

public class PrinterContract {
    /**
     * 小票
     */
    public static class Receipt{
        public static final int CASHIER_ORDER = 0;//收银订单
        public static final int PREPARE_ORDER = 1;//拣货单
        public static final int SEND_ORDER = 2;//配送单
        public static final int SEND_ORDER_3P = 3;//外部配送单
        public static final int ANALYSIS = 4;//日结
        public static final int STOCKOUT = 5;//出库单

    }
}
