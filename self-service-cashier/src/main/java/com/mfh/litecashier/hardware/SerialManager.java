package com.mfh.litecashier.hardware;

import com.bingshanguxue.cashier.hardware.led.PoslabAgent;
import com.bingshanguxue.cashier.hardware.printer.PrinterAgent;
import com.bingshanguxue.cashier.hardware.scale.ScaleProvider;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 串口
 * Created by ZZN.NAT(bingshanguxue) on 15/11/17.
 */
public class SerialManager {
    //屏显（JOOYTEC）
//    public static final String PORT_SCREEN = "/dev/ttyS1";
//    public static final String BAUDRATE_SCREEN = "2400";

    //银联
    public static final String PORT_UMSIPS      = "/dev/ttymxc2";
    public static final String BAUDRATE_UMSIPS  = "9600";

    private List<String> comDevicesPath;//串口信息

    public static final String PREF_NAME_SERIAL = "PREF_NAME_SERIAL";
    public static final String PK_UMSIPS_PORT = "prefkey_umsips_port";
    public static final String PK_UMSIPS_BAUDRATE = "prefkey_umsips_baudrate";

    private static SerialManager instance;

    public static SerialManager getInstance() {
        if (instance == null) {
            synchronized (SerialManager.class) {
                if (instance == null) {
                    instance = new SerialManager();
                }
            }
        }
        return instance;
    }

    public SerialManager() {
        comDevicesPath = new ArrayList<>();
    }

//    /**
//     * 初始化
//     * */
//    public void initialize(){
//        Map<String, String> tem = new HashMap<>();
//        if (AHScaleAgent.isEnabled(PREF_NAME_SERIAL)){
//            String port = AHScaleAgent.getPort(PREF_NAME_SERIAL);
//            if (!StringUtils.isEmpty(port) && !tem.containsKey(port)){
//                tem.put(port, "爱华电子秤");
//            }
//            else{
//                AHScaleAgent.setPort(PREF_NAME_SERIAL);
//            }
//        }
//
//        occupies = tem;
//    }
//

    public List<String> getComDevicesPath() {
        return comDevicesPath;
    }

    public void setComDevicesPath(List<String> comDevicesPath) {
        this.comDevicesPath = comDevicesPath;
    }

    public List<String> getAvailablePath(String port) {
        List<String> occupies = new ArrayList<>();
        if (comDevicesPath != null){
            occupies.addAll(comDevicesPath);
        }

        //打印机的串口被固定占用，不可以更改
        occupies.remove(PrinterAgent.getPort());
        //电子秤的串口被固定占用，不可以更改
        occupies.remove(ScaleProvider.getPort());
        //LED客显的串口被固定占用，不可以更改
        occupies.remove(PoslabAgent.getPort());

        String umsipsPort = getUmsipsPort();
        if (!StringUtils.isEmpty(umsipsPort)){
            occupies.remove(umsipsPort);
        }

        if (!StringUtils.isEmpty(port) && !occupies.contains(port)){
            occupies.add(port);
        }

        ZLogger.d("occupies devicePath:" + occupies.toString());
        return occupies;
    }


    public static String getUmsipsPort() {
        return SharedPrefesManagerFactory.getString(PREF_NAME_SERIAL, PK_UMSIPS_PORT, PORT_UMSIPS);
    }

    public static void setUmsipsPort(String port){
        SharedPrefesManagerFactory.set(PREF_NAME_SERIAL, PK_UMSIPS_PORT, port);
    }

    public static String getUmsipsBaudrate() {
        return SharedPrefesManagerFactory.getString(PREF_NAME_SERIAL, PK_UMSIPS_BAUDRATE, BAUDRATE_UMSIPS);
    }

    public static void setUmsipsBaudrate(String baudrate){
        SharedPrefesManagerFactory.set(PREF_NAME_SERIAL, PK_UMSIPS_BAUDRATE, baudrate);
    }




}
