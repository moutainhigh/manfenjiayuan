package com.mfh.enjoycity.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;


/**
 * 选择地址
 * 
 * @author NAT.ZZN
 * 
 */
public class PayDialog extends CommonDialog {

    public interface OnResponseCallback {
        void onWxpay();
        void onAlipay();
    }
    private OnResponseCallback mListener;
    public void setResponseCallback(OnResponseCallback mListener){
        this.mListener = mListener;
    }

    private View rootView;
    private ImageButton btnClose;
    private TextView tvAmount;
    private View llWxPay, llAliPay;
    private EditText[] etPwds;

    private PayDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private PayDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialog_pay, null);
//        ButterKnife.bind(rootView);

        btnClose = (ImageButton)rootView.findViewById(R.id.button_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tvAmount = (TextView) rootView.findViewById(R.id.tv_amount);
        llWxPay = rootView.findViewById(R.id.ll_wxpay);
        llWxPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                if (mListener != null) {
                    mListener.onWxpay();
                }
            }
        });
        llAliPay = rootView.findViewById(R.id.ll_alipay);
        llAliPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                if (mListener != null) {
                    mListener.onAlipay();
                }
            }
        });
        etPwds = new EditText[5];
        etPwds[0] = (EditText)rootView.findViewById(R.id.et_number_0);
        etPwds[1] = (EditText)rootView.findViewById(R.id.et_number_1);
        etPwds[2] = (EditText)rootView.findViewById(R.id.et_number_2);
        etPwds[3] = (EditText)rootView.findViewById(R.id.et_number_3);
        etPwds[4] = (EditText)rootView.findViewById(R.id.et_number_4);

        setContent(rootView, 0);
    }

    public PayDialog(Context context) {
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
//        p.width = d.getWidth() * 2 / 3;
        p.y = DensityUtil.dip2px(getContext(), 44);
        getWindow().setAttributes(p);
    }

    public String getPassword(){
        //TODO
        StringBuilder sb = new StringBuilder();
        for (EditText et : etPwds){
            String text = et.getText().toString();
            if (StringUtils.isEmpty(text)){
                text = "";
            }
            sb.append(text);
        }
        return sb.toString();
    }

}
