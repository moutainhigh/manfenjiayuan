package com.mfh.litecashier.ui.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.EditInputType;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.utils.DecimalInputFilter;
import com.mfh.litecashier.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;


/**
 * 复合控件－－（输入框 + 按键）
 * 支持自定义属性，可以直接在xml文件中配置。
 * * <declare-styleable name="EditQueryView">
 * <attr name="editQueryView_inputText" format="string" />
 * <attr name="editQueryView_inputTextColor" format="color" />
 * <attr name="editQueryView_inputTextSize" format="dimension" />
 * <attr name="editQueryView_inputHint" format="string" />
 * <attr name="editQueryView_rightButtonWidth" format="dimension" />
 * <attr name="editLabelView_rightImageButtonSrc" format="reference" />
 * </declare-styleable>
 */
public class InputNumberLabelView extends LinearLayout {

    @BindView(R.id.tv_label_title)
    TextView tvTitle;
    @BindView(R.id.et_label_input)
    EditText etInput;
    @BindView(R.id.iv_del)
    ImageView ivDel;
    @BindView(R.id.ib_label_action1)
    ImageButton ibAction1;


    /**
     * 是否支持软键盘
     * <ol>
     * <li>false, 默认使用物理键盘</li>
     * <li>false, 可以通过全局变量控制打开（系统）软键盘</li>
     * <li>true, 使用系统软键盘</li>
     * <li>true, 自定义OnTouchListener实现自定义的键盘</li>
     * </ol>
     */
    private boolean softKeyboardEnabled;
    /**
     * 拦截按键
     * //Press “Enter” KeyEvent.KEYCODE_ENTER
     * //Press “*” KeyEvent.KEYCODE_NUMPAD_MULTIPLY
     * //Press “＋” KeyEvent.KEYCODE_NUMPAD_ADD
     */
    private int[] interceptKeys;

    public interface OnInterceptListener {
        void onKey(int keyCode, String text);
    }

    private OnInterceptListener mOnInterceptListener;

    /**
     * 拦截按键
     */
    public void registerIntercept(int[] interceptKeys, OnInterceptListener onInterceptListener) {
        this.interceptKeys = interceptKeys;
        this.mOnInterceptListener = onInterceptListener;
    }


    public interface OnViewListener {
        void onClickAction1(String text);

        void onLongClickAction1(String text);
    }

    private OnViewListener mOnViewListener;

    public void registerOnViewListener(OnViewListener onViewListener) {
        this.mOnViewListener = onViewListener;
    }

    private boolean clearOnClickDel = true;//默认单击删除按钮清空内容


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
        if (StringUtils.isEmpty(title)) {
            tvTitle.setVisibility(GONE);
        } else {
            tvTitle.setText(title);
//            tvTitle.setWidth(titleWidth);
//            tvTitle.setLayoutParams(new ViewGroup.LayoutParams(titleWidth, ViewGroup.LayoutParams.MATCH_PARENT));
            tvTitle.setVisibility(VISIBLE);
        }

        int editTextSizeInPx = ta.getDimensionPixelSize(R.styleable.InputNumberLabelView_editTextSize, 16);
        int editTextSizeInSp = DensityUtil.px2sp(getContext(), editTextSizeInPx);
        etInput.setTextSize(editTextSizeInSp);
        etInput.setTextColor(ta.getColor(R.styleable.InputNumberLabelView_editTextColor, 0));
        etInput.setHintTextColor(ta.getColor(R.styleable.InputNumberLabelView_editTextColorHint, 0));
        etInput.setFocusable(ta.getBoolean(R.styleable.InputNumberLabelView_editFocusable, true));
        etInput.setFocusableInTouchMode(ta.getBoolean(R.styleable.InputNumberLabelView_editFocusableInTouchMode, true));
        Drawable editBackground = ta.getDrawable(R.styleable.InputNumberLabelView_editBackground);
        if (editBackground != null) {
            etInput.setBackground(editBackground);
        }
//        else{
//            etInput.setBackground(R);
//        }
        etInput.setHint(ta.getString(R.styleable.InputNumberLabelView_inputNumberLabelView_inputHint));
        int inputType = ta.getInteger(R.styleable.InputNumberLabelView_editInputType, EditInputType.TEXT);
        switch (inputType) {
            case EditInputType.BARCODE:
                etInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                break;
            case EditInputType.NUMBER_DECIMAL:
            case EditInputType.PRICE:
                //相当于在.xml文件中设置inputType="numberDecimal
                etInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                this.etInput.setFilters(new InputFilter[]{new DecimalInputFilter(2)});
                break;
            case EditInputType.WEIGHT:
                //相当于在.xml文件中设置inputType="numberDecimal
                etInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                this.etInput.setFilters(new InputFilter[]{new DecimalInputFilter(3)});
                break;
            case EditInputType.NUMBER:
                //相当于在.xml文件中设置inputType="number
                etInput.setInputType(InputType.TYPE_CLASS_NUMBER);
//                this.etInput.setFilters(new InputFilter[]{new DecimalInputFilter(3)});
                break;
            case EditInputType.TEXT_PASSWORD:
                etInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                break;
            case EditInputType.PHONE:
                //相当于在.xml文件中设置inputType="phone
                etInput.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            default:
                etInput.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }

