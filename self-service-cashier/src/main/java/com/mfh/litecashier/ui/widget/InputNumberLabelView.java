package com.mfh.litecashier.ui.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.utils.DecimalInputFilter;
import com.mfh.litecashier.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;


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
public class InputNumberLabelView extends LinearLayout {
    private static final String TAG = "InputNumberLabelView";

    public static final int INPUT_TYPE_NUMBER = 0;
    public static final int INPUT_TYPE_NUMBER_DECIMAL = 1;
    public static final int INPUT_TYPE_TEXT = 2;

    /** 输入框小数的位数*/
    private static final int DECIMAL_DIGITS = 2;

    @Bind(R.id.tv_label_title)
    TextView tvTitle;
    @Bind(R.id.et_label_input)
    EditText etInput;
    @Bind(R.id.ib_label_action1)
    ImageButton ibAction1;


    private boolean softKeyboardEnabled;//是否支持软键盘
    private boolean enterKeySubmitEnabled;//输入框是否支持回车提交
    private boolean clearOnClickDel;//单击删除按钮清空内容

    private int decimalDigits = DECIMAL_DIGITS;

    public interface OnViewListener{
        void onSubmit(String text);
    }
    private OnViewListener onViewListener;
    public void setOnViewListener(OnViewListener onViewListener){
        this.onViewListener = onViewListener;
    }

    public InputNumberLabelView(Context context) {
        this(context, null);
    }

    public InputNumberLabelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.input_number_labelview, this);

        ButterKnife.bind(rootView);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.InputNumberLabelView);
        String title = ta.getString(R.styleable.InputNumberLabelView_inputNumberLabelView_title);
//        int titleWidth = ta.getDimensionPixelSize(R.styleable.InputNumberLabelView_inputNumberLabelView_titleWidth, 72);//px

        float inputTextSize = ta.getDimension(R.styleable.InputNumberLabelView_inputNumberLabelView_inputTextSize, 16);
        int inputTextColor = ta.getColor(R.styleable.InputNumberLabelView_inputNumberLabelView_inputTextColor, 0);
        int inputTextColorHint = ta.getColor(R.styleable.InputNumberLabelView_inputNumberLabelView_inputTextColorHint, 0);
        String inputHint = ta.getString(R.styleable.InputNumberLabelView_inputNumberLabelView_inputHint);

//        etInput.setInputType(input);
        int rightImageButtonResId = ta.getResourceId(R.styleable.InputNumberLabelView_inputNumberLabelView_rightImageButtonSrc, R.mipmap.ic_search_del);
        clearOnClickDel = ta.getBoolean(R.styleable.InputNumberLabelView_clearOnClickDel, false);

        ta.recycle();

        if (StringUtils.isEmpty(title)){
            tvTitle.setVisibility(GONE);
        }
        else{
            tvTitle.setText(title);
//            tvTitle.setWidth(titleWidth);
//            tvTitle.setLayoutParams(new ViewGroup.LayoutParams(titleWidth, ViewGroup.LayoutParams.MATCH_PARENT));
            tvTitle.setVisibility(VISIBLE);
        }

        ibAction1.setImageResource(rightImageButtonResId);

        etInput.setTextSize(inputTextSize);
        etInput.setTextColor(inputTextColor);
        etInput.setHintTextColor(inputTextColorHint);
        etInput.setHint(inputHint);

        etInput.setFocusable(true);
        etInput.setFocusableInTouchMode(true);//不自动获取EditText的焦点
        etInput.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if (SharedPreferencesManager.isSoftKeyboardEnabled() || softKeyboardEnabled){
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
        etInput.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (enterKeySubmitEnabled && onViewListener != null && etInput.length() > 0) {
                            onViewListener.onSubmit(etInput.getText().toString());
                        }
                    }

                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }

    /**
     * 配置
     */
    public void setFilters(KeyListener input, int digits) {
        etInput.setInputType(InputType.TYPE_NULL);
        etInput.setKeyListener(input);
        decimalDigits = digits;
//        etInput.setFilters(new InputFilter[]{lengthfilter});
        etInput.setFilters(new InputFilter[]{new DecimalInputFilter(decimalDigits)});
    }

    /**
     * 设置小数点位数*/
    public void setDigits(int digits){
//        etInput.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etInput.setInputType(InputType.TYPE_NULL);
        etInput.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        decimalDigits = digits;
//        etInput.setFilters(new InputFilter[]{lengthfilter});
        etInput.setFilters(new InputFilter[]{new DecimalInputFilter(decimalDigits)});
    }

    public boolean isSoftKeyboardEnabled() {
        return softKeyboardEnabled;
    }

    public void setSoftKeyboardEnabled(boolean enabled) {
        this.softKeyboardEnabled = enabled;
    }


    public boolean isEnterKeySubmitEnabled() {
        return enterKeySubmitEnabled;
    }

    public void setEnterKeySubmitEnabled(boolean enterKeySubmitEnabled) {
        this.enterKeySubmitEnabled = enterKeySubmitEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        etInput.setEnabled(enabled);
        if (enabled){
            ibAction1.setVisibility(VISIBLE);
        }
        else{
            ibAction1.setVisibility(GONE);
        }
    }

    public void requestFocusEnd(){
        etInput.requestFocus();
        etInput.setSelection(etInput.length());
    }
    /**
     * 设置提示文字
     * */
    public void setHintText(String hintText){
        this.etInput.setHint(hintText);
    }

    public String getInputString() {
        return etInput.getText().toString();
    }

    public void setInputString(String text) {
        this.etInput.setText(text);
    }

    @OnClick(R.id.ib_label_action1)
    public void action1(){
        if (clearOnClickDel){
            etInput.getText().clear();
            return;
        }

        int len = etInput.getText().length();
        if (len > 0){
            etInput.getText().delete(Math.max(0, len-1), len);
        }
    }

    /**
     * 长按清除文字
     * */
    @OnLongClick(R.id.ib_label_action1)
    public boolean clear(){
        etInput.getText().clear();
        return true;
    }

    public void setOnInoutKeyListener(OnKeyListener l){
        etInput.setOnKeyListener(l);
    }

    public void addTextChangedListener(TextWatcher watcher){
        etInput.addTextChangedListener(watcher);
    }

    public void setOnTouchListener(OnTouchListener l){
        etInput.setOnTouchListener(l);
    }

}
