package com.bingshanguxue.cashier.hardware.printer;

import com.bingshanguxue.cashier.model.PosOrder;
import com.gprinter.command.EscCommand;

/**
 * Created by bingshanguxue on 12/12/2016.
 */

public interface IPrinterManager {
    IPrinter create();

    /**
     * 打印一维条码
     * */
    void printBarcode(EscCommand esc, String barcode);

    /**
     * 收银订单
     */
    EscCommand makePosOrderEsc1(PosOrder posOrder);

    /**
     * 平台配送单
     */
    EscCommand makePosOrderEsc2(PosOrder posOrder);

    /**
     * 外部平台配送单
     */
    EscCommand makePosOrderEsc3(PosOrder posOrder);

}
