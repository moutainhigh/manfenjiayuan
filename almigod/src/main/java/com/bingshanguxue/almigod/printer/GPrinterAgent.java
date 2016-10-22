package com.bingshanguxue.almigod.printer;

import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Date;
import java.util.Vector;

/**
 * GPrinter打印机&钱箱
 * <h1>通信协议</h1>
 * <ol>
 * <li>9600波特率，8位数字位，1位停止位，没有校验位</li>
 * </ol>
 * <h1>开/关发送数据流</h1>
 * <ol>
 * <li>按“清除“+"0"选择通讯模式。</li>
 * <li>RS232-0,不发送；RS232-1,连续发送；RS232-2,稳定发送;RS232-3,P 应答模式；RS232-4, $应答模式</li>
 * 应用默认选择RS232-2,稳定发送模式，所以出场时需要正确配置！
 * <li>按“去皮”设置完成</li>
 * </ol>
 * <p>
 * Created by bingshanguxue on 5/27/16.
 */
public class GPrinterAgent {
    private static final boolean ENABLED_DEF = false;
    public static final String PORT_DEF = "";
    public static final String BAUDRATE_DEF = "9600";

    public static String PREF_NAME = "pref_gprinter";
    private static final String PK_GPRINTER_ENABLED = "pk_GPRINTER_ENABLED";
    private static final String PK_GPRINTER_PORT = "pk_GPRINTER_PORT";


    public static String getPort() {
        return SharedPreferencesManager.getText(PREF_NAME,
                PK_GPRINTER_PORT, GPrinterAgent.PORT_DEF);
    }

    public static void setPort(String port) {
        SharedPreferencesManager.set(PREF_NAME,
                PK_GPRINTER_PORT, port);
    }

    public static String getBaudrate() {
        return BAUDRATE_DEF;
    }

    public static boolean isEnabled() {
        return SharedPreferencesManager.getBoolean(PREF_NAME,
                PK_GPRINTER_ENABLED, ENABLED_DEF);
    }

    public static void setEnabled(boolean enabled) {
        SharedPreferencesManager.set(PREF_NAME,
                PK_GPRINTER_ENABLED, enabled);
    }

    /**
     * 打印
     */
    public static void print(EscCommand escCommand) {
        if (escCommand != null) {
            //获得打印命令
            Vector<Byte> datas = escCommand.getCommand();//发送数据
            Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
            byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
//            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.GPRINTER_SEND_DATA, bytes));
        }
    }

