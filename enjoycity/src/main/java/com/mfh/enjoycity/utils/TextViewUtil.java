package com.mfh.enjoycity.utils;

import android.widget.TextView;

/**
 * Created by kun on 15/8/13.
 */
public class TextViewUtil {

    public static void showPrice(TextView textView, double price, int positiveColor, int negativeColor){
        textView.setText(String.format("￥ %.2f", price));
        textView.setTextColor((price > 0 ? positiveColor : negativeColor));
    }
}
