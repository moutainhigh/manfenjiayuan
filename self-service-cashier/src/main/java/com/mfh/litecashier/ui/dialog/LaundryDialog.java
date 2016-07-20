package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.framework.api.impl.CashierApiImpl;


/**
 * 对话框－－洗衣－－输入衣袋编号
 * 
 * @author NAT.ZZN
 * 
 */
public class LaundryDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle;
    private EditText etPocketNumber;
    private Button btnSubmit;
    private ImageButton btnClose;

    public interface DialogListener{
        void onNextStep(String text);
    }
    private DialogListener listener;


    private LaundryDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private LaundryDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_laundry, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        etPocketNumber = (EditText) rootView.findViewById(R.id.et_pocket_number);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);

        tvTitle.setText("洗衣");

        etPocketNumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPreferencesManager.isSoftKeyboardEnabled()){
                        DeviceUtils.showSoftInput(getContext(), etPocketNumber);
                    }
                    else{
                        DeviceUtils.hideSoftInput(getContext(), etPocketNumber);
                    }
                }
                etPocketNumber.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etPocketNumber.setOnKeyListener(new EditText.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etQuery):keyCode=%d, action=%d", keyCode, event.getAction()));
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    //按下回车键后会执行两次，
                    // 猜测一，输入框会自动捕获回车按键，自动切换焦点到下一个控件；
                    // 猜测二，通过打印日志观察发现，每次按下按键，都会监听到两次键盘事件，重复导致。
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        doSubmit();
                    }
                    return true;
                }


                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSubmit();
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public LaundryDialog(Context context) {
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

    public void init(DialogListener listener){
        this.listener = listener;
    }

    @Override
    public void dismiss() {
        super.dismiss();

        if (etPocketNumber != null){
            etPocketNumber.getText().clear();
        }
    }

    @Override
    public void show() {
        super.show();

        etPocketNumber.requestFocus();
        DeviceUtils.hideSoftInput(getOwnerActivity());
    }

    private void doSubmit(){
        final String queryText = etPocketNumber.getText().toString();
        if (StringUtils.isEmpty(queryText) || queryText.length() != 6){
            DialogUtil.showHint("请输入六位衣袋编号");
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        NetCallBack.NetTaskCallBack receiveResponseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                            {"code":"0","msg":"操作成功!","version":"1","data":""}
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String retStr = retValue.getValue();

                        //出库成功:1-556637
                        ZLogger.df("检查衣袋编号成功:" + retStr);

                        if (listener != null){
                            listener.onNextStep(queryText);
                        }

                        dismiss();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("检查衣袋编号失败：" + errMsg);
//                        DialogUtil.showHint("");
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.checkPackageCodeExist(queryText, receiveResponseCallback);
    }


}
