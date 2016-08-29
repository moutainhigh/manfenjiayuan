package com.mfh.framework.core.utils;

import com.mfh.framework.anlaysis.logger.ZLogger;

/**
 * Created by kun on 15/9/7.
 * 数据转换工具
 */
public class DataConvertUtil {
    //-------------------------------------------------------
    // 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
    static public int isOdd(int num) {
        return num & 0x1;
    }

    //-------------------------------------------------------

    /**
     * Hex字符串转int
     */
    static public int HexToInt(String inHex) {
        return Integer.parseInt(inHex, 16);
    }

    //-------------------------------------------------------
    static public byte HexToByte(String inHex)//Hex字符串转byte
    {
        return (byte) Integer.parseInt(inHex, 16);
    }

    //-------------------------------------------------------

    /**
     * 1字节转2个Hex字符
     */
    static public String Byte2Hex(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }

    //-------------------------------------------------------

    /**
     * 字节数组转换成十六进制字符串
     *
     * @param String str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    static public String ByteArrToHex(byte[] inBytArr) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte anInBytArr : inBytArr) {
            strBuilder.append(Byte2Hex(anInBytArr));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    /**
     * 字节数组转换成十六进制字符串
     *
     * @param String str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    static public String ByteArrToHex(byte[] inBytArr, String seperate) {
        StringBuilder strBuilder = new StringBuilder();
        int j = inBytArr.length;
        for (byte anInBytArr : inBytArr) {
            strBuilder.append(Byte2Hex(anInBytArr));
            strBuilder.append(seperate);
        }
        return strBuilder.toString();
    }

    //-------------------------------------------------------
    static public String ByteArrToHex(byte[] inBytArr, int offset, int byteCount)//字节数组转转hex字符串，可选长度
    {
        StringBuilder strBuilder = new StringBuilder();
        int j = byteCount;
        for (int i = offset; i < j; i++) {
            strBuilder.append(Byte2Hex(inBytArr[i]));
        }
        return strBuilder.toString();
    }

    //-------------------------------------------------------
    //转hex字符串转字节数组
    static public byte[] HexToByteArr(String inHex)//hex字符串转字节数组
    {
        int hexlen = inHex.length();
        byte[] result;
        if (isOdd(hexlen) == 1) {//奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {//偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = HexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    public static byte hexStr2Bytes(String paramString) {
        int i = paramString.length() / 2;
        System.out.println(i);
        byte[] arrayOfByte = new byte[i];
        for (int j = 0; ; ++j) {
            if (j >= i)
                return arrayOfByte[0];
            int k = 1 + j * 2;
            int l = k + 1;
            arrayOfByte[j] = Byte.decode("0x" + paramString.substring(j * 2, k) + paramString.substring(k, l)).byteValue();
        }
    }

    public byte[] Hex2byte(String paramString) {
        String str = paramString.replace(" ", "");
        byte[] arrayOfByte = new byte[str.length() / 2];
        char[] arrayOfChar = str.toCharArray();
        for (int i = 0; ; ++i) {
            if (i >= str.length() / 2)
                return arrayOfByte;
            arrayOfByte[i] = (byte) (0xFF & (Character.digit(arrayOfChar[(i * 2)], 16) << 4) + Character.digit(arrayOfChar[(1 + i * 2)], 16));
        }
    }

    /**
     * 十六进制转换字符串
     *
     * @param String str Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    public static String Chr2Hex(CharSequence paramCharSequence) {
        String str = "";
        for (int i = 0; ; ++i) {
            if (i >= paramCharSequence.length())
                return str;
            str = str + Integer.toHexString(0x100 | paramCharSequence.charAt(i)).substring(1) + " ";
        }
    }

    public String Hex2Chr(String paramString) {
        String str1 = paramString.replace(" ", "");
//        ZLogger.d("----", str1);
        char[] arrayOfChar = str1.toCharArray();
        String str2 = "";
        for (int i = 0; ; ++i) {
            if (i >= -1 + str1.length() / 2)
                return str2;
            int j = (Character.digit(arrayOfChar[(i * 2)], 16) << 4) + Character.digit(arrayOfChar[(1 + i * 2)], 16);
            str2 = str2 + (char) j;
        }
    }

    /**
     * 编写一个截取字符串的函数，输入为一个字符串和字节数，输出为按字节截取的字符串。
     * 但是要保证汉字不被截半个，如"我ABC"4，应该截为"我AB"，输入"我ABC汉DEF"，6，
     * 应该输出为"我ABC"而不是"我ABC+汉的半个"
     *
     * @param str
     * @param byteLength
     * @return
     */
    public static String subString(String str, int byteLength) {
        int byteCount = 0;//字节计数
        int charCount = 0;//字符计数
        int len = str.toCharArray().length;
        for (int i = 0; i < len; i++) {
            charCount++;
            int charIndex = (int) str.charAt(i);
//            ZLogger.d("charIndex:" + charIndex);
//          int currentByteNum = strTemp.getBytes().length;
            if (charIndex > 128) {
                byteCount += 2;
            } else {
                byteCount += 1;
            }
            if (byteCount > byteLength) {
                charCount--;
                break;
            }
        }
//        ZLogger.d("num:" + charCount);
        return str.substring(0, charCount);
    }