        int rightImageButtonResId = ta.getResourceId(R.styleable.InputNumberLabelView_inputNumberLabelView_rightImageButtonSrc, R.mipmap.ic_search_del);
        ibAction1.setImageResource(rightImageButtonResId);

        clearOnClickDel = ta.getBoolean(R.styleable.InputNumberLabelView_clearOnClickDel, true);
        softKeyboardEnabled = ta.getBoolean(R.styleable.InputNumberLabelView_softKeyboardEnabled, false);

        ta.recycle();

//
//        etInput.setFocusable(true);
//        etInput.setFocusableInTouchMode(true);//不自动获取EditText的焦点
        etInput.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //私有属性优先
                    if (softKeyboardEnabled || SharedPrefesManagerFactory.isSoftInputEnabled()) {
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
        etInput.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //intercept keys
                if (mOnInterceptListener != null && interceptKeys != null && interceptKeys.length > 0) {
                    for (int interceptKey : interceptKeys) {
                        if (interceptKey == keyCode) {
                            if (event.getAction() == MotionEvent.ACTION_UP) {
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

//    /**
//     * 配置
//     */
//    public void setFilters(KeyListener input, int digits) {
//        etInput.setInputType(InputType.TYPE_NULL);
//        etInput.setKeyListener(input);
//        decimalDigits = digits;
////        etInput.setFilters(new InputFilter[]{lengthfilter});
//        etInput.setFilters(new InputFilter[]{new DecimalInputFilter(decimalDigits)});
//    }
//
//    /**
//     * 设置小数点位数*/
//    public void setDigits(int digits){
////        etInput.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
//        etInput.setInputType(InputType.TYPE_NULL);
//        etInput.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
//        decimalDigits = digits;
////        etInput.setFilters(new InputFilter[]{lengthfilter});
//        etInput.setFilters(new InputFilter[]{new DecimalInputFilter(decimalDigits)});
//    }

    public boolean isSoftKeyboardEnabled() {
        return softKeyboardEnabled;
    }

    public void setSoftKeyboardEnabled(boolean enabled) {
        this.softKeyboardEnabled = enabled;
    }


    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        etInput.setEnabled(enabled);
        if (enabled) {
            ibAction1.setVisibility(VISIBLE);
        } else {
            ibAction1.setVisibility(GONE);
        }
    }

    /**
     * 获取焦点
     */
    public void requestFocusEnd() {
        etInput.requestFocus();
        etInput.setSelection(etInput.length());
    }

    /**
     * 设置提示文字
     */
    public void setHintText(String hintText) {
        this.etInput.setHint(hintText);
    }

    public String getInputString() {
        return etInput.getText().toString();
    }

    public void setInputString(String text) {
        this.etInput.setText(text);
    }

    public void appendInput(CharSequence text) {
        if (!StringUtils.isEmpty(text)) {
            etInput.append(text);
        }
    }

    @OnClick(R.id.iv_del)
    public void clear() {
        if (clearOnClickDel) {
            etInput.getText().clear();
        } else {
            int len = etInput.getText().length();
            if (len > 0) {
                etInput.getText().delete(Math.max(0, len - 1), len);
            }
        }
    }

    @OnClick(R.id.ib_label_action1)
    public void clickAction1() {
        if (mOnViewListener != null) {
            mOnViewListener.onClickAction1(etInput.getText().toString());
        }
    }

    /**
     * 长按清除文字
     */
    @OnLongClick(R.id.ib_label_action1)
    public boolean longClickAction1() {
        if (mOnViewListener != null) {
            mOnViewListener.onLongClickAction1(etInput.getText().toString());
        }
        return true;
    }

    public void setAction1Selected(boolean selected) {
        this.ibAction1.setSelected(selected);
    }

    public boolean isAction1Selected() {
        return this.ibAction1.isSelected();
    }

    public void setOnInoutKeyListener(OnKeyListener l) {
        etInput.setOnKeyListener(l);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        etInput.addTextChangedListener(watcher);
    }

    public void setOnTouchListener(OnTouchListener l) {
        etInput.setOnTouchListener(l);
    }

}
