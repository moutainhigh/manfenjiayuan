package com.mfh.framework.core.utils;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private final static Pattern URL = Pattern.compile("^(https|http)://.*?$(net|com|.com.cn|org|me|)");

    public static String toStr(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public static String toStr(Object obj, String defautl) {
        if (isEmpty(obj)) {
            return defautl;
        }
        return obj.toString();
    }
    
    public static String toStrE(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    public static boolean isEmpty(Object obj) {
        boolean ret = false;
        if (obj == null || "".equals(obj.toString())) {
            ret = true;
        }
        return ret;
    }

    /**
     * 判断给定字符串是否空白串。
     * 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || input.length() == 0 || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static String removeDiskStr(String path) {
        String ret = toStrE(path);
        int start = ret.indexOf(":");
        if (start != -1) {
            ret = ret.substring(start + 1);
        }
        return ret;
    }

    public static boolean toBoolean(String arg0) {
        boolean ret = true;
        if ("0".equals(arg0)) {
            ret = false;
        }

        return ret;
    }

    public static boolean isLetter(char[] chars) {
        for (char c : chars) {
            if (c != 0) {
                if (!('a' <= c && c <= 'z') && !('A' <= c && c <= 'Z')) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isDigit(String val) {
        return isDigit(val.toCharArray());
    }

    public static boolean isDigit(char[] chars) {
        for (char c : chars) {
            if (c != 0) {
                if (!('0' <= c && c <= '9')) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean startsWithNum(char[] chars) {
        char c = chars[0];
        return '0' <= c && c <= '9';
    }

    /**
     * 转义XML数据字符
     * 
     * @param  source 待转义的字符
     * @return 转义后的字符
     */
    public static String convertXmlString(String source) {
        String ret = toStrE(source);
        ret = ret.replace("&", "&amp;");
        ret = ret.replace("'", "&apos;");
        ret = ret.replace("\"", "&quot;");
        ret = ret.replace(">", "&gt;");
        ret = ret.replace("<", "&lt;");
        
        return ret;
    }
    
    /**
     * 比较两个是否相等
     * @param the
     * @param other
     * @return
     * @author zhangyz created on 2013-6-7
     */
    public static boolean equals(String the, String other) {
        if (the == null && other == null)
            return true;
        else if (the == null || other == null)
            return false;
        return the.equals(other);
    }
    
    public static int ComareStr(String the, String other) {
        if (the == null && other == null)
            return 0;
        else if (the == null)
            return -1;
        else if (other == null)
            return 1;
        char[] thisChars = the.toCharArray();
        char[] oChars = other.toCharArray();
        int thisCount = thisChars.length;
        for(int i = 0; i < thisCount; i++) {
            if (thisChars[i] > oChars[i]) {
                return 1;
            } else if (thisChars[i] < oChars[i]) {
                return -1;
            }
        }
        return 0;
    }

    /**
     * 将指定字符串按指定长度输出，左对齐，不足自动补齐。
     * */
    public static String alignLeft(String raw, char padChar, int length) {
        String ret = (raw == null ? "" : raw);
        int len = length - ret.length();

        if (len > 0) {
            for (int i = 0; i < len; i++) {
                ret += padChar;
            }
        }
        return ret;
    }

    /**
     * 将指定字符串按指定长度输出，左对齐，不足自动补齐。
     * */
    public static String alignRight(String raw, char padChar, int length) {
        String ret = (raw == null ? "" : raw);
        int len = length - ret.length();

        StringBuilder sb = new StringBuilder();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                sb.append(padChar);
            }
        }
        sb.append(ret);

        return sb.toString();
    }

    //比较两个字符串，如果有包含的，就返回那个比较长的字符串
    public static String ReturnLongerWhereContain(String str1, String str2) {
        String longer = "";
        String shorter = "";
        if (str1.length() > str2.length()) {
            longer = str1;
            shorter = str2;
        }
        else {
            shorter = str1;
            longer = str2;
        }
        if (longer.contains(shorter))
            return longer;
        else
            return null;
    }

    /**
     * 判断是否为一个合法的url地址
     *
     * @param str
     * @return
     */
    public static boolean isUrl(String str) {
        if (str == null || str.trim().length() == 0)
            return false;
        return URL.matcher(str).matches();
    }

    /**
     * 获取随机字符串
     * @param length 字符串长度
     * @return 一定长度的字符串
     * */
    public static String genNonceStringByLength(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        int baseN = base.length();

        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(baseN);
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String genNonceStringByLength(String tag, int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        int baseN = base.length();

        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        if (!isEmpty(tag)) {
            sb.append(tag).append(":");
        }
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(baseN);
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String getNonceDecimalString(int length) {
        String base = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 判断字符串中时否包含中文
     *
     * @param str
     * @return
     */
    public static boolean isContainsChinese(String str) {
        String regEx = "[\u4e00-\u9fa5]";
        Pattern pat = Pattern.compile(regEx);
        Matcher matcher = pat.matcher(str);
        boolean flg = false;
        if (matcher.find()) {
            flg = true;
        }
        return flg || str.contains("【") || str.contains("】") || str.contains("。");
    }

    /**
     * 随机生成中文
     * @param length 中文字符长度
     * */
    public static String genNonceChinease(int length) {
        Random random = new Random();
        byte[] captcha = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            int hightPos = (176 + Math.abs(random.nextInt(39)));//获取高位值
            int lowPos = (161 + Math.abs(random.nextInt(93)));//获取低位值

            captcha[i*2] = (Integer.valueOf(hightPos).byteValue());
            captcha[i*2 + 1] = (Integer.valueOf(hightPos).byteValue());
        }
        try {
            return new String(captcha, "GBK");//转成中文;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 添加空格
     */
    public static String genBlankspace(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * 添加空格
     */
    public static String contact(int len, char c) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    public static boolean contains(String raw, String seed){
        return raw != null && seed != null && raw.contains(seed);
    }


    public static String parseIpAddress(int ip) {
        return (ip & 0xFF ) + "." +
                ((ip >> 8 ) & 0xFF) + "." +
                ((ip >> 16 ) & 0xFF) + "." +
                ( ip >> 24 & 0xFF) ;
    }

    /**
     * */
    public static String decodeBundle(Bundle bundle){
        if (bundle == null || bundle.size() <= 0){
            return "null or empty";
        }
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            Object object = bundle.get(key);
            sb.append(String.format("\n<%s>:<%s>",
                    key, object != null ? object.toString() : "null"));
        }
        return sb.toString();
    }

    /**
     * */
    public static Spanned toSpanned(String source){
        Spanned result;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(source,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(source);
        }

        return result;
    }

}
