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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.manfenjiayuan.business.presenter.CustomerPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.ICustomerView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.account.UserAccount;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.http.CommonUserAccountHttpManager;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.widget.CustomerView;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;

/**
 * <p>
 * 对话框 -- 开卡
 * <li>Step 1: 输入手机号，查询用户是否注册满分。</li>
 * <li>Step 2: 如果用户已经注册满分，则发送验证码给用户。（再次发送验证码需要等待60秒）</li>
 * <li>Step 3: 输入用户收到的验证码，然后点击‘验证’按钮验证是否正确。</li>
 * </p>
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InitCardByStepDialog extends CommonDialog implements ICustomerView {

    private View rootView;
    private TextView tvTitle;
    private ImageButton btnClose;
    private CustomerView mCustomerView;
    private EditLabelView labelCardNumber, labelCardId;
    private Button btnSubmit;

    private ProgressBar progressBar;

    private CommonDialog confirmDialog = null;
    private CustomerInputDialog scoreDialog = null;
    private CustomerPresenter mCustomerPresenter;
    private String phoneNumber;

    @Override
    public void onICustomerViewLoading() {

    }

    @Override
    public void onICustomerViewError(int type, String content, String errorMsg) {
        DialogUtil.showHint(errorMsg);
        mCustomerView.reload(null);
        phoneNumber = null;

//        if (type == 2) {
//            continueOrNot(content);
//        } else {
//            retryOrNot();
//        }
    }

    private void continueOrNot(final String mobile) {
        if (confirmDialog == null) {
            confirmDialog = new CommonDialog(getContext());
        }

        confirmDialog.setMessage(String.format("%s 未注册", mobile));
        confirmDialog.setPositiveButton("继续开卡", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                phoneNumber = mobile;
                mCustomerView.setTvPhone(mobile);
            }
        });
        confirmDialog.setNegativeButton("重试", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                queryCustomer();
            }
        });
        confirmDialog.show();
    }

    private void retryOrNot() {
        if (confirmDialog == null) {
            confirmDialog = new CommonDialog(getContext());
        }

        confirmDialog.setMessage("未查询到会员信息");
        confirmDialog.setPositiveButton("重试", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                queryCustomer();
            }
        });
        confirmDialog.setNegativeButton("退出", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                dismiss();
            }
        });
        confirmDialog.show();
    }

    @Override
    public void onICustomerViewSuccess(int type, String content, Human human) {
        mCustomerView.reload(human);

        if (human == null) {
            phoneNumber = null;
            if (type == 2) {
                continueOrNot(content);
            } else {
                retryOrNot();
            }
        } else {
            phoneNumber = human.getMobile();
        }

    }


    public interface OnInitCardListener {
        void onSuccess();

        void onFailed();
    }

    private OnInitCardListener mOnInitCardListener;

    private InitCardByStepDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private InitCardByStepDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_initcard_bystep, null);
//        ButterKnife.bind(rootView);

        mCustomerPresenter = new CustomerPresenter(this);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        mCustomerView = (CustomerView) rootView.findViewById(R.id.customer_view);
        labelCardNumber = (EditLabelView) rootView.findViewById(R.id.label_cardnumber);
        labelCardId = (EditLabelView) rootView.findViewById(R.id.label_cardid);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);

        tvTitle.setText("开卡");

        mCustomerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryCustomer();
            }
        });

        labelCardNumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()) {
//                        DeviceUtils.showSoftInput(getContext(), etPhoneNumber);
                        showCardNumberKeyboard();
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), labelCardNumber);
                    }
                }
                labelCardNumber.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        labelCardNumber.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER},
                new EditLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            fourthStep();
                        }
                    }
                });
        labelCardId.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()) {
//                        DeviceUtils.showSoftInput(getContext(), etPhoneNumber);
                        showCardIdKeyboard();
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), labelCardId);
                    }
                }
                labelCardId.requestFocusEnd();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        labelCardId.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER},
                new EditLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            submit();
                        }
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

    public InitCardByStepDialog(Context context) {
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
        p.width = DensityUtil.dip2px(getContext(), 895);
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
            queryCustomer();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();

    }

//    public void initialize(String phonenumber, OnInitCardListener mOnInitCardListener) {
//        this.mOnInitCardListener = mOnInitCardListener;
//        saveHumanInfo(null);
////        queryPhone(phonenumber);
//        thirdStep();
//    }

    public void initialize(Human human, OnInitCardListener mOnInitCardListener) {
        this.mOnInitCardListener = mOnInitCardListener;
        mCustomerView.reload(human);
        phoneNumber = human != null ? human.getMobile() : null;

        thirdStep();
    }

    private NumberInputDialog barcodeInputDialog = null;


    private void showCardNumberKeyboard() {
        if (barcodeInputDialog == null) {
            barcodeInputDialog = new NumberInputDialog(getContext());
            barcodeInputDialog.setCancelable(true);
            barcodeInputDialog.setCanceledOnTouchOutside(true);
        }
        barcodeInputDialog.initializeBarcode(EditInputType.TEXT, "卡面号", "卡面号", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        labelCardNumber.setInput(value);
                        fourthStep();
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

    private void showCardIdKeyboard() {
        if (barcodeInputDialog == null) {
            barcodeInputDialog = new NumberInputDialog(getContext());
            barcodeInputDialog.setCancelable(true);
            barcodeInputDialog.setCanceledOnTouchOutside(true);
        }
        barcodeInputDialog.initializeBarcode(EditInputType.TEXT_PASSWORD, "芯片号", "芯片号", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        labelCardId.setInput(value);
                        submit();
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


//    /**
//     * 第三步，输入手机后
//     */
//    private void secondStep() {
//        labelCardNumber.clearInput();
//        labelCardNumber.setInputEnabled(true);
//        labelCardNumber.requestFocusEnd();
//        labelCardId.clearInput();
//        labelCardId.setInputEnabled(false);
//
//        btnSubmit.setEnabled(true);
//        progressBar.setVisibility(View.GONE);
//    }

    /**
     * 第三步，输入卡面号
     */
    private void thirdStep() {
        labelCardNumber.clearInput();
        labelCardNumber.setInputEnabled(true);
        labelCardNumber.requestFocusEnd();
        labelCardId.clearInput();
        labelCardId.setInputEnabled(false);

        btnSubmit.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    /**
     * 第四步，扫描卡芯片号
     */
    private void fourthStep() {
//        labelCardNumber.getText().clear();
//        labelCardNumber.setEnabled(true);
        labelCardId.clearInput();
        labelCardId.setInputEnabled(true);
        labelCardId.requestFocusEnd();

        btnSubmit.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    public void queryCustomer() {
        if (scoreDialog == null) {
            scoreDialog = new CustomerInputDialog(getContext());
            scoreDialog.setCancelable(false);
            scoreDialog.setCanceledOnTouchOutside(false);
        }

        scoreDialog.initializeBarcode(EditInputType.TEXT, "搜索会员", "会员帐号", "确定",
                new CustomerInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        mCustomerPresenter.getCustomerByOther(value);
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
        if (!scoreDialog.isShowing()) {
            scoreDialog.show();
        }
    }

    /**
     * 提交
     */
    private void submit() {
        btnSubmit.setEnabled(false);

        if (StringUtils.isEmpty(phoneNumber)) {
            queryCustomer();
            return;
        }

        String cardNumber = this.labelCardNumber.getInput();
        if (StringUtils.isEmpty(cardNumber)) {
            DialogUtil.showHint("请输入卡面号");
//            btnSubmit.setEnabled(true);
            thirdStep();
            return;
        }
//        //卡面号10位数字：0512000748
//        if (cardNumber.length() != 10) {
//            DialogUtil.showHint("卡面号不正确,请重新输入");
//            thirdStep();
//            return;
//        }


        String cardId = this.labelCardId.getInput();
        String cardId2 = MUtils.parseCardId(cardId);
        if (StringUtils.isEmpty(cardId2)) {
            DialogUtil.showHint("请重新刷卡获取芯片号");
            fourthStep();
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        doSubmitWork(phoneNumber, cardNumber, cardId2);
    }

    private void doSubmitWork(final String phoneNumber, final String shortNo, final String cardId) {

        if (confirmDialog == null) {
            confirmDialog = new CommonDialog(getContext());
        }

        confirmDialog.setMessage(String.format("请确认是否开卡？\n卡面号：%s", shortNo));
        confirmDialog.setPositiveButton("开卡", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                progressBar.setVisibility(View.VISIBLE);

                Map<String, String> options = new HashMap<>();
                options.put("shortNo", shortNo);
                options.put("cardId", cardId);
                options.put("mobile", phoneNumber);
//                options.put("ownerId", String.valueOf(mHuman.getId()));
                options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
                CommonUserAccountHttpManager.getInstance().activateAccount(options,
                        new Subscriber<UserAccount>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                ZLogger.ef(String.format("开卡失败:%s", e.toString()));
                                btnSubmit.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onNext(UserAccount activateAccountResult) {
                                btnSubmit.setEnabled(true);
                                if (activateAccountResult == null) {
                                    DialogUtil.showHint("开卡失败");
                                    if (mOnInitCardListener != null) {
                                        mOnInitCardListener.onFailed();
                                    }
                                } else {
                                    ZLogger.d(String.format("开卡成功:%d-%d",
                                            activateAccountResult.getId(), activateAccountResult.getOwnerId()));
                                    dismiss();
                                    if (mOnInitCardListener != null) {
                                        mOnInitCardListener.onSuccess();
                                    }
                                }
                            }

                        });

            }
        });
        confirmDialog.setNegativeButton("点错了", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                btnSubmit.setEnabled(true);
            }
        });
        confirmDialog.show();
    }
}
