package com.bingshanguxue.vector_uikit;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.framework.core.utils.StringUtils;


/**
 * 修改数字。
 * Created by bingshanguxue on 2015/5/10.
 */
public class NumberPickerView extends LinearLayout {

    private View rootView;
    private TextView tvNumber;
    private ImageButton btnDecrease;
    private ImageButton btnIncrease;

    private int MIN = 0;
    private int MAX = Integer.MAX_VALUE;
    private int number = 0;

    private boolean isdecreaseDrawableAutoHide = false;
    private boolean zeroIntercept = false;//数字改变时是否拦截处理

    public interface onOptionListener {
        void onPreIncrease();

        void onPreDecrease();

        void onValueChanged(int value);
    }

    private onOptionListener optionListener;

    public void setonOptionListener(onOptionListener optionListener) {
        this.optionListener = optionListener;
    }

    public NumberPickerView(Context context) {
        super(context);
        rootView = LayoutInflater.from(context).inflate(R.layout.view_numberpicker, this, true);
        this.initAndSetUpView(null);
    }

    public NumberPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rootView = LayoutInflater.from(context).inflate(R.layout.view_numberpicker, this, true);
        this.initAndSetUpView(attrs);
    }

    public NumberPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        rootView = LayoutInflater.from(context).inflate(R.layout.view_numberpicker, this, true);
        this.initAndSetUpView(attrs);
    }

    private void initAndSetUpView(AttributeSet attrs) {
        btnDecrease = (ImageButton) rootView.findViewById(R.id.btn_decrease);
        btnIncrease = (ImageButton) rootView.findViewById(R.id.btn_increase);
        tvNumber = (TextView) rootView.findViewById(R.id.tv_number);

        btnDecrease.setFocusable(false);
        btnDecrease.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                decrease();
            }
        });
        btnIncrease.setFocusable(false);
        btnIncrease.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                increase();
            }
        });
        tvNumber.setText(String.valueOf(MIN));
        tvNumber.setEnabled(false);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.NumberPickerView);
            btnDecrease.setImageResource(a.getResourceId(R.styleable.NumberPickerView_decreaseDrawable,
                            R.drawable.btn_decrease));
            btnIncrease.setImageResource(a.getResourceId(R.styleable.NumberPickerView_increaseDrawable,
                            R.drawable.btn_increase));
            isdecreaseDrawableAutoHide = a.getBoolean(R.styleable.NumberPickerView_decreaseDrawableAutoHide,
                    false);
            zeroIntercept = a.getBoolean(R.styleable.NumberPickerView_zeroIntercept,
                    false);
            a.recycle();
        }
        setValue(0);
    }

    public void decrease() {
        if (zeroIntercept) {
            if (optionListener != null) {
                optionListener.onPreDecrease();
            }
            return;
        }

        setValue(number - 1);
        if (optionListener != null) {
            optionListener.onValueChanged(getValue());
        }
    }

    public void increase() {
//        if (zeroIntercept) {
//            if (optionListener != null) {
//                optionListener.onPreIncrease();
//            }
//            return;
//        }

        setValue(number + 1);
        if (optionListener != null) {
            optionListener.onValueChanged(getValue());
        }
    }

    public void setValue(String value) {
        if (StringUtils.isEmpty(value)) {
            setValue(0);
        } else {
            setValue(Integer.valueOf(value));
        }
    }

    /**
     * 设置数字
     * */
    public void setValue(int paramInt) {
        if (number != paramInt && paramInt >= MIN && paramInt <= MAX) {
            number = paramInt;
            tvNumber.setText(String.valueOf(paramInt));
        }

        // 当数字小于等于最小值时自动隐藏‘－’按钮
        if (number <= MIN){
            if (isdecreaseDrawableAutoHide){
                btnDecrease.setVisibility(INVISIBLE);
                tvNumber.setVisibility(INVISIBLE);
            }
            else{
                btnDecrease.setVisibility(VISIBLE);
                btnDecrease.setEnabled(false);
                tvNumber.setVisibility(VISIBLE);
            }
        }
        else{
            btnDecrease.setVisibility(VISIBLE);
            btnDecrease.setEnabled(true);
            tvNumber.setVisibility(VISIBLE);
        }
    }

    public int getValue() {
        return this.number;
    }

    public void setMaxValue(int paramInt) {
        if (this.MAX == paramInt) {
            return;
        }

        if (paramInt < 0) {
            throw new IllegalArgumentException("maxValue must be >= 0");
        }

        this.MAX = paramInt;
    }

    public void setMinValue(int paramInt) {
        if (this.MIN == paramInt) {
            return;
        }

        if (paramInt < 0) {
            throw new IllegalArgumentException("maxValue must be >= 0");
        }

        this.MIN = paramInt;
    }

    public void setZeroIntercept(boolean zeroIntercept) {
        this.zeroIntercept = zeroIntercept;
    }

    public void setDecreaseButton(int resId) {
        this.btnDecrease.setImageResource(resId);
    }

    public void setIncreaseButton(int resId) {
        this.btnIncrease.setImageResource(resId);
    }


    /**
     * 设置样式
     */
    public void setTheme(int decResId, int incResId) {
        this.btnDecrease.setImageResource(decResId);
        this.btnIncrease.setImageResource(incResId);
    }

}
