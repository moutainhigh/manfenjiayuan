package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.bean.Human;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.http.CommonUserAccountHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import net.tsz.afinal.http.AjaxParams;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;

/**
 * <p>
 * 对话框 -- 门店用户注册
 * <li>Step 1: 输入手机号，查询用户是否注册满分。</li>
 * <li>Step 2: 如果用户已经注册满分，则发送验证码给用户。（再次发送验证码需要等待60秒）</li>
 * <li>Step 3: 输入用户收到的验证码，然后点击‘验证’按钮验证是否正确。</li>
 * </p>
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class RegisterUserDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle;
    private ImageButton btnClose;
    private EditText etPhonenumber, etName, etLoginPass, etPayPass;
    private Button btnSubmit;

    private ProgressBar progressBar;

    private String phoneNumber;//手机号

    public interface OnRegisterListener{
        void onSuccess(Human human);
        void onCandel();
        void onFailed();
    }
    private OnRegisterListener mRegisterListener;

    //确认对话框
    private CommonDialog confirmDialog = null;

    private RegisterUserDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private RegisterUserDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_register_user, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        etPhonenumber = (EditText) rootView.findViewById(R.id.et_phoneNumber);
        etName = (EditText) rootView.findViewById(R.id.et_name);
        etLoginPass = (EditText) rootView.findViewById(R.id.et_login_password);
        etPayPass = (EditText) rootView.findViewById(R.id.et_pay_password);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);

        tvTitle.setText("注册");

        etName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()) {
                        DeviceUtils.showSoftInput(getContext(), etName);
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), etName);
                    }
                }
                etName.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        etLoginPass.requestFocus();
                    }
                    return true;
                }
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etLoginPass.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()) {
//                        DeviceUtils.showSoftInput(getContext(), etPhoneNumber);
                        showLoginPwdKeyboard();
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), etLoginPass);
                    }
                }
                etLoginPass.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etLoginPass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        etPayPass.requestFocus();
                        etPayPass.setSelection(etPayPass.length());
                    }
                    return true;
                }
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etPayPass.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()) {
//                        DeviceUtils.showSoftInput(getContext(), etPhoneNumber);
                        showPayPwdKeyboard();
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), etPayPass);
                    }
                }
                etPayPass.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
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
                if (mRegisterListener != null){
                    mRegisterListener.onCandel();
                }
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public RegisterUserDialog(Context context) {
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

        if (StringUtils.isEmpty(phoneNumber)) {
            DialogUtil.showHint("手机号无效或为空！");
            dismiss();
        }

        etName.requestFocus();
        etName.getText().clear();
        etLoginPass.getText().clear();
        etPayPass.getText().clear();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public void initialize(String phoneNumber, OnRegisterListener mRegisterListener) {
        this.phoneNumber = phoneNumber;
        this.mRegisterListener = mRegisterListener;

        this.etPhonenumber.setText(phoneNumber);
        this.etName.getText().clear();
        this.etLoginPass.getText().clear();
        this.etPayPass.getText().clear();
    }

    private NumberInputDialog barcodeInputDialog = null;


    /**
     * 显示条码输入界面
     * 相当于扫描条码
     */
    private void showLoginPwdKeyboard() {
        if (barcodeInputDialog == null) {
            barcodeInputDialog = new NumberInputDialog(getContext());
            barcodeInputDialog.setCancelable(true);
            barcodeInputDialog.setCanceledOnTouchOutside(true);
        }
        barcodeInputDialog.initializeBarcode(EditInputType.TEXT_PASSWORD, "登录密码", "登录密码", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        etLoginPass.setText(value);
                        etPayPass.requestFocus();
                        etPayPass.setSelection(etPayPass.length());
                    }

                    @Override
                    public void onNext(Double value) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
//        barcodeInputDialog.setMinimumDoubleCheck(0.01D, true);
        if (!barcodeInputDialog.isShowing()) {
            barcodeInputDialog.show();
        }
    }

    private void showPayPwdKeyboard() {
        if (barcodeInputDialog == null) {
            barcodeInputDialog = new NumberInputDialog(getContext());
            barcodeInputDialog.setCancelable(true);
            barcodeInputDialog.setCanceledOnTouchOutside(true);
        }
        barcodeInputDialog.initializeBarcode(EditInputType.TEXT_PASSWORD, "支付密码", "支付密码", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        etPayPass.setText(value);
                        etPayPass.setSelection(etPayPass.length());
                    }

                    @Override
                    public void onNext(Double value) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
//        barcodeInputDialog.setMinimumDoubleCheck(0.01D, true);
        if (!barcodeInputDialog.isShowing()) {
            barcodeInputDialog.show();
        }
    }

    /**
     * 提交
     */
    private void submit() {
        btnSubmit.setEnabled(false);

        //验证手机号和会员信息
        String name = this.etName.getText().toString();
        if (StringUtils.isEmpty(name)) {
            DialogUtil.showHint("请输入您的姓名");
            btnSubmit.setEnabled(true);
            etName.requestFocus();
            return;
        }
        String loginPassword = this.etLoginPass.getText().toString();
        if (StringUtils.isEmpty(loginPassword)) {
            DialogUtil.showHint("请输入您的登录密码");
            btnSubmit.setEnabled(true);
            etLoginPass.requestFocus();
            return;
        }
        String payPassword = this.etPayPass.getText().toString();
        if (StringUtils.isEmpty(payPassword)) {
            DialogUtil.showHint("请输入您的支付密码");
            btnSubmit.setEnabled(true);
            etPayPass.requestFocus();
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        registerUser(phoneNumber, name, loginPassword, payPassword);
    }

    private void registerUser(final String phoneNumber, final String humanName,
                              final String loginPassword, final String payPassword) {

        if (confirmDialog == null) {
            confirmDialog = new CommonDialog(getContext());
        }

        confirmDialog.setMessage("请确认是否注册？");
        confirmDialog.setPositiveButton("注册", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

//                DialogUtil.showHint("开卡");
                progressBar.setVisibility(View.VISIBLE);

                Map<String, String> options = new HashMap<>();
                options.put("humanMobile", phoneNumber);
                if (!StringUtils.isEmpty(humanName)) {
                    options.put("humanName", humanName);
                }
                options.put("password", loginPassword);
                options.put("payPassword", payPassword);
                options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

                CommonUserAccountHttpManager.getInstance().registerUser(options,
                        new Subscriber<Human>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                ZLogger.e(String.format("注册失败:%s", e.toString()));
                                btnSubmit.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onNext(Human human) {
                                btnSubmit.setEnabled(true);
                                if (human == null) {
                                    DialogUtil.showHint("注册失败");
                                    progressBar.setVisibility(View.GONE);

                                    if (mRegisterListener != null){
                                        mRegisterListener.onFailed();
                                    }
                                } else {
                                    DialogUtil.showHint(String.format("注册成功:\n姓名：%s\n手机号：%s",
                                            human.getName(), human.getMobile()));
//                        dismiss();
//                        DialogUtil.showHint("注册成功");
                                    //自动去绑定小区，如果失败，则提示。
                                    createParamDirect(human);
                                }
                            }

                        });
            }
        });
        confirmDialog.setNegativeButton("点错了", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                btnSubmit.setEnabled(true);
            }
        });
        confirmDialog.show();
    }

    private void createParamDirect(final Human human) {
        Map<String, String> options = new HashMap<>();
        AjaxParams params = new AjaxParams();
        JSONObject paramObject = new JSONObject();
        paramObject.put("humanId", human.getId());
        paramObject.put("paramName", "defaultNet");//固定不变
        paramObject.put("paramValue", MfhLoginService.get().getCurOfficeId());//当前门店登录账号的网点编号
        params.put("param", paramObject.toJSONString());
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        RxHttpManager.getInstance().createParamDirect(options,
                new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.e(String.format("服务网点绑定失败:%s", e.toString()));
                        btnSubmit.setEnabled(true);
                        progressBar.setVisibility(View.GONE);

                        if (mRegisterListener != null){
                            mRegisterListener.onSuccess(human);
                        }

                        dismiss();
                    }

                    @Override
                    public void onNext(String s) {
                        btnSubmit.setEnabled(true);
                        if (s == null) {
                            DialogUtil.showHint("服务网点绑定失败");
                            progressBar.setVisibility(View.GONE);
                        } else {
                            ZLogger.d(String.format("服务网点绑定成功:%s", s));
                        }

                        if (mRegisterListener != null){
                            mRegisterListener.onSuccess(human);
                        }

                        dismiss();
                    }


                });

    }
}
