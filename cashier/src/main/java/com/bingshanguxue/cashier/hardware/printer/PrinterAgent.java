package com.bingshanguxue.cashier.hardware.printer;

import com.mfh.framework.prefs.SharedPrefesUltimate;

import java.util.Observable;


/**
 * Created by bingshanguxue on 27/11/2016.
 */

public class PrinterAgent extends Observable {
    private static final boolean ENABLED_DEF = true;
    private static final int PRINTER_TYPE_DEF = PrinterModel.PRINTER_TYPE_COMMON;//0,外接；1,嵌入式
    private static final int PRINT_TIMES_DEF = 1;
    private static final String PORT_DEF = "/dev/ttymxc0";
    private static final String BAUDRATE_DEF = "9600";

    public static final String PREF_NAME = "pref_printer";
    private static final String PK_ENABLED = "PK_ENABLED";
    private static final String PK_PRINTER_TYPE = "PK_PRINTER_TYPE";//打印机型号
    private static final String PK_I_PRINTTIMES_CASHIER = "PK_I_PRINTTIMES_CASHIER";//收银打印次数
    private static final String PK_I_PRINTTIMES_PREPAREORDER = "PK_I_PRINTTIMES_PREPAREORDER";//拣货单打印次数
    private static final String PK_I_PRINTTIMES_SENDORDER = "PK_I_PRINTTIMES_SENDORDER";//配送单打印次数
    private static final String PK_I_PRINTTIMES_SENDORDER_3P = "PK_I_PRINTTIMES_SENDORDER_3P";//外部配送单打印次数
    private static final String PK_I_PRINTTIMES_ANALYSIS = "PK_I_PRINTTIMES_ANALYSIS";//日结单打印次数
    private static final String PK_I_PRINTTIMES_STOCKOUT = "PK_I_PRINTTIMES_STOCKOUT";//出库单打印次数

    private static PrinterAgent instance = null;

    /**
     * 返回 PrintManagerImpl 实例
     *
     * @return PrintManagerImpl
     */
    public static PrinterAgent getInstance() {
        if (instance == null) {
            synchronized (PrinterAgent.class) {
                if (instance == null) {
                    instance = new PrinterAgent();
                }
            }
        }

        return instance;
    }


    public static String getPort() {
        return PORT_DEF;
    }

    public static String getBaudrate() {
        return BAUDRATE_DEF;
    }

    public static boolean isEnabled() {
        return SharedPrefesUltimate.getBoolean(PREF_NAME, PK_ENABLED, ENABLED_DEF);
    }

    public static void setEnabled(boolean enabled) {
        SharedPrefesUltimate.set(PREF_NAME, PK_ENABLED, enabled);
    }

    public static int getPrinterType() {
        return SharedPrefesUltimate.getInt(PREF_NAME, PK_PRINTER_TYPE, PRINTER_TYPE_DEF);
    }

    public static void setPrinterType(int type) {
        SharedPrefesUltimate.set(PREF_NAME, PK_PRINTER_TYPE, type);
    }

    public int getPrinterTimes(int receipt) {
        int printTimes = 0;
        switch (receipt) {
            case PrinterContract.Receipt.CASHIER_ORDER: {
                printTimes = SharedPrefesUltimate.getInt(PREF_NAME, PK_I_PRINTTIMES_CASHIER, PRINT_TIMES_DEF);
            }
            break;
            case PrinterContract.Receipt.PREPARE_ORDER: {
                printTimes =  SharedPrefesUltimate.getInt(PREF_NAME, PK_I_PRINTTIMES_PREPAREORDER, PRINT_TIMES_DEF);
            }
            break;
            case PrinterContract.Receipt.SEND_ORDER: {
                printTimes = SharedPrefesUltimate.getInt(PREF_NAME, PK_I_PRINTTIMES_SENDORDER, PRINT_TIMES_DEF);
            }
            break;
            case PrinterContract.Receipt.SEND_ORDER_3P: {
                printTimes = SharedPrefesUltimate.getInt(PREF_NAME, PK_I_PRINTTIMES_SENDORDER_3P, PRINT_TIMES_DEF);
            }
            break;
            case PrinterContract.Receipt.ANALYSIS: {
                printTimes = SharedPrefesUltimate.getInt(PREF_NAME, PK_I_PRINTTIMES_ANALYSIS, PRINT_TIMES_DEF);
            }
            break;
            case PrinterContract.Receipt.STOCKOUT: {
                printTimes = SharedPrefesUltimate.getInt(PREF_NAME, PK_I_PRINTTIMES_STOCKOUT, PRINT_TIMES_DEF);
            }
            break;
            default: {
                break;
            }
        }

        return printTimes;
    }

    public void setCashierPrinterTimes(int receipt, int times) {
        switch (receipt) {
            case PrinterContract.Receipt.CASHIER_ORDER: {
                SharedPrefesUltimate.set(PREF_NAME, PK_I_PRINTTIMES_CASHIER, times);            }
            break;
            case PrinterContract.Receipt.PREPARE_ORDER: {
                SharedPrefesUltimate.set(PREF_NAME, PK_I_PRINTTIMES_PREPAREORDER, times);
            }
            break;
            case PrinterContract.Receipt.SEND_ORDER: {
                SharedPrefesUltimate.set(PREF_NAME, PK_I_PRINTTIMES_SENDORDER, times);
            }
            break;
            case PrinterContract.Receipt.SEND_ORDER_3P: {
                SharedPrefesUltimate.set(PREF_NAME, PK_I_PRINTTIMES_SENDORDER_3P, times);
            }
            break;
            case PrinterContract.Receipt.ANALYSIS: {
                SharedPrefesUltimate.set(PREF_NAME, PK_I_PRINTTIMES_ANALYSIS, times);
            }
            break;
            case PrinterContract.Receipt.STOCKOUT: {
                SharedPrefesUltimate.set(PREF_NAME, PK_I_PRINTTIMES_STOCKOUT, times);
            }
            break;
            default: {
                break;
            }
        }

        setChanged();    //标记此 Observable对象为已改变的对象
        notifyObservers();    //通知所有观察者
    }


    public static String getPrinterName() {
        int scaleType = getPrinterType();
        if (scaleType == PrinterModel.PRINTER_TYPE_COMMON) {
            return "分离式";
        } else if (scaleType == PrinterModel.PRINTER_TYPE_EMBEDED) {
            return "嵌入式";
        } else {
            return null;
        }
    }

}
