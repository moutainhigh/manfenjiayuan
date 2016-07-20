package com.manfenjiayuan.cashierdisplay.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.manfenjiayuan.cashierdisplay.R;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.dialog.CommonDialog;

import java.util.Locale;


/**
 * 输入手机号
 *
 * @author NAT.ZZN(bingshanguxue)
 */
public class PhoneInputDialog extends CommonDialog {

    public interface OnResponseCallback {
        void onQuantityChanged(Double quantity);
    }

    private OnResponseCallback mListener;
    private View rootView;
    private EditText etPhone;
    private ImageButton ibDel;
    private Button btnEnter;

    private PhoneInputDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private PhoneInputDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_phoneinput, null);
//        ButterKnife.bind(rootView);

        etPhone = (EditText) rootView.findViewById(R.id.et_phone);
        etPhone.setCursorVisible(false);//隐藏光标
        etPhone.setFocusable(true);
        etPhone.setFocusableInTouchMode(true);
//        etPhone.setFilters(new InputFilter[]{new DecimalInputFilter(DECIMAL_DIGITS)});
        etPhone.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        dismiss();
                        submit();
                    }
                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etPhone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPreferencesManager.isSoftKeyboardEnabled()) {
                        DeviceUtils.showSoftInput(getContext(), etPhone);
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), etPhone);
                    }
                }
                etPhone.requestFocus();
                etPhone.setSelection(etPhone.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    ibDel.setVisibility(View.VISIBLE);
                    btnEnter.setEnabled(true);
                } else {
                    ibDel.setVisibility(View.INVISIBLE);
                    btnEnter.setEnabled(false);
                }
            }
        });

        rootView.findViewById(R.id.key_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tvQuantity.append("0");
                // 直接设置文字，不会触发filter
                etPhone.append("0");
            }
        });
        rootView.findViewById(R.id.key_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPhone.append("1");
            }
        });
        rootView.findViewById(R.id.key_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPhone.append("2");
            }
        });
        rootView.findViewById(R.id.key_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPhone.append("3");
            }
        });
        rootView.findViewById(R.id.key_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPhone.append("4");
            }
        });
        rootView.findViewById(R.id.key_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPhone.append("5");
            }
        });
        rootView.findViewById(R.id.key_6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPhone.append("6");
            }
        });
        rootView.findViewById(R.id.key_7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPhone.append("7");
            }
        });
        rootView.findViewById(R.id.key_8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPhone.append("8");
            }
        });
        rootView.findViewById(R.id.key_9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPhone.append("9");
            }
        });
        ibDel = (ImageButton)rootView.findViewById(R.id.key_del);
        ibDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int len = etPhone.length();
                if (len > 0) {
                    String text = etPhone.getText().toString();
                    etPhone.setText(text.substring(0, Math.max(0, len - 1)));
                }
            }
        });
        ibDel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                etPhone.getText().clear();
                return true;
            }
        });
        btnEnter = (Button) rootView.findViewById(R.id.key_enter);
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();

                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public PhoneInputDialog(Context context) {
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

    public void init(OnResponseCallback callback) {
        this.mListener = callback;

//        etQuantity.setText(rawQuantity.toString());
        etPhone.getText().clear();
        etPhone.setSelection(etPhone.length());
//        etPhone.setFilters(new InputFilter[]{new DecimalInputFilter(DECIMAL_DIGITS)});
        etPhone.requestFocus();
    }

    private void submit(){
        String phonenumber = etPhone.getText().toString();
        if (!StringUtils.isEmpty(phonenumber)) {
        }

        DialogUtil.showHint(PhoneNumberUtils.formatNumber(phonenumber, Locale.CHINA.getISO3Country()));
        if (mListener  != null){
            mListener.onQuantityChanged(0D);
        }
    }

}
