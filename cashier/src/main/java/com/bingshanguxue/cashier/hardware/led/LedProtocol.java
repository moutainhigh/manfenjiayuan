package com.bingshanguxue.cashier.hardware.led;

/**
 * 客显指令集合
 * Created by bingshanguxue on 15/9/9.
 */
public class LedProtocol {
    public static final String HEX_CR = "0D";
    public static final String HEX_0 = "30";
    public static final String HEX_1 = "31";
    public static final String HEX_2 = "32";
    public static final String HEX_3 = "33";
    public static final String HEX_4 = "34";
    public static final String HEX_5 = "35";
    public static final String HEX_6 = "36";
    public static final String HEX_7 = "37";
    public static final String HEX_8 = "38";
    public static final String HEX_9 = "39";
    public static final String HEX_MINUS = "2D";
    public static final String HEX_DOT = "2E";

//    STX  L  d1  d2  d3  d4控制显示状态灯
//    ACSII码 格式：STX  L  d1  d2  d3  d4      d=0、1
//    十进制   格式：[002][076]d1 d2 d3 d4           d=048、049
//    十六进制 格式：[02H][4CH]d1 d2 d3 d4          d=30H、31H
//    说明：控制状态灯相应位的亮灭。
//    当d1=0时，单价字符 灭；d1=1时，单价字符 亮
//    当d2=0时，总计字符 灭；d2=1时，总计字符 亮
//    当d3=0时，收款字符 灭；d3=1时，收款字符 亮
//    当d4=0时，找零字符 灭；d4=1时，找零字符 亮
    public static final String CMD_HEX_STX_L = "024C";

//    ESC  s  n设置 “单价”、“总计”、“收款”、“找零”字符显示状态命令
//    十六进制 格式：[1BH][73H] n                30H<=n<=34H
//    说明：
//    当 n=0，四种字符 全暗。
//    当 n=1，“单价”字符 亮，其它三种 全暗。
//    当 n=2，“总计”字符 亮，其它三种 全暗。
//    当 n=3，“收款”字符 亮，其它三种 全暗。
//    当 n=4，“找零”字符 亮，其它三种 全暗。
    public static final String CMD_HEX_ESC_S = "1B73";
    public static final String CMD_HEX_ESC_S_0 = CMD_HEX_ESC_S + HEX_0;
    public static final String CMD_HEX_ESC_S_1 = CMD_HEX_ESC_S + HEX_1;
    public static final String CMD_HEX_ESC_S_2 = CMD_HEX_ESC_S + HEX_2;
    public static final String CMD_HEX_ESC_S_3 = CMD_HEX_ESC_S + HEX_3;
    public static final String CMD_HEX_ESC_S_4 = CMD_HEX_ESC_S + HEX_4;

//    STX  M开钱箱命令
//    十六进制 格式：[02H][4DH]
//    说明：通过顾客显示屏开启钱箱。
    public static final String CMD_HEX_STX_M = "024D";

//    CLR清屏命令
//    十六进制 格式：[0CH]
//    说明：清除屏幕上的字符。
    public static final String CMD_HEX_CLR = "0C";

//    ESC  Q  A  d1d2d3…dn  CR送显示数据命令
//    ASCII码 格式：ESC  Q  A  d1d2d3…dn  CR
//    十进制   格式：[027][081][065]d1d2d3…dn[013]     48<=dn<=57或dn=45或dn=46
//    十六进制 格式：[1BH][51H][41H]d1d2d3…dn[0DH]  30H<=dn<=39H或dn=2DH或dn=2EH
//    说明：
//    执行该命令时，会以覆盖模式送要显示的数据，这样就不需要在每次送显示数据前都去执行CAN清除光标行命令了。
//    显示的d1…dn没有小数点时1<=n<=8。
//    显示的d1…dn有小数点时1<=n<=15（8位数值+7位小数点）。
//    显示的内容可用CLR或CAN命令清除。
    public static final String CMD_HEX_ESC_Q_A = "1B5141";

//    US  _  n  m设置“多谢惠顾”字符、“动态线”显示状态命令
//    ASCII码 格式：US  _  n  m                0<=n<=3      0<=m<=1
//    十进制   格式：[031][095] n m               48<=n<=51     48<=m<=49
//    十六进制 格式：[1FH][5FH] n m             30H<=n<=33H   30H<=m<=31H
//    说明：通过这个命令可以是顾客显示屏的显示更加生动活泼。
//            (1) 当 n=0, “多谢惠顾”字符 暗。
//            (2) 当 n=1, “多谢惠顾”字符 亮。
//            (3) 当 n=2, “多谢惠顾”字符 转动。
//            (4) 当 n=3,	 “多谢惠顾”字符 闪烁。
//            (5) 当 m=0, “动态线” 全暗。
//            (6) 当 m=1, “动态线” 全亮。
    public static final String CMD_HEX_US = "1F5F";


}
