package com.manfenjiayuan.business.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.manfenjiayuan.business.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.dialog.CommonDialog;


/**
 * 选择货架
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InputNumberDialog extends CommonDialog  {
    private View rootView;
    private TextView tvTitle;
    private EditText etNumber;

    public interface OnDialogListener {
        void onConfirm(String numberInputStr);
    }
    private OnDialogListener listener;


    private InputNumberDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private InputNumberDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_inputnumber, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        etNumber = (EditText) rootView.findViewById(R.id.et_number);
        etNumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()) {
                        DeviceUtils.showSoftInput(getContext(), etNumber);
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), etNumber);
                    }
                }
                etNumber.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d(String.format("setOnKeyListener(etNumber): keyCode=%d, action=%d",
                        keyCode, event.getAction()));
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        String inputStr = etNumber.getText().toString();

                        dismiss();
                        if (!StringUtils.isEmpty(inputStr)){
                            if (listener != null){
                                listener.onConfirm(inputStr);
                            }
                        }
                    }
                    return true;
                }
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        rootView.findViewById(R.id.button_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputStr = etNumber.getText().toString();

                if (!StringUtils.isEmpty(inputStr) && listener != null){
                    listener.onConfirm(inputStr);
                }
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public InputNumberDialog(Context context) {
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
//////        p.width = d.getWidth() * 2 / 3;
//////        p.y = DensityUtil.dip2px(getContext(), 44);
//        p.height = d.getHeight();
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);


        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void show() {
        super.show();
    }


    public void init(String title, OnDialogListener listener) {
        this.listener = listener;

        tvTitle.setText(title);
        etNumber.getText().clear();
    }

}
