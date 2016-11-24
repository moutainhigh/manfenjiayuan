package com.mfh.litecashier.ui.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.litecashier.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


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
public class InputSearchView extends LinearLayout {
    public static final int INPUT_TYPE_NUMBER = 0;
    public static final int INPUT_TYPE_NUMBER_DECIMAL = 1;
    public static final int INPUT_TYPE_TEXT = 2;

    @BindView(R.id.et_label_input)
    EditText etInput;
    @BindView(R.id.ib_del)
    ImageButton ibDel;
    @BindView(R.id.vertial_line)
    View verticalLine;
    @BindView(R.id.ib_search)
    ImageButton ibSearch;

    /**
     * 是否支持软键盘
     * <ol>
     *     <li>false, 默认使用物理键盘</li>
     *     <li>false, 可以通过全局变量控制打开（系统）软键盘</li>
     *     <li>true, 使用系统软键盘</li>
     *     <li>true, 自定义OnTouchListener实现自定义的键盘</li>
     * </ol>
     * */
    private boolean softKeyboardEnabled;
    private int[] interceptKeys;

    public interface OnInterceptListener{
        void onKey(int keyCode, String text);
    }
    private InputNumberLabelView.OnInterceptListener mOnInterceptListener;

    public InputSearchView(Context context) {
        this(context, null);
    }

    public InputSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.input_searchview, this);

        ButterKnife.bind(rootView);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.InputNumberLabelView);

        float inputTextSize = ta.getDimension(R.styleable.InputNumberLabelView_inputNumberLabelView_inputTextSize, 16);
        int inputTextColor = ta.getColor(R.styleable.InputNumberLabelView_inputNumberLabelView_inputTextColor, 0);
        int inputTextColorHint = ta.getColor(R.styleable.InputNumberLabelView_inputNumberLabelView_inputTextColorHint, 0);
        String inputHint = ta.getString(R.styleable.InputNumberLabelView_inputNumberLabelView_inputHint);
        softKeyboardEnabled = ta.getBoolean(R.styleable.InputNumberLabelView_softKeyboardEnabled, false);

        ta.recycle();

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
                    //打开or关闭系统软键盘
                    if (softKeyboardEnabled || SharedPrefesManagerFactory.isSoftInputEnabled()){
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
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0){
                    ibDel.setVisibility(VISIBLE);
                }
                else{
                    ibDel.setVisibility(INVISIBLE);
                }

//                if (onViewListener != null) {
//                    onViewListener.onSubmit(s.toString());
//                }
            }
        });
        etInput.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(%s.%s):%d", TAG, etInput.getClass().getSimpleName(), keyCode));
                //intercept keys
                if (mOnInterceptListener != null && interceptKeys != null && interceptKeys.length > 0){
                    for (int interceptKey : interceptKeys){
                        if (interceptKey == keyCode){
                            if (event.getAction() == MotionEvent.ACTION_UP){
                                mOnInterceptListener.onKey(keyCode, etInput.getText().toString());
                            }
                            return true;
                        }
                    }
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }

    public void requestFocusEnd(){
        etInput.requestFocus();
        etInput.setSelection(etInput.length());
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

//        if (softKeyboardEnabled){
//            DeviceUtils.showSoftInput(getContext(), etInput);
//        }else{
//            DeviceUtils.hideSoftInput(getContext(), etInput);
//        }
//
//        etInput.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_UP){
//                    if (softKeyboardEnabled){
//                        DeviceUtils.showSoftInput(getContext(), etInput);
//                    }else{
//                        DeviceUtils.hideSoftInput(getContext(), etInput);
//                    }
//                }
//                etInput.requestFocus();
//                etInput.setSelection(etInput.length());
//                //返回true,不再继续传递事件
//                return true;
//            }
//        });
    }

    /**
     * 拦截按键
     * */
    public void registerIntercept(int[] interceptKeys, InputNumberLabelView.OnInterceptListener onInterceptListener){
        this.interceptKeys = interceptKeys;
        this.mOnInterceptListener = onInterceptListener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.etInput.setEnabled(enabled);
        this.etInput.getText().clear();
        this.ibSearch.setEnabled(enabled);
    }

    public String getInputString() {
        return etInput.getText().toString();
    }

    public void setInputString(String text) {
        this.etInput.setText(text);
    }

    @OnClick(R.id.ib_search)
    public void search(){
        if (mOnInterceptListener != null && etInput.length() > 0) {
            mOnInterceptListener.onKey(KeyEvent.KEYCODE_ENTER, etInput.getText().toString());
        }
    }

    public void action1(){
        int len = etInput.getText().length();
        if (len > 0){
            etInput.getText().delete(Math.max(0, len - 1), len);
        }
    }

    /**
     * 长按清除文字
     * */
    @OnClick(R.id.ib_del)
    public void clear(){
        etInput.getText().clear();
        if (mOnInterceptListener != null) {
            mOnInterceptListener.onKey(KeyEvent.KEYCODE_ENTER, etInput.getText().toString());
        }
    }

    public void clear(boolean isSubmitEnabled){
        etInput.getText().clear();
        if (isSubmitEnabled && mOnInterceptListener != null) {
            mOnInterceptListener.onKey(KeyEvent.KEYCODE_ENTER, etInput.getText().toString());
        }
    }

    public void setOnInoutKeyListener(OnKeyListener l){
        etInput.setOnKeyListener(l);
    }

    public void addTextChangedListener(TextWatcher watcher){
        etInput.addTextChangedListener(watcher);
    }

    public void setSearchButtonVisible(boolean isVisible){
        if (isVisible){
            verticalLine.setVisibility(VISIBLE);
            ibSearch.setVisibility(VISIBLE);
        }
        else{
            verticalLine.setVisibility(GONE);
            ibSearch.setVisibility(GONE);
        }
    }

    /**
     * 设置提示文字
     * */
    public void setHintText(String hint){
        etInput.setHint(hint);
    }

    public void setOnTouchListener(OnTouchListener l){
        etInput.setOnTouchListener(l);
    }
}
