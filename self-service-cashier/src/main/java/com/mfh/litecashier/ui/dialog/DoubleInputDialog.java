package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.utils.DecimalInputFilter;
import com.mfh.litecashier.R;


/**
 * 修改数量
 *
 * @author NAT.ZZN(bingshanguxue)
 */
public class DoubleInputDialog extends CommonDialog {

    /**
     * 输入框小数的位数
     */
    private static int DECIMAL_DIGITS = 2;

    public interface OnResponseCallback {
        void onQuantityChanged(Double value);
    }

    private OnResponseCallback mListener;
    private View rootView;
    private TextView tvTitle;
    private EditText etQuantity;

    private Double hintValue = 0D;
    private boolean minimumIntCheckEnabled = false;
    private int minimumIntCheckValue = 0;
    private boolean minimumDoubleCheckEnabled = false;
    private Double minimumDoubleCheckValue = 0D;

    private DoubleInputDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private DoubleInputDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_changequantity, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        tvTitle.setText("数量");
        etQuantity = (EditText) rootView.findViewById(R.id.et_quantity);
        etQuantity.setCursorVisible(false);//隐藏光标
        etQuantity.setFocusable(true);
        etQuantity.setFocusableInTouchMode(true);
        etQuantity.setFilters(new InputFilter[]{new DecimalInputFilter(DECIMAL_DIGITS)});
        etQuantity.setOnKeyListener(new View.OnKeyListener() {
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
        etQuantity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPreferencesManager.isSoftKeyboardEnabled()) {
                        DeviceUtils.showSoftInput(getContext(), etQuantity);
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), etQuantity);
                    }
                }
                etQuantity.requestFocus();
                etQuantity.setSelection(etQuantity.length());
                //返回true,不再继续传递事件
                return true;
            }
        });

        rootView.findViewById(R.id.key_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 直接设置文字，不会触发filter
                etQuantity.append("0");
            }
        });
        rootView.findViewById(R.id.key_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuantity.append("1");
            }
        });
        rootView.findViewById(R.id.key_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuantity.append("2");
            }
        });
        rootView.findViewById(R.id.key_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuantity.append("3");
            }
        });
        rootView.findViewById(R.id.key_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuantity.append("4");
            }
        });
        rootView.findViewById(R.id.key_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuantity.append("5");
            }
        });
        rootView.findViewById(R.id.key_6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuantity.append("6");
            }
        });
        rootView.findViewById(R.id.key_7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuantity.append("7");
            }
        });
        rootView.findViewById(R.id.key_8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuantity.append("8");
            }
        });
        rootView.findViewById(R.id.key_dot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuantity.append(".");
            }
        });
        rootView.findViewById(R.id.key_9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuantity.append("9");
            }
        });
        rootView.findViewById(R.id.key_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int len = etQuantity.length();
                if (len > 0) {
                    String text = etQuantity.getText().toString();
                    etQuantity.setText(text.substring(0, Math.max(0, len - 1)));
                }
            }
        });
        rootView.findViewById(R.id.key_del).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                etQuantity.setText("");
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

    public DoubleInputDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.CENTER);

//        WindowManager m = getWindow().getWindowManager();
//        Display d = m.getDefaultDisplay();
//        WindowManager.LayoutParams p = getWindow().getAttributes();
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
        this.hintValue = hintValue;
        this.mListener = callback;
        DECIMAL_DIGITS = decimalDigits;

        tvTitle.setText(title);
//        etQuantity.setText(hintValue.toString());
        etQuantity.getText().clear();
        etQuantity.setHint(String.format("%.2f", hintValue));
        etQuantity.setSelection(etQuantity.length());
        etQuantity.setFilters(new InputFilter[]{new DecimalInputFilter(DECIMAL_DIGITS)});
        etQuantity.requestFocus();
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
        String quantityStr = etQuantity.getText().toString();
        if (StringUtils.isEmpty(quantityStr)) {
            return;
        }

        try {
            Double newQuantity = Double.valueOf(quantityStr);
            //不为空，且有变化时才返回
            if (hintValue != null && newQuantity.compareTo(hintValue) != 0) {
                if (minimumDoubleCheckEnabled &&
                        minimumDoubleCheckValue != null &&
                        minimumDoubleCheckValue.compareTo(newQuantity) > 0) {
                    DialogUtil.showHint(String.format("数量不能低于 %.2f", minimumDoubleCheckValue));
                    return;
                }
                if (mListener != null) {
                    mListener.onQuantityChanged(newQuantity);
                }
            }

            dismiss();
        } catch (Exception e) {
//                        java.lang.NumberFormatException: Invalid double: "88.0.08"
            ZLogger.e(e.toString());
        }

    }

}
