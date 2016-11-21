package com.mfh.litecashier.com;

import com.bingshanguxue.cashier.hardware.PoslabAgent;
import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.bingshanguxue.cashier.hardware.printer.GPrinterAgent;
import com.bingshanguxue.cashier.hardware.scale.AHScaleAgent;
import com.bingshanguxue.cashier.hardware.scale.SMScaleAgent;
import com.gprinter.command.EscCommand;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.greenrobot.event.EventBus;

/**
 * 串口
 * Created by ZZN.NAT(bingshanguxue) on 15/11/17.
 */
public class SerialManager {
    //屏显（JOOYTEC）
//    public static final String PORT_SCREEN = "/dev/ttyS1";
//    public static final String BAUDRATE_SCREEN = "2400";

    //银联
    public static final String PORT_UMSIPS      = "/dev/ttymxc0";
    public static final String BAUDRATE_UMSIPS  = "9600";

    private List<String> comDevicesPath;//串口信息

    public static final String PREF_NAME_SERIAL = "PREF_NAME_SERIAL";
    public static final String PREF_KEY_PRINTER_PORT = "PREF_KEY_PRINTER_PORT";
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

        if (AHScaleAgent.isEnabled()){
            String ahPort = AHScaleAgent.getPort();
            if (!StringUtils.isEmpty(ahPort)){
                occupies.remove(ahPort);
            }
        }

        if (SMScaleAgent.isEnabled()){
            String smPort = SMScaleAgent.getPort();
            if (!StringUtils.isEmpty(smPort)){
                occupies.remove(smPort);
            }
        }

        String gprinterPort = getPrinterPort();
        if (!StringUtils.isEmpty(gprinterPort)){
            occupies.remove(gprinterPort);
        }

        if (PoslabAgent.isEnabled()){
            String ledPort = PoslabAgent.getPort();
            if (!StringUtils.isEmpty(ledPort)){
                occupies.remove(ledPort);
            }
        }

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


    public static String getPrinterPort() {
        return SharedPrefesManagerFactory.getString(
                PREF_NAME_SERIAL, PREF_KEY_PRINTER_PORT, GPrinterAgent.PORT_DEF);
    }

    public static void setPrinterPort(String port){
        SharedPrefesManagerFactory.set(PREF_NAME_SERIAL, PREF_KEY_PRINTER_PORT, port);
    }

    public static String getUmsipsPort() {
        return SharedPrefesManagerFactory.getString(
                PREF_NAME_SERIAL, PK_UMSIPS_PORT, PORT_UMSIPS);
    }

    public static void setUmsipsPort(String port){
        ZLogger.df(String.format("setUMSIPSPort(%s):%s", PREF_NAME_SERIAL, port));
        SharedPrefesManagerFactory.set(PREF_NAME_SERIAL, PK_UMSIPS_PORT, port);
    }

    public static String getUmsipsBaudrate() {
        return SharedPrefesManagerFactory.getString(
                PREF_NAME_SERIAL, PK_UMSIPS_BAUDRATE, BAUDRATE_UMSIPS);
    }

    public static void setUmsipsBaudrate(String baudrate){
        SharedPrefesManagerFactory.set(
                PREF_NAME_SERIAL, PK_UMSIPS_BAUDRATE, baudrate);
    }

//    public EscCommand addCODE128(EscCommand rawEsc, String content) {
//        EscCommand esc = rawEsc;
//        if (esc == null){
//            esc = new EscCommand();
//        }
//        byte[] command = new byte[]{(byte)29, (byte)107, (byte)73, (byte)content.length()};
//        esc.addArrayToCommand(command);
//        esc.addText(content);
//
//        return esc;
//    }

    /**
     * format like "{B01234567"
     * 最多支持打印8位条码
     * */
    public static void printBarcode(String barCode){
        EscCommand esc = new EscCommand();
//        esc.addPrintAndLineFeed();
        /*打印一维条码code128*/
        // 设置条码可识别字符位置在条码下方
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        //设置条码高度为 60 点
        esc.addSetBarcodeHeight((byte) 60);

        String cmdStr4 = "1D6B49" + String.format("%02X", (byte) (barCode.length())) + DataConvertUtil.ByteArrToHex(barCode.getBytes(), "");
//        ZLogger.d(String.format("printBarCode: %s, %s", barCode, cmdStr4));

        esc.addUserCommand(DataConvertUtil.HexToByteArr(cmdStr4));

        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);

        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.GPRINTER_SEND_DATA, bytes));
    }

    public static void printBarcode(){
        EscCommand esc = new EscCommand();

//                    esc.addPrintAndLineFeed();
        /**打印 订单号条形码code128图片*/
//        try {
//            Bitmap QRCodeBmp = QRCodeUtils.CreateCode128ForGPrinter(String.valueOf(orderEntity.getId()), 300, 60);
//            esc.addRastBitImage(QRCodeBmp, QRCodeBmp.getWidth(), 0); //打印图片
//            esc.addPrintAndLineFeed();
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }
        /*打印一维条码code128*/
        // 设置条码可识别字符位置在条码下方
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        //设置条码高度为 60 点
        esc.addSetBarcodeHeight((byte) 60);
