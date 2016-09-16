package com.bingshanguxue.pda.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bingshanguxue.pda.R;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.StringUtils;


/**
 * 自定义扫描条码控件
 */
public class ScanBar extends LinearLayout {

    private EditText etInput;
    private ImageView ivDel;
    private ImageView ivAction1;

    private boolean softKeyboardEnabled = false;//是否支持软键盘,默认不支持软键盘
    private int[] interceptKeyCodes;

    public interface OnScanBarListener {
        void onKeycodeEnterClick(String text);
        void onAction1Click(String text);
    }

    private OnScanBarListener onViewListener;

    public void setOnScanBarListener(OnScanBarListener onViewListener) {
        this.onViewListener = onViewListener;
    }

    public ScanBar(Context context) {
        this(context, null);
    }

    public ScanBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.scan_bar, this);
        etInput = (EditText) rootView.findViewById(R.id.et_input);
        ivDel = (ImageView) rootView.findViewById(R.id.iv_del);
        ivAction1 = (ImageView) rootView.findViewById(R.id.iv_action_1);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ScanBar);

        float textSizeInPx = ta.getDimensionPixelSize(R.styleable.ScanBar_textSize, 12);
        int textSizeInSp = DensityUtil.px2sp(getContext(), textSizeInPx);
        etInput.setTextSize(textSizeInSp);

        etInput.setTextColor(ta.getColor(R.styleable.ScanBar_textColor, 0));
        etInput.setHint(ta.getString(R.styleable.ScanBar_hint));
        etInput.setHintTextColor(ta.getColor(R.styleable.ScanBar_textColorHint, 0));
        int inputType = ta.getInteger(R.styleable.ScanBar_editInputType, 0);
        if (inputType == EditInputType.BARCODE){
//            etInput.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        }
        else{
            etInput.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        softKeyboardEnabled = ta.getBoolean(R.styleable.ScanBar_softKeyboardEnabled, false);

        boolean isAction1Enabled = ta.getBoolean(R.styleable.ScanBar_action1Enabled, false);
        if (isAction1Enabled){
            ivAction1.setVisibility(VISIBLE);
        }
        else{
            ivAction1.setVisibility(GONE);
        }
        ivAction1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etInput.getText().toString();

                if (onViewListener != null){
                    onViewListener.onAction1Click(text);
                }
            }
        });

        ta.recycle();

//        etInput.setFocusableInTouchMode(false);
        etInput.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!softKeyboardEnabled) {
                    DeviceUtils.hideSoftInput(getContext(), v);
                }
            }
        });
        //
        etInput.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (softKeyboardEnabled) {
                        DeviceUtils.showSoftInput(getContext(), etInput);
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), etInput);
                    }
                }
                requestFocusEnd();
                //返回true,不再继续传递事件
                return true;
            }
        });
        etInput.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d(String.format("(%s):%d,%d", etInput.getClass().getSimpleName(),
                        event.getKeyCode(), event.getAction()));
                //Press “Enter”
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        String text = etInput.getText().toString();
                        if (!StringUtils.isEmpty(text) && onViewListener != null) {
                            onViewListener.onKeycodeEnterClick(text);
                        }
                    }

                    return true;
                }
//                //Press “F5”，盘点机，F5对"Scan"扫描按钮
//                if (event.getKeyCode() == KeyEvent.KEYCODE_F5) {
//                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        if (onViewListener != null){
//                            onViewListener.onScan();
//                        }
//                    }
//
//                    return true;
//                }

                return false;
            }
        });

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    ivDel.setVisibility(VISIBLE);
                } else {
                    ivDel.setVisibility(INVISIBLE);
                }
            }
        });
        ivDel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }


    /**
     * 光标定位到最后
     */
    public void requestFocusEnd() {
        etInput.requestFocus();
        etInput.setSelection(etInput.length());
    }

    public void reset() {
        etInput.getText().clear();
        etInput.requestFocus();
        etInput.setSelection(etInput.length());
    }

    public String getInputText() {
        return etInput.getText().toString();
    }

    public void setInputText(String text){
        etInput.setText(text);
    }
    public boolean isSoftKeyboardEnabled() {
        return softKeyboardEnabled;
    }

    public void setSoftKeyboardEnabled(boolean softKeyboardEnabled) {
        this.softKeyboardEnabled = softKeyboardEnabled;
    }

    public void addTextChangedListener(TextWatcher watcher) {
        etInput.addTextChangedListener(watcher);
    }

}
