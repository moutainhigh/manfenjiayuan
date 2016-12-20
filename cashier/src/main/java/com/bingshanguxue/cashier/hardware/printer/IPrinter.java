package com.bingshanguxue.cashier.hardware.printer;

import com.gprinter.command.EscCommand;

/**
 * Created by bingshanguxue on 12/12/2016.
 */

public interface IPrinter {

    void feedPaper();

    void openMoneyBox();

    /**
     * 打印并且走纸
     * */
    void printAndLineFeed(EscCommand escCommand, int lines);


}
