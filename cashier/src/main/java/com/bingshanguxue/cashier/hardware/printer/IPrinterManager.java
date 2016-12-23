package com.bingshanguxue.cashier.hardware.printer;

import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.hardware.printer.gprinter.EscCommand;
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
    IPrinter create();

    /**
     * 开钱箱
     * */
    void openMoneyBox();

    /**
     * 充值单据
     * */
    void printTopupReceipt(final QuickPayInfo mQuickPayInfo, final String outTradeNo);
    EscCommand makeTopupEsc(QuickPayInfo mQuickPayInfo, String outTradeNo);

    /**
     * 收银流水，退单
     * */
    void printPosOrder(final PosOrderEntity posOrderEntity);
    EscCommand makePosOrderEsc(PosOrderEntity posOrderEntity);

    /**
     * 拣货单
     * */
    void printPrepareOrder(final ScOrder scOrder);
    EscCommand makePrepareOrderEsc(ScOrder scOrder);

    /**
     * 配送单
     * */
    void printSendOrder(final ScOrder scOrder, final int printTimes);
    EscCommand makeSendOrderEsc(ScOrder scOrder);

    /**
     * 出库单
     * */
    void printStockOutOrder(final List<StockOutItem> orderItems);
    EscCommand makeStockOutOrderEsc(List<StockOutItem> orderItems);

    /**
     * 日结
     * */
    void printDailySettleReceipt(final DailysettleInfo dailysettleInfo);
    EscCommand makeDailySettleEsc(DailysettleInfo dailysettleInfo);
    EscCommand makeHandoverTemp(EscCommand rawEsc, String name,
                                String bcount, String amount);

    /**
     * 交接班
     * */
    void printHandoverBill(final HandOverBill handOverBill);
    EscCommand makeHandoverEsc(HandOverBill handOverBill);

    /**
     * 订单流水
     * */
    void printPosOrder(final PosOrder posOrder, final int printTimes);
    EscCommand makePosOrderEsc1(PosOrder posOrder);//收银订单
    EscCommand makePosOrderEsc2(PosOrder posOrder);//平台配送单
    EscCommand makePosOrderEsc3(PosOrder posOrder);//外部平台配送单

    /**
     * 测试页
     * */
    void printTestPage();
    EscCommand makeTestPageEsc();
    void makeTestTemp(EscCommand esc, String name, String price, String bcount, String amount);


    /**
     * 模版
     * */
    void makeOrderItem1(EscCommand esc, String name, String unit, String bcount);
    void makeOrderItem2(EscCommand esc, String name, String price,
                     String bcount, String amount);
    void makeOrderItem3(EscCommand esc, String name, String bcount, String amount);
    void makeOrderItem4(EscCommand esc, String name, String bcount, String amount);





}
