package com.bingshanguxue.cashier.hardware.printer;

import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;
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
 * Created by bingshanguxue on 08/12/2016.
 */

public class Printer implements IPrinter{

    public static java.text.SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.US);

    /**
     * 打印
     * @param escCommand 打印命令
     * */
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

    public static  void print(EscCommand escCommand, int printTimes) {
        if (escCommand != null && printTimes > 0) {
            for (int i = 0 ; i < printTimes; i++){
                print(escCommand);
            }
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

    /**
     * 走纸
     * */
    @Override
    public void feedPaper() {
        EscCommand esc = new EscCommand();
//       esc.addPrintAndLineFeed();
        //打印并且走纸多少行
        esc.addPrintAndFeedLines((byte) 3);

        print(esc);
    }

    /**
     * 打开钱箱
     * */
    @Override
    public void openMoneyBox() {
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY,
                CommandConstants.CMD_HEX_STX_M));

        EscCommand esc = new EscCommand();
        esc.addGeneratePluseAtRealtime(LabelCommand.FOOT.F2, (byte)20);

        print(esc);
    }

    @Override
    public void printAndLineFeed(EscCommand escCommand, int lines) {
        if (escCommand == null){
            return;
        }
        escCommand.addPrintAndFeedLines((byte) lines);//打印并且走纸3行
    }

    /**
     * 空格对齐方式
     **/
    public static enum BLANK_GRAVITY {
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
}
