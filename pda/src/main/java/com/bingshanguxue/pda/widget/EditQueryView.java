package com.bingshanguxue.pda.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.bingshanguxue.pda.R;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;

//import butterknife.Bind;
//import butterknife.ButterKnife;
//import butterknife.OnClick;


/**
 * 复合控件－－（输入框 + 按键）
 * 支持自定义属性，可以直接在xml文件中配置。
 * * <declare-styleable name="EditQueryView">
 *     <attr name="editQueryView_inputText" format="string" />
 *     <attr name="editQueryView_inputTextColor" format="color" />
 *     <attr name="editQueryView_inputTextSize" format="dimension" />
 *     <attr name="editQueryView_inputHint" format="string" />
 *     <attr name="editQueryView_rightButtonWidth" format="dimension" />
 *     <attr name="editLabelView_rightImageButtonSrc" format="reference" />
 *   </declare-styleable>
 */
public class EditQueryView extends LinearLayout {
    private static final String TAG = "EditQueryView";

    public static final int INPUT_TYPE_NUMBER = 0;
    public static final int INPUT_TYPE_NUMBER_DECIMAL = 1;
    public static final int INPUT_TYPE_TEXT = 2;

//    @Bind(R.id.et_input)
    private EditText etInput;
//    @Bind(R.id.ib_search)
    private ImageButton ibRight;

    private boolean softKeyboardEnabled;//是否支持软键盘
    private boolean inputSubmitEnabled; //输入框是否支持回车提交动作
    private boolean holdFocusEnable;//是否持有焦点，不受其它按键影响。

    public interface OnViewListener{
        void onSubmit(String text);
    }
    private OnViewListener onViewListener;
    public void setOnViewListener(OnViewListener onViewListener){
        this.onViewListener = onViewListener;
    }

    public EditQueryView(Context context) {
        this(context, null);
    }

    public EditQueryView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.edit_query_view, this);

//        ButterKnife.bind(rootView);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EditQueryView);
        int rightImageButtonResId = ta.getResourceId(R.styleable.EditQueryView_editLabelView_rightImageButtonSrc,
                R.mipmap.ic_search_green);

        float inputTextSize = ta.getDimension(R.styleable.EditQueryView_editQueryView_inputTextSize, 12);
        int inputTextColor = ta.getColor(R.styleable.EditQueryView_editQueryView_inputTextColor, 0);
        int inputTextColorHint = ta.getColor(R.styleable.EditQueryView_editLabelView_inputTextColorHint, 0);
        String inputHint = ta.getString(R.styleable.EditQueryView_editQueryView_inputHint);
        ta.recycle();

        ibRight = (ImageButton) rootView.findViewById(R.id.ib_search);
        ibRight.setImageResource(rightImageButtonResId);
        etInput = (EditText) rootView.findViewById(R.id.et_input);
        etInput.setTextSize(inputTextSize);
        etInput.setTextColor(inputTextColor);
        etInput.setHintTextColor(inputTextColorHint);
        etInput.setHint(inputHint);
        etInput.setInputType(InputType.TYPE_NULL);

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

                if(event.getAction() == MotionEvent.ACTION_UP){
                    if (softKeyboardEnabled){
                        DeviceUtils.showSoftInput(getContext(), etInput);
                    }else{
                        DeviceUtils.hideSoftInput(getContext(), etInput);
                    }
                }
                etInput.requestFocus();
                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etInput.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d(String.format("%s.onKey: %d", TAG, event.getKeyCode()));
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP && inputSubmitEnabled) {
                        submit();
                    }

                    return true;
                }

                //Press Directional “UP-DOWN-LEFT-RIGHT”
                return (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) && holdFocusEnable;

            }
        });
    }

    /**
     * 配置
     */
    public void config(int inputType) {
        if (inputType == INPUT_TYPE_NUMBER) {
            etInput.setInputType(InputType.TYPE_NULL);
            etInput.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        } else if (inputType == INPUT_TYPE_NUMBER_DECIMAL) {
//			etContent.setInputType(InputType.TYPE_CLASS_NUMBER);
            etInput.setInputType(InputType.TYPE_NULL);
            etInput.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        } else {
            etInput.setInputType(InputType.TYPE_CLASS_TEXT);
        }
    }

    public boolean isSoftKeyboardEnabled() {
        return softKeyboardEnabled;
    }

    public void setSoftKeyboardEnabled(boolean enabled) {
        this.softKeyboardEnabled = enabled;

        etInput.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if (softKeyboardEnabled){
                        DeviceUtils.showSoftInput(getContext(), etInput);
                    }else{
                        DeviceUtils.hideSoftInput(getContext(), etInput);
                    }
                }
                requestFocusEnd();
                //返回true,不再继续传递事件
                return true;
            }
        });
    }

    public boolean isInputSubmitEnabled() {
        return inputSubmitEnabled;
    }

    public void setInputSubmitEnabled(boolean inputSubmitEnabled) {
        this.inputSubmitEnabled = inputSubmitEnabled;
    }

    public void setHoldFocusEnable(boolean holdFocusEnable) {
        this.holdFocusEnable = holdFocusEnable;
    }

    public String getInputString() {
        return etInput.getText().toString();
    }

    public void setInputString(String text) {
        this.etInput.setText(text);
    }

    public void clear(){
        this.etInput.getText().clear();
    }

//    @OnClick(R.id.ib_search)
    public void submit(){
        if (onViewListener != null && etInput.length() > 0){
            onViewListener.onSubmit(etInput.getText().toString());
        }
    }

    /**
     * 获取焦点并定位到最后
     * */
    public void requestFocusEnd(){
        etInput.requestFocus();
        etInput.setSelection(etInput.length());
    }

}
