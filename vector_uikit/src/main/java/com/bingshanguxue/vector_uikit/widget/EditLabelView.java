package com.bingshanguxue.vector_uikit.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DeviceUtils;


/**
 * 复合控件－－（文本＋输入框）
 * 支持自定义属性，可以直接在xml文件中配置。
 */
public class EditLabelView extends LinearLayout {

    private TextView tvStartText;
    private EditText etInput;
    private TextView tvEndText;

    private boolean softKeyboardEnabled = false;//是否支持软键盘,默认不支持软键盘
    private int[] interceptKeyCodes;

    public interface OnViewListener {
        void onKeycodeEnterClick(String text);

        void onScan();
    }

    private OnViewListener onViewListener;

    public void setOnViewListener(OnViewListener onViewListener) {
        this.onViewListener = onViewListener;
    }

    public EditLabelView(Context context) {
        this(context, null);
    }

    public EditLabelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.widget_editlabelview, this);
        tvStartText = (TextView) rootView.findViewById(R.id.tv_lefttext);
        etInput = (EditText) rootView.findViewById(R.id.et_content);
        tvEndText = (TextView) rootView.findViewById(R.id.tv_endText);

        try {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EditLabelView);
            tvStartText.setText(ta.getString(R.styleable.EditLabelView_startText));
            //像素
            int textSizeInPx = ta.getDimensionPixelSize(R.styleable.EditLabelView_startTextSize, 16);
            int textSizeInSp = DensityUtil.px2sp(getContext(), textSizeInPx);
            int editTextSizeInPx = ta.getDimensionPixelSize(R.styleable.EditLabelView_editTextSize, 16);
            int editTextSizeInSp = DensityUtil.px2sp(getContext(), editTextSizeInPx);
            int endTextSizeInPx = ta.getDimensionPixelSize(R.styleable.EditLabelView_endTextSize, 16);
            int endTextSizeInSp = DensityUtil.px2sp(getContext(), endTextSizeInPx);
            //px
            int startTextWidth = ta.getDimensionPixelSize(R.styleable.EditLabelView_startTextWidth, 80);//px

//            ZLogger.d(String.format("textSize=%dpx =%dsp\nstartTextWidth=%d",
//                    textSizeInPx, textSizeInSp,
//                    startTextWidth));
            tvStartText.setTextSize(textSizeInSp);
//        tvStartText.setTextSize(DensityUtil.sp2px(context, leftTextSize));
            tvStartText.setTextColor(ta.getColor(R.styleable.EditLabelView_startTextColor, 0x59000000));

            ViewGroup.LayoutParams stLayoutParams = tvStartText.getLayoutParams();
            stLayoutParams.width = startTextWidth;
            tvStartText.setLayoutParams(stLayoutParams);

            etInput.setHint(ta.getString(R.styleable.EditLabelView_editTextHint));
            etInput.setTextSize(editTextSizeInSp);
            etInput.setTextColor(ta.getColor(R.styleable.EditLabelView_editTextColor, 0xFF000000));
            etInput.setHintTextColor(ta.getColor(R.styleable.EditLabelView_editTextColorHint, 0x26000000));
            int inputType = ta.getInteger(R.styleable.EditLabelView_editInputType, EditInputType.TEXT);
            if (inputType == EditInputType.BARCODE) {
                etInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
            }
            else if (inputType == EditInputType.NUMBER_DECIMAL) {
                //相当于在.xml文件中设置inputType="numberDecimal
                etInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
            else {
                etInput.setInputType(InputType.TYPE_CLASS_TEXT);
            }
//            ZLogger.d(String.format("inputType=%d", etInput.getInputType()));

            softKeyboardEnabled = ta.getBoolean(R.styleable.EditLabelView_softKeyboardEnabled, false);
            if (ta.getBoolean(R.styleable.EditLabelView_endTextVisible, false)) {
                tvEndText.setVisibility(VISIBLE);
            } else {
                tvEndText.setVisibility(GONE);
            }
            tvEndText.setText(ta.getString(R.styleable.EditLabelView_endText));
            tvEndText.setTextSize(endTextSizeInSp);
            tvEndText.setTextColor(ta.getColor(R.styleable.EditLabelView_endTextColor, 0xFF000000));

            ta.recycle();
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }

//        etInput.setInputType(InputType.TYPE_NULL);

//        etInput.setFocusableInTouchMode(false);
//        etInput.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!softKeyboardEnabled) {
//                    DeviceUtils.hideSoftInput(getContext(), v);
//                }
//            }
//        });
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
        etInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d(String.format("(%s):%d", etInput.getClass().getSimpleName(),
                        event.getKeyCode()));
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        DeviceUtils.hideSoftInput(getContext(), v);

                        if (onViewListener != null) {
                            onViewListener.onKeycodeEnterClick(etInput.getText().toString());
                        }
                    }

                    return true;
                }
                //Press “F5”，盘点机，F5对"Scan"扫描按钮
                if (keyCode == KeyEvent.KEYCODE_F5) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (onViewListener != null) {
                            onViewListener.onScan();
                        }
                    }

                    return true;
                }

                return false;
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

    public void setInputAndEnd(String inputText, String endText) {
        this.etInput.setText(inputText);
        this.tvEndText.setText(endText);
    }

    public void setInputEnabled(boolean enabled){
        this.etInput.setEnabled(enabled);
    }

    public void setStartText(String text) {
        tvStartText.setText(text);
    }

    public void setInput(String text) {
        this.etInput.setText(text);
    }

    public String getInput() {
        return etInput.getText().toString();
    }

    public void setEndText(String text) {
        this.tvEndText.setText(text);
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
