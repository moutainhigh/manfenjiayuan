package com.bingshanguxue.cashier.hardware.printer.gprinter;

import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.bingshanguxue.cashier.hardware.led.LedProtocol;
import com.bingshanguxue.cashier.hardware.printer.Printer;

import org.greenrobot.eventbus.EventBus;


/**
 * GPrinter打印机&钱箱
 * <h1>通信协议</h1>
 * <ol>
 * <li>9600波特率，8位数字位，1位停止位，没有校验位</li>
 * </ol>
 * Created by bingshanguxue on 08/12/2016.
 */

public class GPrinter extends Printer {

    /**
     * 走纸
     */
    @Override
    public void feedPaper() {
        EscCommand esc = new EscCommand();
        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);

        print(esc);
    }

    /**
     * 打开钱箱
     */
    @Override
    public void openMoneyBox() {
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY,
                LedProtocol.CMD_HEX_STX_M));

        EscCommand esc = new EscCommand();
        esc.addGeneratePluseAtRealtime(LabelCommand.FOOT.F2, (byte) 20);

        print(esc);
    }

    @Override
    public void printAndLineFeed(EscCommand escCommand, int lines) {
        if (escCommand == null) {
            return;
        }
        escCommand.addPrintAndFeedLines((byte) lines);//打印并且走纸3行
    }

    @Override
    public void addCODE128(EscCommand esc, String barcode) {
        if (esc == null) {
            return;
        }

        // ：设置条码可识别字符位置在条码下方
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        //设置条码高度为 60 点
        esc.addSetBarcodeHeight((byte) 60);
        //设置条码单元宽度为1点
        esc.addSetBarcodeWidth((byte) 1);
        esc.addCODE128(esc.genCode128(barcode));
    }
}
