package com.bingshanguxue.cashier.hardware;

import com.bingshanguxue.cashier.hardware.printer.CommandConstants;
import com.mfh.framework.helper.SharedPreferencesManager;

import de.greenrobot.event.EventBus;

/**
 * POSLAB
 * <h1>通信协议</h1>
 * <ol>
 *     <li>默认使用串口5(/dev/ttymxc4)</li>
 *     <li>19200波特率，8位数字位，1位停止位，没有校验位</li>
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
public class PoslabAgent {
    private static final boolean ENABLED_DEF = false;
    public static final String PORT_DEF = "/dev/ttymxc4";
    public static final String BAUDRATE_DEF = "19200";

    public static String PREF_NAME = "pref_poslab";
    public static final String PK_DISPLAY_ENABLED = "pk_DISPLAY_ENABLED";


    public static String getPort(){
        return PORT_DEF;
    }

    public static String getBaudrate() {
        return BAUDRATE_DEF;
    }

    public static boolean isEnabled(){
        return SharedPreferencesManager.getBoolean(PREF_NAME,
                PK_DISPLAY_ENABLED, ENABLED_DEF);
    }

    public static void setEnabled(boolean enabled){
        SharedPreferencesManager.set(PREF_NAME,
                PK_DISPLAY_ENABLED, enabled);
    }

    public static void show(int sn, Double amount){
        if (sn == 1){
            //更新显示屏,显示'单价'字符
            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY,
                    CommandConstants.CMD_HEX_ESC_S_1));
        }
        else if (sn == 2){
            //更新显示屏,显示'总计'字符
            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY,
                    CommandConstants.CMD_HEX_ESC_S_2));
        }
        else if (sn == 3){
            //更新显示屏,显示'收款'字符
            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY,
                    CommandConstants.CMD_HEX_ESC_S_3));
        }
        else if (sn == 4){
            //更新显示屏,显示'找零'字符
            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY,
                    CommandConstants.CMD_HEX_ESC_S_4));
        }
//        //更新显示屏,显示'单价''总计'字符
//        EventBus.getDefault().post(new SerialPortEvent(0, CommandConstants.CMD_HEX_STX_L
//                + CommandConstants.HEX_0 + CommandConstants.HEX_1
//                + CommandConstants.HEX_1 + CommandConstants.HEX_0));

        //更新显示屏,显示商品价格
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY,
                CommandConstants.CMD_HEX_ESC_Q_A
                        + showNumber(amount) + CommandConstants.HEX_CR));
    }

    public static String showNumber(Double number){
        if (number == null){
            return "";
        }

        StringBuilder sb = new StringBuilder();

//        if (number > 0){
            String numberStr = String.format("%.2f", number);
            char[] numberCharArr = numberStr.toCharArray();
            int len = numberCharArr.length;
            for (char c : numberCharArr){
//                sb.append(String.valueOf(numberCharArr[i]));
                sb.append(Integer.toHexString((int)c));
            }
//        }else{
//            String numberStr = MUtils.formatDouble(number, "");
//            char[] numberCharArr = numberStr.toCharArray();
//            int len = numberCharArr.length;
//            for (char c : numberCharArr){
////                sb.append(String.valueOf(numberCharArr[i]));
//                sb.append(Integer.toHexString((int)c));
//            }
//        }

        return sb.toString();
    }
}