//        esc.addCODE128("Gprinter"); //打印 Code128 码

//        esc.addText("{BNo.{C123456");

        //CODE B,使用转义字符; CODE C, DEC
        String content1 = "13579";
        String content2 = "{B13579";
//        esc.addText("1D6B49" + content1.length() + "7B42" + "4E6F46" + "7B43" + content1);
//        esc.addText("1D6B49" + "7B43" + content1.length() + "7B42" + "4E6F46" + "7B43" + content1);
//        esc.addCODE128("1D6B49" + "7B42" + "4E6F46" + "7B43" + "123456789");
//        esc.addText(ArrayUtils.toString(DataConvertUtil.HexToByteArr("1D6B49" + Integer.toHexString(content1.length() + 3) + "7B42" + "4E6F46" + "7B43" + content1)));

//        esc.addText(ArrayUtils.toString(DataConvertUtil.HexToByteArr("1D6B49" + Integer.toHexString(content1.length()) +  "7B43" + content1)));
//        esc.addCODE128("7B42" + "4E6F46" + "7B43" + "30313233343536");
        //CODE B,使用转义字符; CODE C, DEC
//        esc.addCODE128("1D6B49" + "7B42" + "4E6F46" + "7B43" + "30313233343536");

//        EventBus.getDefault().post(new SerialPortEvent(2, bytes));
//
//        EventBus.getDefault().post(new SerialPortEvent(2, DataConvertUtil.HexToByteArr("1D6B49"
//                + DataConvertUtil.Byte2Hex(Byte.valueOf(String.valueOf(content1.length()))) +  "7B43" + DataConvertUtil.Chr2Hex(content1))));
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
//        EventBus.getDefault().post(new SerialPortEvent(2, DataConvertUtil.HexToByteArr("1D6B49" + "09" + "7B42" + "4E6F" + "7B43" + "30313233343536")));
//        EventBus.getDefault().post(new SerialPortEvent(2, DataConvertUtil.HexToByteArr("1D6B49" + "09" + "7B42" + "35363031323334")));

        String lenStr = String.format("%02X", (byte)content1.length());
//        String cmdStr = "1D6B49" + lenStr + "7B43" + DataConvertUtil.ByteArrToHex(content1.getBytes(), "");
//        String cmdStr2 = "1D6B49" + lenStr + "7B42" + DataConvertUtil.ByteArrToHex(content1.getBytes(), "");
//        String cmdStr3 = "7B42" + DataConvertUtil.ByteArrToHex(content1.getBytes(), "");
        String cmdStr4 = "1D6B49" + String.format("%02X", (byte) (content2.length())) + DataConvertUtil.ByteArrToHex(content2.getBytes(), "");
//        EventBus.getDefault().post(new SerialPortEvent(2, DataConvertUtil.HexToByteArr(cmdStr)));

//        esc.addText(cmdStr);
        esc.addUserCommand(DataConvertUtil.HexToByteArr(cmdStr4));

        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.GPRINTER_SEND_DATA, bytes));

//        EventBus.getDefault().post(new SerialPortEvent(2, DataConvertUtil.HexToByteArr("1D6B49" + Integer.toHexString(content1.length()) + "7B43" + DataConvertUtil.Chr2Hex(content1))));
//        EventBus.getDefault().post(new SerialPortEvent(3, "1D6B490A7B424E6F7B4330313233343536"));
//        EventBus.getDefault().post(new SerialPortEvent(3, cmdStr));
//        try {
//            EventBus.getDefault().post(new SerialPortEvent(2, cmdStr.getBytes("GB2312")));
//            EventBus.getDefault().post(new SerialPortEvent(2, DataConvertUtil.HexToByteArr(cmdStr)));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 打印 商品明细
     *
     *012345678901234567890123 45678901
     *         品名               金额
     * */
//    private static EscCommand makeShipTemp(EscCommand rawEsc, String name, Double count){
//        EscCommand esc = rawEsc;
//        if (esc == null){
//            esc = new EscCommand();
//        }
//
//        int maxLine = Math.max(1, (name == null ? 0 : (getLength(name) - 1) / PRINT_PRODUCT_NAME_MAX_LEN + 1));
//
////        ZLogger.d(String.format("maxLine=%d", maxLine));
//        String nameTemp = name;
//
//        int mid = maxLine / 2;
//        for (int i = 0; i < maxLine; i++){
//            StringBuilder line = new StringBuilder();
//
////            ZLogger.d(String.format("nameTemp=%s", nameTemp));
////            ZLogger.d(String.format("nameTemp: %d(%d)", getLength(nameTemp), nameTemp.toCharArray().length));
//            String sub2 = DataConvertUtil.subString(nameTemp, Math.min(PRINT_PRODUCT_NAME_MAX_LEN, getLength(nameTemp)));
////            String sub2 = DataConvertUtil.subString(nameTemp, Math.min(PRINT_PRODUCT_NAME_MAX_LEN, nameTemp.toCharArray().length));
////            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
//            if (nameTemp != null){
//                nameTemp = nameTemp.substring(sub2.length(), nameTemp.length()).trim();
//            }
////            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
////            line.append(formatLong(sub2, 8));
//            line.append(sub2).append(addBlank(Math.max(PRINT_PRODUCT_NAME_MAX_LEN - getLength(sub2), 0)));
//
//            if (i == mid){
//                line.append(formatShort(String.valueOf(count), PRINT_PRODUCT_NAME_MAX_LEN, BLANK_GRAVITY.NONE));
//            }
//
//            line.append("\n");
////            ZLogger.d(String.format("print line(%d/%d):%s" , i, mid, line.toString()));
//            esc.addText(line.toString());
//        }
//
//        return esc;
//    }





