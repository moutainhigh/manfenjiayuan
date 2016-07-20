package com.mfh.framework.uikit.utils;

import android.text.InputFilter;
import android.text.Spanned;

import com.mfh.framework.core.utils.StringUtils;

/**
 * IP Address: format like 192.168.135.244
 * Created by bingshanguxue on 16/3/15.
 */
public class IpInputFilter implements InputFilter {

    private static final String REGULAR_EXPRESSION = "^\\d{1,3}(\\." +
            "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?";

    /**
     * source    新输入的字符串
     * start    新输入的字符串起始下标，一般为0
     * end    新输入的字符串终点下标，一般为source长度-1
     * dest    输入之前文本框内容
     * dstart    原内容起始坐标，一般为0
     * dend    原内容终点坐标，一般为dest长度-1
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        // 删除等特殊字符，直接返回
        if (StringUtils.isEmpty(source)) {
//            ZLogger.d("filter: empty");
            return null;
        }

        if (end > start) {
            String destTxt = dest.toString();
            String resultingTxt = destTxt.substring(0, dstart) +
                    source.subSequence(start, end) +
                    destTxt.substring(dend);
            if (!isIpValidate(resultingTxt)) {
                return "";
            } else {
                String[] splits = resultingTxt.split("\\.");
                for (int i = 0; i < splits.length; i++) {
                    if (Integer.valueOf(splits[i]) > 255) {
                        return "";
                    }
                }
            }
        }
        return null;

    }

    public static boolean isIpValidate(String src) {
        return src.matches(REGULAR_EXPRESSION);
    }
}
