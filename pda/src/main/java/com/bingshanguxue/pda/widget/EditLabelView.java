package com.bingshanguxue.pda.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;


/**
 * 复合控件－－（文本＋输入框）
 * 支持自定义属性，可以直接在xml文件中配置。
 */
public class EditLabelView extends LinearLayout {
    private static final String TAG = "EditLabelView";

    public static final int INPUT_TYPE_NUMBER = 0;
    public static final int INPUT_TYPE_NUMBER_DECIMAL = 1;

    private TextView tvLeftText;
    private EditText etContent;
    private TextView tvEndText;

    private boolean softKeyboardEnabled;//是否支持软键盘
    private int[] interceptKeyCodes;

    public interface OnViewListener{
        void onKeycodeEnterClick(String text);
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
        tvLeftText = (TextView) rootView.findViewById(R.id.tv_lefttext);
        etContent = (EditText) rootView.findViewById(R.id.et_content);
        tvEndText = (TextView) rootView.findViewById(R.id.tv_endText);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EditLabelView);
        tvLeftText.setText(ta.getString(R.styleable.EditLabelView_editLabelView_leftText));
        //像素
        tvLeftText.setTextSize(ta.getDimension(R.styleable.EditLabelView_editLabelView_leftTextSize, 12));
//        tvLeftText.setTextSize(DensityUtil.sp2px(context, leftTextSize));
        tvLeftText.setTextColor(ta.getColor(R.styleable.EditLabelView_editLabelView_leftTextColor, 0));
        //px
        tvLeftText.setWidth(ta.getDimensionPixelSize(R.styleable.EditLabelView_editLabelView_leftTextWidth, 80));

        etContent.setHint(ta.getString(R.styleable.EditLabelView_editLabelView_rightHint));
        etContent.setTextSize(ta.getDimension(R.styleable.EditLabelView_editLabelView_rightTextSize, 12));
        etContent.setTextColor(ta.getColor(R.styleable.EditLabelView_editLabelView_rightTextColor, 0));
        etContent.setHintTextColor(ta.getColor(R.styleable.EditLabelView_editLabelView_rightTextColorHint, 0));

        if (ta.getBoolean(R.styleable.EditLabelView_endTextVisible, false)){
            tvEndText.setVisibility(VISIBLE);
        }
        else{
            tvEndText.setVisibility(GONE);
        }
        tvEndText.setText(ta.getString(R.styleable.EditLabelView_endText));
        tvEndText.setTextSize(ta.getDimension(R.styleable.EditLabelView_endTextSize, 12));
//        tvLeftText.setTextSize(DensityUtil.sp2px(context, leftTextSize));
        tvEndText.setTextColor(ta.getColor(R.styleable.EditLabelView_endTextColor, 0));
        tvEndText.setWidth(ta.getDimensionPixelSize(R.styleable.EditLabelView_endTextWidth, 80));

        ta.recycle();

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
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if (softKeyboardEnabled){
                        DeviceUtils.showSoftInput(getContext(), etContent);
                    }else{
                        DeviceUtils.hideSoftInput(getContext(), etContent);
                    }
                }
                requestFocusEnd();
                //返回true,不再继续传递事件
                return true;
            }
        });
        etContent.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d(String.format("(%s):%d", etContent.getClass().getSimpleName(),
                        event.getKeyCode()));
                //Press “Enter”
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (onViewListener != null){
                            onViewListener.onKeycodeEnterClick(etContent.getText().toString());
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
     * 光标定位到最后
     * */
    public void requestFocusEnd(){
        etContent.requestFocus();
        etContent.setSelection(etContent.length());
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

    public void setEndText(String text){
        tvEndText.setText(text);
    }
    public boolean isSoftKeyboardEnabled() {
        return softKeyboardEnabled;
    }

    public void setSoftKeyboardEnabled(boolean softKeyboardEnabled) {
        this.softKeyboardEnabled = softKeyboardEnabled;
    }

    public void addTextChangedListener(TextWatcher watcher){
        etContent.addTextChangedListener(watcher);
    }

}
