package com.bingshanguxue.cashier.hardware.led;

import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.bingshanguxue.cashier.hardware.printer.gprinter.EscCommand;
import com.mfh.framework.core.utils.DataConvertUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.Vector;


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
public class LedAgent {

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
