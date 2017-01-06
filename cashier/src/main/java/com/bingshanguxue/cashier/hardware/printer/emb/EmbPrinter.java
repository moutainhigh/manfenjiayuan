package com.bingshanguxue.cashier.hardware.printer.emb;

import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.bingshanguxue.cashier.hardware.led.LedProtocol;
import com.bingshanguxue.cashier.hardware.printer.Printer;
import com.bingshanguxue.cashier.hardware.printer.gprinter.EscCommand;
import com.bingshanguxue.cashier.hardware.printer.gprinter.LabelCommand;

import org.greenrobot.eventbus.EventBus;


/**
 * GPrinter打印机&钱箱
 * <h1>通信协议</h1>
 * <ol>
 * <li>9600波特率，8位数字位，1位停止位，没有校验位</li>
 * </ol>
 * <p>
 * Created by bingshanguxue on 5/27/16.
 */
public class EmbPrinter extends Printer {

    public static byte[] setPrinter(int command, int value) {
        byte[] arrayOfByte = new byte[3];
        switch (command) {
            case 0:
                arrayOfByte[0] = 27;
                arrayOfByte[1] = 74;
                break;
            case 1:
                arrayOfByte[0] = 27;
                arrayOfByte[1] = 100;
                break;
            case 4:
                arrayOfByte[0] = 27;
                arrayOfByte[1] = 86;
                break;
            case 11:
                arrayOfByte[0] = 27;
                arrayOfByte[1] = 32;
                break;
            case 13:
                arrayOfByte[0] = 27;
                arrayOfByte[1] = 97;
                if (value > 2 || value < 0) {
                    value = 0;
                }
        }

        arrayOfByte[2] = (byte) value;
        return arrayOfByte;
    }

    /**
     * 设置打印机字体
     * @param mCharacterType 0 表示 12*24 字体大小，1 表示 9*16 字体大小，此设置临 时有效，
     *                       打印机不保存此设置，即打印机重启后无效。
     *                       如需打印 9*16 大小 字体，需要每次传入 1 手动设置
     * @param mWidth 倍宽，范围 0~7
     * @param mHeight 倍高，范围 0~7
     * @param mBold 0 不加粗，1 加粗
     * @param mUnderline 0 无下划线，1 下划线
     *
     *
     * <table>
     *     <tr>
     *        <td>格式</td>
     *        <table>
     *            <tr><td>ASCII码</td><td>ESC</td><td>@</td></tr>
     *            <tr><td>十六进制码</td><td>1B</td><td>40</td></tr>
     *            <tr><td>十进制码</td><td>27</td><td>64</td></tr>
     *        </table>
     *     </tr>
     *     <tr>
     *         <td>描述</td>
     *         <td>设置打印机打印字体，包括设置加粗，倍高，倍宽，下划线， 标准 ASCII 字体 A (12 × 24)，压缩 ASCII 字体 B (9 × 17)</td>
     *     </tr>
     *     <tr>
     *         <td>注释</td>
     *         <td>
     *             <li>DIP开关的设置不进行再次检测。</li>
     *             <li>接收缓冲区中的数据保留。</li>
     *             <li>NV位图数据不擦除。</li>
     *             <li>用户NV存储器数据不擦除。</li>
     *         </td>
     *     </tr>
     * </table>
     * */
    public static byte[] setFont(int mCharacterType, int mWidth, int mHeight, int mBold, int mUnderline) {
        byte mFontSize = 0;
        byte mFontMode = 0;
        if (mBold != 0 && mBold != 1) {
            mBold = 0;
        }

        byte mFontMode1 = (byte) (mFontMode | mBold << 3);
        if (mUnderline != 0 && mUnderline != 1) {
            mUnderline = 0;
        }

        mFontMode1 = (byte) (mFontMode1 | mUnderline << 7);
        if (mCharacterType != 0 && mCharacterType != 1) {
            mCharacterType = 0;
        }

        mFontMode1 = (byte) (mFontMode1 | mCharacterType << 0);
        if (mWidth < 0 || mWidth > 7) {
            mWidth = 0;
        }

        byte mFontSize1 = (byte) (mFontSize | mWidth << 4);
        if (mHeight < 0 | mHeight > 7) {
            mHeight = 0;
        }

        mFontSize1 = (byte) (mFontSize1 | mHeight);
        return new byte[]{(byte) 27, (byte) 33, mFontMode1, (byte) 29, (byte) 33, mFontSize1};
    }

    /**
     * 走纸
     */
    @Override
    public void feedPaper() {
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());

        printAndLineFeed(esc, 3);

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
        esc.addUserCommand(EmbPrinter.initPrinter());
        esc.addGeneratePluseAtRealtime(LabelCommand.FOOT.F2, (byte) 20);

        print(esc);
    }

    @Override
    public void printAndLineFeed(EscCommand escCommand, int lines) {
        if (escCommand == null) {
            return;
        }
        byte[] command = EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, lines);
        escCommand.addUserCommand(command);
    }

    @Override
    public void addCODE128(EscCommand esc, String barcode) {
        if (esc == null) {
            return;
        }

        //设置条码可识别字符位置在条码下方
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        //设置条码高度为 60 点
        esc.addSetBarcodeHeight((byte) 60);
        //设置条码单元宽度为1点
        esc.addSetBarcodeWidth((byte) 1);

        Barcode barcode1 = new Barcode(PrinterConstants.BarcodeType.CODE128, 3,
                80, 2, barcode);
        esc.addUserCommand(barcode1.getBarcodeData());
    }
}