//    /**
//     * 打印 商品明细
//     *
//     *01234567 89012345 6789 012345 678901
//     *  商品ID    品名   数量   单价   金额
//     * */
//    private static EscCommand makeTemp2(EscCommand rawEsc, String id,
//                                        String name, String quantity, String price, String amount){
//        EscCommand esc = rawEsc;
//        if (esc == null){
//            esc = new EscCommand();
//        }
//
////        int maxLine = Math.max(1, (name == null ? 0 : (getLength(name) - 1) / 4 + 1));
////        int nameLen = name.length();
//        char[] nameChars = name.trim().toCharArray();
//        int len = nameChars.length;
//
//        getLength(name);
////        ZLogger.d(String.format("text=%s, len=%d", name, name.length()));
////        int maxLine = Math.max(1, len/7);
//        int maxLine = Math.max(1,((getLength(name) - 1) / 8 + 1));
//
////        ZLogger.d(String.format("maxLine=%d", maxLine));
//        String nameTemp = name;
//
//        int mid = maxLine / 2;
//        for (int i = 0; i < maxLine; i++){
//            StringBuilder line = new StringBuilder();
//            if (i == mid){
//                line.append(formatShort(id, 8, BLANK_GRAVITY.LEFT));
//            }else{
//                line.append(addBlank(8));
//            }
////            String subId = DataConvertUtil.subString(id, Math.min(8, getLength(nameTemp)));
//
////            ZLogger.d(String.format("nameTemp=%s", nameTemp));
//            String sub2 = DataConvertUtil.subString(nameTemp, Math.min(8, getLength(nameTemp)));
////            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
//            nameTemp = nameTemp.substring(sub2.length(), nameTemp.length()).trim();
////            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
////            line.append(formatLong(sub2, 8));
//            line.append(sub2).append(addBlank(Math.max(8 - getLength(sub2), 0)));
////
////            try {
////                byte[] nameBs = name.getBytes("GB2312");
////                if (i == maxLine -1){
////                    byte[] subBs = Arrays.copyOfRange(nameBs, 8 * i, nameBs.length - 1);
//////                    String sub = new String(Base64.encode(nameBs, 8 * i, nameBs.length - 8*i, 0), "GB2312");
////                    String sub = new String(subBs, "GB2312");
////                    ZLogger.d(String.format("name=%s subName=%s subName2=%s nameTemp=%s", name, sub, sub2, nameTemp));
////                    line.append(formatShort(sub, 8, true));
////                }else{
////                    byte[] subBs = Arrays.copyOfRange(nameBs, 8 * i, Math.min(8 * i+8, nameBs.length-1));
//////                    String sub = new String(Base64.encode(nameBs, 8 * i, 8, 0), "GB2312");
////                    String sub = new String(subBs, "GB2312");
////                    ZLogger.d(String.format("name=%s subName=%s subName2=%s nameTemp=%s", name, sub, sub2, nameTemp));
////                    line.append(formatShort(sub, 8, true));
////                }
////            } catch (UnsupportedEncodingException e) {
////                e.printStackTrace();
////            }
//
////            String subName = name.substring(8*i, Math.min(8*i + 8, name.length()-1));
//////            String subName = String.valueOf(nameChars, 7 * i, 7);
////            ZLogger.d(String.format("name=%s subName=%s", name, subName));
////            line.append(formatShort(subName, 8, true));
//
//            if (i == mid){
//                line.append(formatShort(quantity, 4, BLANK_GRAVITY.LEFT));
//                line.append(formatShort(price, 6, BLANK_GRAVITY.LEFT));
//                line.append(formatShort(amount, 6, BLANK_GRAVITY.NONE));
//            }
//
//            line.append("\n");
////            ZLogger.d(String.format("print line(%d/%d):%s" , i, mid, line.toString()));
//            esc.addText(line.toString());
//        }
//        esc.addText("--------------------------------\n");//32个
//
////        Vector<Byte> datas = esc.getCommand();
////        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
////        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//
//        return esc;
//    }




//    Rockchip




}
