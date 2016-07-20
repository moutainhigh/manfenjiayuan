package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.widget.AvatarView;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.framework.api.impl.CashierApiImpl;
import com.mfh.litecashier.bean.ActivateAccountResult;
import com.mfh.litecashier.bean.Human;
import com.mfh.framework.uikit.utils.DecimalInputFilter;


/**
 * 对话框 -- 充值
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class TopupDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle;
    private AvatarView ivHeaderTo;
    private TextView tvUsernameTo;
    private EditText etPhoneNumber, etAmount, etPassword;
    private Button btnSubmit;
    private ImageButton btnClose;
    private ProgressBar progressBar;

    private Human humanTo = null;

    private TopupDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private TopupDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_topup, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        ivHeaderTo = (AvatarView) rootView.findViewById(R.id.iv_header_to);
        tvUsernameTo = (TextView) rootView.findViewById(R.id.tv_username_to);
        etPhoneNumber = (EditText) rootView.findViewById(R.id.et_phoneNumber);
        etAmount = (EditText) rootView.findViewById(R.id.et_amount);
        etPassword = (EditText) rootView.findViewById(R.id.et_password);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);

        tvTitle.setText("充值");
        ivHeaderTo.setBorderWidth(3);
        ivHeaderTo.setBorderColor(Color.parseColor("#e8e8e8"));
        etPhoneNumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(getContext(), etPhoneNumber);
                }
                etPhoneNumber.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etPhoneNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        queryPhone();
                    }
                    return true;
                }
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etAmount.setFilters(new InputFilter[]{new DecimalInputFilter(2)});
        etAmount.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(getContext(), etAmount);
                }
                etAmount.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(getContext(), etPassword);
                }
                etPassword.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

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
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
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

    public TopupDialog(Context context) {
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
        p.height = d.getHeight();
////        p.width = d.getWidth() * 2 / 3;
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

        DeviceUtils.hideSoftInput(getOwnerActivity());
    }

    public void init(){
        etAmount.getText().clear();
        etPassword.getText().clear();
        refreshHumanInfo(null);
    }

    /**
     * 刷新会员信息
     */
    private void refreshHumanInfo(Human human){
        if (human != null){
            ivHeaderTo.setAvatarUrl(human.getHeadimageUrl());
            tvUsernameTo.setText(human.getName());
            etAmount.requestFocus();

            btnSubmit.setEnabled(true);
        }
        else{
            ivHeaderTo.setAvatarUrl("");
            tvUsernameTo.setText("");
            etPhoneNumber.getText().clear();
            etPhoneNumber.requestFocus();
            etAmount.getText().clear();
            etPassword.getText().clear();

            btnSubmit.setEnabled(false);
        }

        this.humanTo = human;
    }

    /**
     * 查询会员
     * */
    private void queryPhone(){
        String phoneNumber = etPhoneNumber.getText().toString();
        if (StringUtils.isEmpty(phoneNumber)){
            DialogUtil.showHint("请输入手机号");
            return;
        }

        if (phoneNumber.length() != 11) {
            DialogUtil.showHint("手机号格式不正确");
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        CashierApiImpl.findHumanByPhone(phoneNumber, findMemberResponseCallback);
    }
    //查询会员信息
    private NetCallBack.NetTaskCallBack findMemberResponseCallback = new NetCallBack.NetTaskCallBack<Human,
            NetProcessor.Processor<Human>>(
            new NetProcessor.Processor<Human>() {
                @Override
                public void processResult(final IResponseData rspData) {
                    if (rspData == null) {
                        DialogUtil.showHint("未查到会员信息");
                        refreshHumanInfo(null);
                        return;
                    }

                    RspBean<Human> retValue = (RspBean<Human>) rspData;
                    refreshHumanInfo(retValue.getValue());
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.e(String.format("未查到会员信息:%s", errMsg));
                    DialogUtil.showHint("未查到会员信息");
                    refreshHumanInfo(null);
                }
            }
            , Human.class
            , CashierApp.getAppContext()) {
    };


    private void submit() {
        btnSubmit.setEnabled(false);

        if (humanTo == null){
            DialogUtil.showHint("请输入会员信息");
            btnSubmit.setEnabled(true);
            return;
        }

        String amount = etAmount.getText().toString();
        if (StringUtils.isEmpty(amount)) {
            DialogUtil.showHint("请输入充值金额");
            btnSubmit.setEnabled(true);
            return;
        }

        String password = etPassword.getText().toString();
        if (StringUtils.isEmpty(password)) {
            DialogUtil.showHint("请输入密码");
            btnSubmit.setEnabled(true);
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        CashierApiImpl.transferFromMyAccount(amount, password, humanTo.getGuid(), submitCallback);
    }

    //充值
    private NetCallBack.NetTaskCallBack submitCallback = new NetCallBack.NetTaskCallBack<ActivateAccountResult,
            NetProcessor.Processor<ActivateAccountResult>>(
            new NetProcessor.Processor<ActivateAccountResult>() {
                @Override
                public void processResult(final IResponseData rspData) {
                    ActivateAccountResult result = null;
                    if (rspData != null){
                        RspBean<ActivateAccountResult> retValue = (RspBean<ActivateAccountResult>) rspData;
                        result = retValue.getValue();
                    }

                    if (result == null){
                        DialogUtil.showHint("充值失败");
                        refreshHumanInfo(null);
                        btnSubmit.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                    else{
                        ZLogger.d(String.format("充值成功:%d-%d", result.getId(), result.getOwnerId()));
                        dismiss();
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //{"code":"1","msg":"1181527857该卡已被他人使用，请重新确认！","version":"1","data":null}
                    ZLogger.e(String.format("充值失败:%s", errMsg));
//                    DialogUtil.showHint("未查到会员信息");
//                    refreshHumanInfo(null);
                    btnSubmit.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                }
            }
            , ActivateAccountResult.class
            , CashierApp.getAppContext()) {
    };

}