    /**
     * 走纸
     */
    public static void feedPaper() {
        EscCommand esc = new EscCommand();
//                    esc.addPrintAndLineFeed();
        //打印并且走纸多少行
        esc.addPrintAndFeedLines((byte) 5);
        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.GPRINTER_SEND_DATA, bytes));
    }

    /**
     * 打开钱箱
     */
    public static void openMoneyBox() {
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY,
//                CommandConstants.CMD_HEX_STX_M));

        EscCommand esc = new EscCommand();
        esc.addGeneratePluseAtRealtime(LabelCommand.FOOT.F2, (byte) 20);
        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.GPRINTER_SEND_DATA, bytes));
    }

    /**
     * 清除屏幕上的字符
     */
    public static void clear() {
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY, CommandConstants.CMD_HEX_CLR));
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD, ""));
        vfdShow("WELCOME");
    }

    /**
     * 显示字符
     */
    public static void vfdShow(String text) {
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD_BYTE,
//                VFD_CH(text)));
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD, text));
    }

    /**
     * 将字符串转换成VFD格式显示
     * <p>
     * 0x1b0x40 -- Max add : Initialize device(清空)
     */
    public static byte[] VFD(String displayText) {
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
     */
    public static byte[] VFD_CH(String displayText) {
        EscCommand esc = new EscCommand();

        String cmdStr4 = "1B40" + DataConvertUtil.ByteArrToHex(displayText.getBytes(), "");
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


    /**
     * 打印测试
     */
    public static EscCommand makeTestEsc() {
        try {
            EscCommand esc = new EscCommand();
            esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
            //设置打印居中
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                    EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
            esc.addText("打印测试");
            esc.addPrintAndLineFeed();//进纸一行


//        //取消倍高倍宽
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                    EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
            esc.addText("Welcome to use Gprinter!\n");   //  打印文字
            esc.addPrintAndLineFeed();//进纸一行

//        /*打印繁体中文  需要打印机支持繁体字库*/
//        String message = GPrinterAgent.SimToTra("佳博票据打印机\n");
//        //	esc.addText(message,"BIG5");
//        esc.addText(message,"GB2312");
//        esc.addPrintAndLineFeed();


            /**打印 机器设备号＋订单号*/
            esc.addText(String.format("%s NO.%s \n", SharedPreferencesManager.getTerminalId(),
                    MUtils.getOrderBarCode()));
            /**打印 订购日期*/
            esc.addText(String.format("%s \n", TimeUtil.format(new Date(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
            //5个数字等于3个汉字（1个数字＝3/5个汉字）
            esc.addText("--------------------------------\n");//32(正确)
            esc.addText("01234567890123456789012345678901\n");//32(正确)
            esc.addText("零一二三四五六七八九零一二三四五\n");//16(正确)
            esc.addText("零一二三四五六七八九零一二三四五六七八\n");//19.2(错误)
            esc.addText("货号/品名       单价 数量 小计\n");
            esc.addText("货号/品名       单价 数量   小计\n");
            esc.addText("货号/品名       00.0100.0200.03\n");//32=17+5+5+5

            esc.addPrintAndLineFeed();

/*打印一维条码code128*/
            esc.addText("Print code128\n");   //  打印文字
            // 一维条码：设置条码可识别字符位置在条码下方
            esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
            //设置条码高度为 60 点
            esc.addSetBarcodeHeight((byte) 60);
            //设置条码单元宽度为1点
            esc.addCODE128(esc.genCode128("123456"));  //打印Code128码
            esc.addCODE128(esc.genCode128(StringUtils.getNonceDecimalString(16)));  //打印Code128码
            esc.addCODE128(esc.genCodeB("Gprinter"));
//            esc.addCODE128(esc.genCodeB(StringUtils.getNonceDecimalString(16)));  //打印Code128码

            esc.addPrintAndLineFeed();


        /*QRCode 命令打印
        此命令只在支持 QRCode 命令打印的机型才能使用。
        在不支持二维码指令打印的机型上,则需要发送二维条码图片
        */
            esc.addText("Print QRcode\n");   //  打印文字
            esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31); //设置纠错等级
            esc.addSelectSizeOfModuleForQRCode((byte) 3);//设置 qrcode 模块大小
            esc.addStoreQRCodeData("www.manfenjiayuan.cn");//设置 qrcode 内容
            esc.addPrintQRCode();//打印 QRCode
            esc.addPrintAndLineFeed();

            /**打印 APP LOGO*/
//            esc.addText("Print bitmap!\n");   //  打印文字
//            Bitmap b = BitmapFactory.decodeResource(CashierApp.getAppContext().getResources(),
//                    R.mipmap.ic_launcher);
//            esc.addRastBitImage(b, b.getWidth(), 0);


//            /*打印图片*/
//            try {
//                Bitmap QRCodeBmp = QrCodeUtils.Create2DCode("www.manfenjiayuan.cn");
//                esc.addRastBitImage(QRCodeBmp, QRCodeBmp.getWidth(), 0); //打印图片
//                esc.addPrintAndLineFeed();
//            } catch (WriterException e) {
//                e.printStackTrace();
//            }

            //开钱箱
//            esc.addGeneratePlus(LabelCommand.FOOT.F5, (byte) 255, (byte) 255);
            //立即开钱箱
            //esc.addGeneratePluseAtRealtime(LabelCommand.FOOT.F2, (byte) 8);

            /**打印 订单号条形码code128图片*/
//        try {
//            Bitmap QRCodeBmp = QrCodeUtils.CreateCode128ForGPrinter(String.valueOf(orderEntity.getId()), 300, 60);
//            esc.addRastBitImage(QRCodeBmp, QRCodeBmp.getWidth(), 0); //打印图片
//            esc.addPrintAndLineFeed();
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }

            return esc;
        } catch (Exception e) {
            ZLogger.ef(e.toString());
            return null;
        }
    }


}
