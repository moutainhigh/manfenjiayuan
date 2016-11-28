package com.bingshanguxue.cashier.hardware.printer;

import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.printer.sdk.PrinterConstants;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Vector;

import de.greenrobot.event.EventBus;

/**
 * GPrinter打印机&钱箱
 * <h1>通信协议</h1>
 * <ol>
 *     <li>9600波特率，8位数字位，1位停止位，没有校验位</li>
 * </ol>
 * <h1>开/关发送数据流</h1>
 * <ol>
 *     <li>按“清除“+"0"选择通讯模式。</li>
 *     <li>RS232-0,不发送；RS232-1,连续发送；RS232-2,稳定发送;RS232-3,P 应答模式；RS232-4, $应答模式</li>
 *     应用默认选择RS232-2,稳定发送模式，所以出场时需要正确配置！
 *     <li>按“去皮”设置完成</li>
 * </ol>
 *
 * Created by bingshanguxue on 5/27/16.
 */
public class EmbPrinter {

    public static byte[] initPrinter() {
        return new byte[]{(byte)27, (byte)64};
    }

    public static byte[] setPrinter(int command, int value) {
        byte[] arrayOfByte = new byte[3];
        switch(command) {
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
                if(value > 2 || value < 0) {
                    value = 0;
                }
        }

        arrayOfByte[2] = (byte)value;
        return arrayOfByte;
    }

    public static byte[] setFont(int mCharacterType, int mWidth, int mHeight, int mBold, int mUnderline) {
        byte mFontSize = 0;
        byte mFontMode = 0;
        if(mBold != 0 && mBold != 1) {
            mBold = 0;
        }

        byte mFontMode1 = (byte)(mFontMode | mBold << 3);
        if(mUnderline != 0 && mUnderline != 1) {
            mUnderline = 0;
        }

        mFontMode1 = (byte)(mFontMode1 | mUnderline << 7);
        if(mCharacterType != 0 && mCharacterType != 1) {
            mCharacterType = 0;
        }

        mFontMode1 = (byte)(mFontMode1 | mCharacterType << 0);
        if(mWidth < 0 || mWidth > 7) {
            mWidth = 0;
        }

        byte mFontSize1 = (byte)(mFontSize | mWidth << 4);
        if(mHeight < 0 | mHeight > 7) {
            mHeight = 0;
        }

        mFontSize1 = (byte)(mFontSize1 | mHeight);
        return new byte[]{(byte)27, (byte)33, mFontMode1, (byte)29, (byte)33, mFontSize1};
    }


    /**
     * 打印
     * */
    public static void print(EscCommand escCommand){
        if (escCommand != null) {
            //获得打印命令
            Vector<Byte> datas = escCommand.getCommand();//发送数据
            Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
            byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.GPRINTER_SEND_DATA, bytes));
        }
    }

    /**
     * 走纸
     * */
    public static void feedPaper(){
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());

        //打印并且走纸多少行
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 5));

        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.GPRINTER_SEND_DATA, bytes));
    }

    /**
     * 打开钱箱
     * */
    public static void openMoneyBox(){
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY,
                CommandConstants.CMD_HEX_STX_M));

        EscCommand esc = new EscCommand();
        esc.addGeneratePluseAtRealtime(LabelCommand.FOOT.F2, (byte)20);
        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.GPRINTER_SEND_DATA, bytes));
    }

    /**
     * 清除屏幕上的字符
     * */
    public static void clear(){
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY, CommandConstants.CMD_HEX_CLR));
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD, ""));
        vfdShow("WELCOME");
    }

    /**
     * 显示字符
     * */
    public static void vfdShow(String text){
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD_BYTE,
                VFD_CH(text)));
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD, text));
    }

    /**
     * 将字符串转换成VFD格式显示
     *
     * 0x1b0x40 -- Max add : Initialize device(清空)
     */
    public static byte[] VFD(String displayText){
        EscCommand esc = new EscCommand();

//        String cmdStr4 = "1B40" + String.format("%02X", (byte) (displayText.length())) + DataConvertUtil.ByteArrToHex(displayText.getBytes(), "");
        String cmdStr4 = "1B40" + DataConvertUtil.ByteArrToHex(displayText.getBytes(), "");
//        ZLogger.d(String.format("VFD: %s, %s", displayText, cmdStr4));
        esc.addUserCommand(DataConvertUtil.HexToByteArr(cmdStr4));

        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        return ArrayUtils.toPrimitive(Bytes);
    }


    /**
     * 显示中文字符
     * */
    public static byte[] VFD_CH(String displayText){
        EscCommand esc = new EscCommand();

        String cmdStr4  = "1B40" + DataConvertUtil.ByteArrToHex(displayText.getBytes(), "");
//        String cmdStr4;
//        try {
//            cmdStr4 = "1B40" + DataConvertUtil.ByteArrToHex(displayText.getBytes("GB2312"), "");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            cmdStr4 = "1B40";
//        }
//        ZLogger.d(String.format("VFD: %s, %s", displayText, cmdStr4));
        esc.addUserCommand(DataConvertUtil.HexToByteArr(cmdStr4));
//        esc.addUserCommand(new byte[]{'\r'});
//        esc.addUserCommand(new byte[]{'\n'});
//        esc.addUserCommand(DataConvertUtil.HexToByteArr("111"));

        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        return ArrayUtils.toPrimitive(Bytes);
    }

//    public static String SimToTra(String simpStr) {
//        String traditionalStr = null;
//        try {
//            JChineseConvertor jChineseConvertor = JChineseConvertor
//                    .getInstance();
//            traditionalStr = jChineseConvertor.s2t(simpStr);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return traditionalStr;
//    }


}
