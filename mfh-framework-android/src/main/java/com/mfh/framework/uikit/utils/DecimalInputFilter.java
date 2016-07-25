package com.mfh.framework.uikit.utils;

import android.text.InputFilter;
import android.text.Spanned;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;

/**
 * Created by bingshanguxue on 16/3/15.
 */
public class DecimalInputFilter implements InputFilter {

    private int digits = 2;//小数点后有效数字位数，默认为2

    public DecimalInputFilter(int digits) {
        this.digits = digits;
    }

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
            ZLogger.d("filter: empty");
            return null;
        }

        String dValue = dest.toString();
        ZLogger.d(String.format("filter: [%s][%s] [%d-%d]", dValue, source, start, end));

        if (digits > 0){
            //主要针对输入类型是text用这种判断方法
            if (StringUtils.contains(dValue, ".") && StringUtils.contains(source.toString(), ".")) {
                ZLogger.d("最多只能输入一个小数点");
                return "";
            }
        }
        else{
            if (StringUtils.contains(source.toString(), ".")) {
                ZLogger.d("不允许输入小数点");
                return "";
            }
        }

        String[] splitArray = dValue.split("\\.");
        int dotNum = splitArray.length - 1;
        ZLogger.d(String.format("%s已经输入 %d 个小数点", dValue, dotNum));
        //最多只能接受一个小数点,有一位小数点时才去限制，否则不限制
        if (dotNum == 1) {
            //有效数字
            String dotValue = splitArray[1];
            ZLogger.d(String.format("%s 小数后有效数字位数:%d", dValue, dotValue.length()));

            //计算超出有效数字位数的长度
            int diff = dotValue.length() + source.length() - digits;
            if (diff > 0) {
                ZLogger.d("filter:" + source.subSequence(start, end - diff));
                return source.subSequence(start, end - diff);
            }
        }
        return null;
    }
}
