package com.bingshanguxue.cashier.hardware.scale;

import com.mfh.framework.anlaysis.logger.ZLogger;

/**
 * 爱华电子计价秤(ACS-P215)
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
public class AHScaleAgent {
//    private static final boolean ENABLED_DEF = false;
//    private static final String PORT_ACS_P215_DEF = "";
//    private static final String BAUDRATE_ACS_P215_DEF = "9600";
//
//    public static final String PREF_NAME = "pref_ahscale";
//    private static final String PK_ACS_P215_ENABLED = "pk_ACS_P215_ENABLED";
//    private static final String PK_ACS_P215_PORT = "pk_ACS_P215_PORT";
//
//
//    public static String getPort(){
//        return SharedPrefesManagerFactory.getString(PREF_NAME,
//                PK_ACS_P215_PORT, AHScaleAgent.PORT_ACS_P215_DEF);
//    }
//
//    public static void setPort(String port){
//        SharedPrefesManagerFactory.set(PREF_NAME,
//                PK_ACS_P215_PORT, port);
//    }
//
//    public static String getBaudrate() {
//        return BAUDRATE_ACS_P215_DEF;
//    }
//
//    public static boolean isEnabled(){
//        return SharedPrefesManagerFactory.getBoolean(PREF_NAME,
//                PK_ACS_P215_ENABLED, ENABLED_DEF);
//    }
//
//    public static void setEnabled(boolean enabled){
//        SharedPrefesManagerFactory.set(PREF_NAME,
//                PK_ACS_P215_ENABLED, enabled);
//    }


    /**
     * 解析串口数据
     * <ol>
     *     <li><0A ></li>
     *     格式：回车
     *     <li><0D 20 30 30 34 36 ></li>
     *     <li><0D 20 30 30 30 30 ></li>
     *      cr sp 0046
     * </ol>
     * */
    public static synchronized Double parseACSP215(byte[] data) {
        try {
            if (data == null || data.length < 5) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            for (byte b : data) {
                char c = (char) b;
                if (c >= '0' && c <= '9') {
                    sb.append(c);
                }
            }

            if (sb.length() > 0){
                String dest = sb.toString();
                int val = Integer.valueOf(dest);
                return  0.001 * val;
            }

        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
        return null;
    }
}
