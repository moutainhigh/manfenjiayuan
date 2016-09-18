package com.bingshanguxue.cashier;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.EditInputType;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.utils.DecimalInputFilter;


/**
 * 输入窗口
 *
 * @author bingshanguxue
 */
public class NumberInputDialog extends CommonDialog {

    /**
     * 输入框小数的位数
     */
    private static int DECIMAL_DIGITS = 2;

    public interface OnResponseCallback {
        void onNext(String value);
        void onCompleted();
    }

    private OnResponseCallback mListener;
    private View rootView;
    private TextView tvTitle;
    private EditText etValue;
    private TextView tvUnit;
    private Double hintValue = 0D;
    private String unit;
    private boolean minimumIntCheckEnabled = false;
    private int minimumIntCheckValue = 0;
    private boolean minimumDoubleCheckEnabled = false;
    private Double minimumDoubleCheckValue = 0D;

    private NumberInputDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private NumberInputDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_input_style_1, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        tvTitle.setText("数量");
        etValue = (EditText) rootView.findViewById(R.id.et_quantity);
        etValue.setCursorVisible(false);//隐藏光标
        etValue.setFocusable(true);
        etValue.setFocusableInTouchMode(true);
        etValue.setFilters(new InputFilter[]{new DecimalInputFilter(DECIMAL_DIGITS)});
        etValue.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        submit();
                    }
                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etValue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(getContext(), etValue);
                }
                etValue.requestFocus();
                etValue.setSelection(etValue.length());
                //返回true,不再继续传递事件
                return true;
            }
        });

        tvUnit = (TextView) rootView.findViewById(R.id.tv_unit);

        rootView.findViewById(R.id.key_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0));
            }
        });
        rootView.findViewById(R.id.key_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_1));
            }
        });
        rootView.findViewById(R.id.key_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_2));
            }
        });
        rootView.findViewById(R.id.key_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_3));
            }
        });
        rootView.findViewById(R.id.key_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_4));
            }
        });
        rootView.findViewById(R.id.key_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_5));
            }
        });
        rootView.findViewById(R.id.key_6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_6));
            }
        });
        rootView.findViewById(R.id.key_7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_7));
            }
        });
        rootView.findViewById(R.id.key_8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_8));
            }
        });
        rootView.findViewById(R.id.key_dot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_NUMPAD_DOT));
            }
        });
        rootView.findViewById(R.id.key_9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_9));
            }
        });
        rootView.findViewById(R.id.key_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            }
        });
        rootView.findViewById(R.id.key_del).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                etValue.setText("");
                etValue.getText().clear();
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                return true;
            }
        });
        rootView.findViewById(R.id.button_header_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        rootView.findViewById(R.id.button_footer_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();

            }
        });

        setContent(rootView, 0);
    }

    public NumberInputDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.CENTER);

        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = DensityUtil.dip2px(getContext(), 400);
////        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void init(String title, int decimalDigits, Double hintValue, OnResponseCallback callback) {
        initialzie(title, decimalDigits, hintValue, null, callback);
    }

    public void initializeBarcode(String title, int inputType, OnResponseCallback callback){
        initialzie(title, 0, 0D, "", callback);

        if (inputType == EditInputType.BARCODE) {
            etValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        }
        else if (inputType == EditInputType.NUMBER_DECIMAL) {
            //相当于在.xml文件中设置inputType="numberDecimal
            etValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
        else {
            etValue.setInputType(InputType.TYPE_CLASS_TEXT);
        }
    }

    public void initialzie(String title, int decimalDigits, Double hintValue, String unit,
                           OnResponseCallback callback) {
        this.hintValue = hintValue;
        this.unit = unit;
        this.mListener = callback;
        this.DECIMAL_DIGITS = decimalDigits;

        this.tvTitle.setText(title);
//        etValue.setText(hintValue.toString());
        this.etValue.getText().clear();
        if (decimalDigits == 0) {
            this.etValue.setHint(String.format("%.0f", hintValue));
        } else {
            this.etValue.setHint(String.format("%.2f", hintValue));
        }

        this.etValue.setSelection(etValue.length());
        this.etValue.setFilters(new InputFilter[]{new DecimalInputFilter(DECIMAL_DIGITS)});
        this.etValue.requestFocus();

        if (StringUtils.isEmpty(unit)) {
            this.tvUnit.setVisibility(View.GONE);
        } else {
            this.tvUnit.setText(unit);
            this.tvUnit.setVisibility(View.VISIBLE);
        }
    }


    public void setMinimumIntCheck(int minIntValue, boolean checkEnabled) {
        this.minimumIntCheckValue = minIntValue;
        this.minimumIntCheckEnabled = checkEnabled;
    }

    public void setMinimumDoubleCheck(Double minDoubleValue, boolean checkEnabled) {
        this.minimumDoubleCheckValue = minDoubleValue;
        this.minimumDoubleCheckEnabled = checkEnabled;
    }

    private void submit() {
        String quantityStr = etValue.getText().toString();
        if (StringUtils.isEmpty(quantityStr)) {
            return;
        }

        if (mListener != null) {
            mListener.onNext(quantityStr);
        }
        dismiss();

//        try {
//            Double newQuantity = Double.valueOf(quantityStr);
//            //不为空，且有变化时才返回
//            if (hintValue != null && newQuantity.compareTo(hintValue) != 0) {
//                if (minimumDoubleCheckEnabled &&
//                        minimumDoubleCheckValue != null &&
//                        minimumDoubleCheckValue.compareTo(newQuantity) > 0) {
//                    DialogUtil.showHint(String.format("数量不能低于 %.2f", minimumDoubleCheckValue));
//                    return;
//                }
//                if (mListener != null) {
//                    mListener.onNext(newQuantity);
//                }
//            }
//
//            dismiss();
//        } catch (Exception e) {
////                        java.lang.NumberFormatException: Invalid double: "88.0.08"
//            e.printStackTrace();
//            ZLogger.ef(e.toString());
//        }

    }

}
