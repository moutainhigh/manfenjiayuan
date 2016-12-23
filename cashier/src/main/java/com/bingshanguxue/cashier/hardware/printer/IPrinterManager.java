package com.bingshanguxue.cashier.hardware.printer;

import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.model.PosOrder;
import com.bingshanguxue.cashier.model.wrapper.DailysettleInfo;
import com.bingshanguxue.cashier.model.wrapper.HandOverBill;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.mfh.framework.api.pmcstock.StockOutItem;
import com.mfh.framework.api.scOrder.ScOrder;

import java.util.List;

/**
 * Created by bingshanguxue on 12/12/2016.
 */

public interface IPrinterManager {
    /**
     * 开钱箱
     * */
    void openMoneyBox();

    /**
     * 充值单据
     * */
    void printTopupReceipt(final QuickPayInfo mQuickPayInfo, final String outTradeNo);

    /**
     * 收银流水，退单
     * */
    void printPosOrder(final PosOrderEntity posOrderEntity);

    /**
     * 拣货单
     * */
    void printPrepareOrder(final ScOrder scOrder);

    /**
     * 配送单
     * */
    void printSendOrder(final ScOrder scOrder);

    /**
     * 出库单
     * */
    void printStockOutOrder(final List<StockOutItem> orderItems);

    /**
     * 日结
     * */
    void printDailySettleReceipt(final DailysettleInfo dailysettleInfo);

    /**
     * 交接班
     * */
    void printHandoverBill(final HandOverBill handOverBill);

    /**
     * 订单流水
     * */
    void printPosOrder(final PosOrder posOrder, final int printTimes);

    /**
     * 测试页
     * */
    void printTestPage();

}
