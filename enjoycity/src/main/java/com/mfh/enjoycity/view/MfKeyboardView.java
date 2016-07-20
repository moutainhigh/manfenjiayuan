package com.mfh.enjoycity.view;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

/**
 * Created by kun on 15/8/11.
 */
public class MfKeyboardView extends KeyboardView {
    public static final int KEYCODE_OPTIONS = -100;

    public MfKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MfKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    public MfKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    /**
     * 覆写这个方法，当用户长按CANCEL键的时候 抛出事件，可以用来现实现实输入法选项的操作
     */
    @Override
    protected boolean onLongPress(Keyboard.Key key) {
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        } else {
            return super.onLongPress(key);
        }
    }
}
