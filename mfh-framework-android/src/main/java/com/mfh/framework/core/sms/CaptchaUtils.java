package com.mfh.framework.core.sms;

import com.mfh.framework.anlaysis.logger.ZLogger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bingshanguxue on 07/11/2016.
 */

public class CaptchaUtils {
    public static String[] CPATCHAS_KEYWORD = new String[]{"身份验证"};

    /**
     * 是否是验证码信息
     * 只要信息中有匹配验证啊吗的keyword,即认为是验证码
     * */
    public static boolean isCaptchasMessage(String content) {
        Boolean isCaptcha = false;
        for (int i = 0; i < CPATCHAS_KEYWORD.length; i++) {
            if (content.contains(CPATCHAS_KEYWORD[i])) {
                isCaptcha = true;
                break;
            }
        }
        ZLogger.d("isCaptcha=" + isCaptcha);

        return isCaptcha;
    }

    /**
     * 获取验证码
     * */
    public static String tryToGetCaptchas(String str) {
        Pattern continuousNumberPattern = Pattern.compile("[a-zA-Z0-9\\.]+");
        Matcher m = continuousNumberPattern.matcher(str);
        String mostLikelyCaptchas = "";
        int currentLevel = -1; //只有字母相似级别为0， 只有字母和数字可能级别为1, 只有数字可能级别为2.
        while (m.find()) {
            if (m.group().length() > 3 && m.group().length() < 8 && !m.group().contains(".")) {
                if(isNearToKeyWord(m.group(), str)) {
                    final String strr = m.group();
                    if(currentLevel == -1) {
                        mostLikelyCaptchas = m.group();
                    }
                    final int level = getLikelyLevel(m.group());
                    ZLogger.d("level=" + level);
                    if(level > currentLevel) {
                        mostLikelyCaptchas = m.group();
                    }
                    currentLevel = level;
                }
            }
        }
        ZLogger.d("mostLikelyCaptchas=" + mostLikelyCaptchas);

        return mostLikelyCaptchas;
    }
    public static boolean isNearToKeyWord(String currentStr, String content) {
        int startPosition = 0;
        int endPosition = content.length() - 1;
        if (content.indexOf(currentStr) > 12) {
            startPosition = content.indexOf(currentStr) - 12;
        }
        if (content.indexOf(currentStr)  + currentStr.length() + 12 < content.length() - 1) {
            endPosition = content.indexOf(currentStr) + currentStr.length() + 12;
        }
        Boolean isNearToKeyWord = false;
        for (int i = 0; i < CPATCHAS_KEYWORD.length; i++) {
            if (content.substring(startPosition, endPosition).contains(CPATCHAS_KEYWORD[i])) {
                isNearToKeyWord = true;
                break;
            }
        }
        return isNearToKeyWord;
    }

    private static  int getLikelyLevel(String str) {
        if(str.matches("^[0-9]*$")) {
            return 2;
        } else if(str.matches("^[a-zA-Z]*$")) {
            return 0;
        } else {
            return 1;
        }
    }


}
