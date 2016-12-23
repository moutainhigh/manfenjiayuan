package com.bingshanguxue.cashier.hardware.printer;


import com.bingshanguxue.cashier.hardware.printer.emb.EmbPrinterManager;
import com.bingshanguxue.cashier.hardware.printer.gprinter.GPrinterManager;

/**
 * 工厂方法
 * 该类的作用是返回一个IPrinterManager对象，这个IPrinterManager的实现类分别是不同打印机的实现。
 * Created by bingshanguxue on 23/12/2016.
 */

public class PrinterFactory {

    public static IPrinterManager getPrinterManager(){
        return getPrinterManager(PrinterAgent.getPrinterType());
    }

    /**
     * @param printerModel 打印机型号
     * */
    public static IPrinterManager getPrinterManager(int printerModel){
        if (printerModel == PrinterModel.PRINTER_TYPE_COMMON){
            return GPrinterManager.getInstance();
        }
        else if (printerModel == PrinterModel.PRINTER_TYPE_EMBEDED){
            return EmbPrinterManager.getInstance();
        }
        else {
            return GPrinterManager.getInstance();
        }
    }
}
