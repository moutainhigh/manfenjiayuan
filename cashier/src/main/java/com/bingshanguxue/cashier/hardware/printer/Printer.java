package com.bingshanguxue.cashier.hardware.printer;

import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.bingshanguxue.cashier.hardware.printer.gprinter.EscCommand;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.StringUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

/**
 * 打印机
 * Created by bingshanguxue on 23/12/2016.
 */

public class Printer implements IPrinter {
    public static final java.text.SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm",
            Locale.US);

    /**默认线条，虚线，共32个字符*/
    public static final String LINE_DEFAULT = "--------------------------------";

    /**
     * 空格对齐方式
     **/
    public enum BLANK_GRAVITY {
        NONE(0),
        LEFT(1),
        CENTER(2),
        RIGHT(3);

        private final int value;

        BLANK_GRAVITY(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) this.value;
        }
    }

    /**
     * 返回字节数
     */
    public static int getLength(String text) {
        if (StringUtils.isEmpty(text)) {
            return 0;
        }

        int len = 0;
        if (!text.equals("")) {
            try {
                byte[] bs = text.getBytes("GB2312");
                len = bs.length;
            } catch (UnsupportedEncodingException var4) {
                var4.printStackTrace();
                ZLogger.e(var4.toString());
            }
        }
//        ZLogger.d(String.format("text=%s, len=%d", text, len));

        return len;
    }

    /**
     * */
    public static String formatShort(String raw, int maxWidth, BLANK_GRAVITY blankGravity) {
//        Pattern p = Pattern.compile("[0-9]*");
//        p=Pattern.compile("[\u4e00-\u9fa5]");

//        String formated = raw.trim();
//        char[] rawChars = raw.trim().toCharArray();
//        int len = rawChars.length;
        int len = getLength(raw);
        String subStr = DataConvertUtil.subString(raw, Math.min(len, maxWidth));//截取字符串 String.valueOf(rawChars, 0, len2)
        String blankStr = StringUtils.genBlankspace(Math.max(maxWidth - len, 0));
//        ZLogger.d(String.format("subString([%s%s]", subStr, blankStr));
        if (len > maxWidth) {
            return subStr;
        } else {
            //右对齐，在前面补空格
            if (blankGravity == BLANK_GRAVITY.LEFT) {
                return blankStr + subStr;
            }
            //左对齐，在后面补空格
            else if (blankGravity == BLANK_GRAVITY.RIGHT) {
                return subStr + blankStr;
            } else {
                return subStr;
            }
        }
    }

    /**
     * 添加横线
     */
    public void addLine(EscCommand esc) {
        if (esc == null) {
            return;
        }
        esc.addText("--------------------------------\n");//32个
    }


    /**
     * 初始化打印机
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
     *         <td>清除打印缓冲区数据，打印模式被设为上电时的默认值模式。</td>
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
    public static byte[] initPrinter() {
        return new byte[]{(byte) 27, (byte) 64};
    }

    /**
     * 打印
     * @param escCommand 打印命令
     * @param printTimes 打印次数
     * */
    public static  void print(EscCommand escCommand, int printTimes) {
        if (escCommand != null && printTimes > 0) {
            for (int i = 0 ; i < printTimes; i++){
                print(escCommand);
            }
        }
    }

    public static void print(EscCommand escCommand) {
        if (escCommand != null) {
            ZLogger.d(">>发送打印命令");

            Vector<Byte> datas = escCommand.getCommand();//发送数据
            Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
            byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.RINTER_PRINT_PRIMITIVE, bytes));
        }
    }

    public static void print(String text) {
        byte[] bs = null;
        if(!text.equals("")) {
            try {
                bs = text.getBytes("GB2312");
            } catch (UnsupportedEncodingException var4) {
                var4.printStackTrace();
            }
        }
        if (bs != null){
            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.RINTER_PRINT_PRIMITIVE, bs));
        }
    }

    @Override
    public void feedPaper() {

    }

    @Override
    public void openMoneyBox() {

    }

    @Override
    public void printAndLineFeed(EscCommand escCommand, int lines) {

    }

    @Override
    public void addCODE128(EscCommand esc, String barcode) {

    }
}
