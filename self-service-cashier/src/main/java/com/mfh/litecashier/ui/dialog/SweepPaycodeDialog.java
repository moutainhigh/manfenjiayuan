package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;


/**
 * <h1>扫描支付密码对话框</h1><br>
 *
 * @author bingshanguxue
 * 
 */
public class SweepPaycodeDialog extends CommonDialog {

    private TextView tvTitle, tvSubTitle;
    private EditText etCardNo;

    public interface DialogViewListener {
        void onCardDetected(String cardNo);
        void onCancel();
    }
    private DialogViewListener mListener;

    private View rootView;


    private SweepPaycodeDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SweepPaycodeDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_sweep_paycode, null);
//        ButterKnife.bind(rootView);

        etCardNo = (EditText) rootView.findViewById(R.id.et_pay_code);
        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        tvSubTitle = (TextView) rootView.findViewById(R.id.tv_subtitle);

        etCardNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        String cardNo = etCardNo.getText().toString();
                        if (!StringUtils.isEmpty(cardNo)){
                            if (mListener != null){
                                mListener.onCardDetected(cardNo);
                            }
                            dismiss();
                        }
                    }
                    return true;
                }
//                return true;
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });


        setContent(rootView, 0);
    }

    public SweepPaycodeDialog(Context context) {
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
////        p.y = DensityUtil.dip2px(getContext(), 44);
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
        etCardNo.getText().clear();
        etCardNo.requestFocus();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        etCardNo.getText().clear();
    }

    public void init(String title, String subTitle, DialogViewListener callback) {
        this.mListener = callback;
        tvTitle.setText(title);
        if (!StringUtils.isEmpty(subTitle)){
            tvSubTitle.setText(subTitle);
            tvSubTitle.setVisibility(View.VISIBLE);
        }
        else{
            tvSubTitle.setVisibility(View.GONE);
        }
        etCardNo.getText().clear();
        etCardNo.requestFocus();
    }
}
