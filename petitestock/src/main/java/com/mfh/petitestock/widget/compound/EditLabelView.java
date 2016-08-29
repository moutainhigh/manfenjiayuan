package com.mfh.petitestock.widget.compound;


import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.petitestock.R;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * 复合控件－－（文本＋输入框）
 * 支持自定义属性，可以直接在xml文件中配置。
 */
public class EditLabelView extends LinearLayout {
    private static final String TAG = "EditLabelView";

    public static final int INPUT_TYPE_NUMBER = 0;
    public static final int INPUT_TYPE_NUMBER_DECIMAL = 1;

    @Bind(R.id.tv_lefttext)
    TextView tvLeftText;
    @Bind(R.id.et_content)
    EditText etContent;

    private boolean softKeyboardEnabled;//是否支持软键盘
    private int[] interceptKeyCodes;

    public interface OnViewListener{
        void onInput(String text);
        void onScan();
    }
    private OnViewListener onViewListener;
    public void setOnViewListener(OnViewListener onViewListener){
        this.onViewListener = onViewListener;
    }

    public EditLabelView(Context context) {
        this(context, null);
    }

    public EditLabelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.itemview_edit, this);

        ButterKnife.bind(rootView);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EditLabelView);
        String leftText = ta.getString(R.styleable.EditLabelView_editLabelView_leftText);
//		int leftTextSize = ta.getDimensionPixelSize(R.styleable.EditLabelView_editLabelView_leftTextSize, 12);//像素
        float leftTextSize = ta.getDimension(R.styleable.EditLabelView_editLabelView_leftTextSize, 12);//像素
        int leftTextColor = ta.getColor(R.styleable.EditLabelView_editLabelView_leftTextColor, 0);
        int leftTextWidth = ta.getDimensionPixelSize(R.styleable.EditLabelView_editLabelView_leftTextWidth, 80);//px

        float rightTextSize = ta.getDimension(R.styleable.EditLabelView_editLabelView_rightTextSize, 12);
        int rightTextColor = ta.getColor(R.styleable.EditLabelView_editLabelView_rightTextColor, 0);
        int rightTextColorHint = ta.getColor(R.styleable.EditLabelView_editLabelView_rightTextColorHint, 0);
        String rightHint = ta.getString(R.styleable.EditLabelView_editLabelView_rightHint);
        ta.recycle();

        //
        tvLeftText.setText(leftText);
        tvLeftText.setTextSize(leftTextSize);
//        tvLeftText.setTextSize(DensityUtil.sp2px(context, leftTextSize));
        tvLeftText.setTextColor(leftTextColor);
        tvLeftText.setWidth(leftTextWidth);

//		etContent.setText(leftText);

        etContent.setHint(rightHint);
        etContent.setTextSize(rightTextSize);
        etContent.setTextColor(rightTextColor);
        etContent.setHintTextColor(rightTextColorHint);

        etContent.setInputType(InputType.TYPE_NULL);

//        etContent.setFocusableInTouchMode(false);
//        etContent.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!softKeyboardEnabled) {
//                    DeviceUtils.hideSoftInput(getContext(), v);
//                }
//            }
//        });
        //
        etContent.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP && !softKeyboardEnabled){
                    DeviceUtils.hideSoftInput(getContext(), etContent);
                }
                etContent.requestFocus();
                etContent.setSelection(etContent.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etContent.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d(String.format("setOnKeyListener(%s.%s):%d", TAG, etContent.getClass().getSimpleName(), event.getKeyCode()));
                //Press “Enter”
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (onViewListener != null){
                            onViewListener.onInput(etContent.getText().toString());
                        }
                    }

                    return true;
                }
                //Press “F5”，盘点机，F5对"Scan"扫描按钮
                if (event.getKeyCode() == KeyEvent.KEYCODE_F5) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (onViewListener != null){
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
     * 配置
     */
    public void config(int inputType) {
        if (inputType == INPUT_TYPE_NUMBER) {
            etContent.setInputType(InputType.TYPE_NULL);
            etContent.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        } else if (inputType == INPUT_TYPE_NUMBER_DECIMAL) {
//			etContent.setInputType(InputType.TYPE_CLASS_NUMBER);
            etContent.setInputType(InputType.TYPE_NULL);
            etContent.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        } else {
            etContent.setInputType(InputType.TYPE_CLASS_TEXT);
        }
    }

    public void setTvTitle(String text) {
        this.tvLeftText.setText(text);
    }

    public void setEtContent(String text) {
        this.etContent.setText(text);
    }

    public String getEtContent() {
        return etContent.getText().toString();
    }

    public boolean isSoftKeyboardEnabled() {
        return softKeyboardEnabled;
    }

    public void setSoftKeyboardEnabled(boolean softKeyboardEnabled) {
        this.softKeyboardEnabled = softKeyboardEnabled;
    }


}