    public static String subString2(String str, int byteLength) {
//      int byteCount = 0;//字节计数
//      int charCount = 0;//字符计数
//      for(int i=0;i<byteLength;i++)
//      {
//          charCount++;
//          String strTemp = String.valueOf(str.charAt(i));
//          int currentByteNum = strTemp.getBytes().length;
//          if(currentByteNum==2)
//          {
//              byteCount+=2;
//          }
//          else if(currentByteNum==1)
//          {
//              byteCount+=1;
//          }
//          if(byteCount>byteLength)
//          {
//              charCount--;
//              break;
//          }
//      }
//      System.out.println("num:"+charCount);
//      return str.substring(0, charCount);

        int byteCount = 0;//字节计数
        int charCount = 0;//字符计数
        for (int i = 0; i < byteLength; i++) {
            charCount++;
            int charIndex = (int) str.charAt(i);
            ZLogger.d("charIndex:" + charIndex);
//          int currentByteNum = strTemp.getBytes().length;
            if (charIndex > 128) {
                byteCount += 2;
            } else {
                byteCount += 1;
            }
            if (byteCount > byteLength) {
                charCount--;
                break;
            }
        }
        ZLogger.d("num:" + charCount);
        return str.substring(0, charCount);
    }

    public static String upper(String money) throws Exception {
        if (!money.matches("^[1-9]+[0-9]*$|^[1-9]+[0-9]*.[0-9]+$")) {
            throw new Exception("钱数格式错误！");
        }

        String[] part = money.split("\\.");
        StringBuilder integer = new StringBuilder();
        for (int i = 0; i < part[0].length(); i++) {
            char perchar = part[0].charAt(i);
            integer.append(upperNumber(perchar));
            integer.append(upperNumber(part[0].length() - i - 1));
        }

        StringBuilder decimal = new StringBuilder();
        if (part.length > 1 && !"00".equals(part[1])) {
            int length = part[1].length() >= 2 ? 2 : part[1].length();
            for (int i = 0; i < length; i++) {
                char perchar = part[1].charAt(i);
                decimal.append(upperNumber(perchar));
                if (i == 0) decimal.append('角');
                if (i == 1) decimal.append('分');
            }
        }

        String result = integer.toString() + decimal.toString();
        return dispose(result);
    }


    private static char upperNumber(char number) {
        switch (number) {
            case '0':
                return '零';
            case '1':
                return '壹';
            case '2':
                return '贰';
            case '3':
                return '叁';
            case '4':
                return '肆';
            case '5':
                return '伍';
            case '6':
                return '陆';
            case '7':
                return '柒';
            case '8':
                return '捌';
            case '9':
                return '玖';
        }
        return 0;
    }

    private static char upperNumber(int index) {
        switch (index) {
            case 0:
                return '圆';
            case 1:
                return '拾';
            case 2:
                return '佰';
            case 3:
                return '仟';
            case 4:
                return '万';
            case 5:
                return '拾';
            case 6:
                return '佰';
            case 7:
                return '仟';
            case 8:
                return '亿';
            case 9:
                return '拾';
            case 10:
                return '佰';
            case 11:
                return '仟';
        }
        return 0;
    }

    private static String dispose(String result) {
        result = result.replaceAll("零仟零佰零拾|零仟零佰|零佰零拾|零仟|零佰|零拾", "零")
                .replaceAll("零+", "零").replace("零亿", "亿");
        result = result.matches("^.*亿零万[^零]仟.*$") ? result.replace("零万", "零")
                : result.replace("零万", "万");
        result = result.replace("亿万", "亿").replace("零圆", "圆").replace("零分", "")
                .replaceAll("圆零角零分|圆零角$|圆$", "圆整");
        return result;
    }

    public static String bytesToAsciiString(byte[] array, int startIndex, int len) {
        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            sb.append((char) array[startIndex++]);
        }

        return sb.toString();
    }


}
