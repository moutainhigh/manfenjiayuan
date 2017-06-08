package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.utils.DecimalInputFilter;
import com.mfh.litecashier.R;


/**
 * 修改数量
 *
 * @author bingshanguxue
 */
public class FrontCategoryGoodsDialog extends CommonDialog {

    /**
     * 输入框小数的位数
     */
    private static int DECIMAL_DIGITS = 2;

    public interface OnResponseCallback {
        void onAction1(Double value);
        void onAction2();
        void onAction3();
    }

    private OnResponseCallback mListener;
    private View rootView;
    private EditText etValue;
    private TextView tvUnit;
    private Button btnAction3;
    private Double hintValue = 0D;
    private String unit;
    private boolean minimumIntCheckEnabled = false;
    private int minimumIntCheckValue = 0;
    private boolean minimumDoubleCheckEnabled = false;
    private Double minimumDoubleCheckValue = 0D;

    private FrontCategoryGoodsDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private FrontCategoryGoodsDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_frontcategory_goods, null);
//        ButterKnife.bind(rootView);

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
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()) {
                        DeviceUtils.showSoftInput(getContext(), etValue);
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), etValue);
                    }
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
                // 直接设置文字，不会触发filter
                etValue.append("0");
            }
        });
        rootView.findViewById(R.id.key_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etValue.append("1");
            }
        });
        rootView.findViewById(R.id.key_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etValue.append("2");
            }
        });
        rootView.findViewById(R.id.key_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etValue.append("3");
            }
        });
        rootView.findViewById(R.id.key_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etValue.append("4");
            }
        });
        rootView.findViewById(R.id.key_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etValue.append("5");
            }
        });
        rootView.findViewById(R.id.key_6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etValue.append("6");
            }
        });
        rootView.findViewById(R.id.key_7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etValue.append("7");
            }
        });
        rootView.findViewById(R.id.key_8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etValue.append("8");
            }
        });
        rootView.findViewById(R.id.key_dot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etValue.append(".");
            }
        });
        rootView.findViewById(R.id.key_9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etValue.append("9");
            }
        });
        rootView.findViewById(R.id.key_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int len = etValue.length();
                if (len > 0) {
                    String text = etValue.getText().toString();
                    etValue.setText(text.substring(0, Math.max(0, len - 1)));
                }
            }
        });
        rootView.findViewById(R.id.key_del).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                etValue.setText("");
                return true;
            }
        });
        rootView.findViewById(R.id.button_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        rootView.findViewById(R.id.button_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                if (mListener != null){
                    mListener.onAction2();
                }
            }
        });
        btnAction3 = (Button) rootView.findViewById(R.id.button_sellout);
        btnAction3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DialogUtil.showHint(R.string.coming_soon);
                dismiss();

                if (mListener != null){
                    mListener.onAction3();
                }
            }
        });

        setContent(rootView, 0);
    }

    public FrontCategoryGoodsDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (getWindow() != null) {
            getWindow().setGravity(Gravity.CENTER);
        }
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = DensityUtil.dip2px(getContext(), 400);
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void initialzie(int decimalDigits, Double hintValue, String unit,
                     OnResponseCallback callback) {
        this.hintValue = hintValue;
        this.unit = unit;
        this.mListener = callback;
        DECIMAL_DIGITS = decimalDigits;

//        etValue.setText(hintValue.toString());
        etValue.getText().clear();
        etValue.setHint(String.format("%.2f", hintValue));
        etValue.setSelection(etValue.length());
        etValue.setFilters(new InputFilter[]{new DecimalInputFilter(DECIMAL_DIGITS)});
        etValue.requestFocus();

        if (StringUtils.isEmpty(unit)){
            tvUnit.setVisibility(View.GONE);
        }
        else{
            tvUnit.setText(unit);
            tvUnit.setVisibility(View.VISIBLE);
        }
    }

    public void setAction3(String text){
        btnAction3.setText(text);
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
                    mListener.onAction1(newQuantity);
                }
            }

            dismiss();
        } catch (Exception e) {
//                        java.lang.NumberFormatException: Invalid double: "88.0.08"
            ZLogger.e(e.toString());
        }

    }

}
