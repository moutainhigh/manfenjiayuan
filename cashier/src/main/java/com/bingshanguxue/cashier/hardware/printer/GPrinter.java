package com.bingshanguxue.cashier.hardware.printer;

import com.mfh.framework.helper.SharedPreferencesManager;

/**
 * GPrinter打印机()
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
public class GPrinter {
    public static final boolean ENABLED_DEF = false;
    public static final String PORT_DEF = "";
    public static final String BAUDRATE_DEF = "9600";

    private static String PREF_NAME = "pref_gprinter";
    public static final String PK_GPRINTER_ENABLED = "pk_GPRINTER_ENABLED";
    public static final String PK_GPRINTER_PORT = "pk_GPRINTER_PORT";


    public static String getPort(){
        return SharedPreferencesManager.getText(PREF_NAME,
                PK_GPRINTER_PORT, GPrinter.PORT_DEF);
    }

    public static void setPort(String port){
        SharedPreferencesManager.set(PREF_NAME,
                PK_GPRINTER_PORT, port);
    }

    public static String getBaudrate() {
        return BAUDRATE_DEF;
    }

    public static boolean isEnabled(){
        return SharedPreferencesManager.getBoolean(PREF_NAME,
                PK_GPRINTER_ENABLED, ENABLED_DEF);
    }

    public static void setEnabled(boolean enabled){
        SharedPreferencesManager.set(PREF_NAME,
                PK_GPRINTER_ENABLED, enabled);
    }
}
