package com.bingshanguxue.cashier.hardware.printer;


import com.bingshanguxue.cashier.hardware.printer.gprinter.EscCommand;

/**
 * Created by bingshanguxue on 12/12/2016.
 */

public interface IPrinter {

    /**
     * 走纸,默认3行
     * */
    void feedPaper();

    /**
     * 开钱箱
     * */
    void openMoneyBox();

    /**
     * 打印并且走纸
     * */
    void printAndLineFeed(EscCommand escCommand, int lines);

    /**
     * 打印CODE128条码
     * */
    void addCODE128(EscCommand esc, String barcode);
}
