package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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
import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.account.UserAccount;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.http.CommonUserAccountHttpManager;
import com.mfh.framework.rxapi.http.SysHttpManager;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

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
public class InitCardByStepDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle;
    private ImageButton btnClose;
    private AvatarView ivHeader;
    private TextView tvUsername;
    private TextLabelView labelPhone;
    private EditLabelView labelCardNumber, labelCardId;
    private Button btnSubmit;

    private ProgressBar progressBar;

    private Human mHuman = null;//会员信息
    private CommonDialog confirmDialog = null;

    public interface OnInitCardListener{
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

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        ivHeader = (AvatarView) rootView.findViewById(R.id.iv_header);
        tvUsername = (TextView) rootView.findViewById(R.id.tv_username);
        labelPhone = (TextLabelView) rootView.findViewById(R.id.label_phone);
        labelCardNumber = (EditLabelView) rootView.findViewById(R.id.label_cardnumber);
        labelCardId = (EditLabelView) rootView.findViewById(R.id.label_cardid);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);

        tvTitle.setText("开卡");
        ivHeader.setBorderWidth(3);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));

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

    @Override
    public void dismiss() {
        super.dismiss();

    }

    public void initialize(String phonenumber, OnInitCardListener mOnInitCardListener) {
        this.mOnInitCardListener = mOnInitCardListener;
        saveHumanInfo(null);
        queryPhone(phonenumber);
        thirdStep();
    }

    public void initialize(Human human, OnInitCardListener mOnInitCardListener) {
        this.mOnInitCardListener = mOnInitCardListener;
        saveHumanInfo(human);
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


    /**
     * 第三步，输入卡面号
     */
    private void thirdStep() {
        labelCardNumber.clearInput();
        labelCardNumber.setEnabled(true);
        labelCardNumber.requestFocusEnd();
        labelCardId.clearInput();
        labelCardId.setEnabled(false);

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
        labelCardId.setEnabled(true);
        labelCardId.requestFocusEnd();

        btnSubmit.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    /**
     * 刷新会员信息
     */
    private void saveHumanInfo(Human human) {
        mHuman = human;
        if (human == null) {
            ivHeader.setAvatarUrl(null);
            tvUsername.setText("");
            labelPhone.setEndText("");
        }
        else{
            ivHeader.setAvatarUrl(human.getHeadimageUrl());
            tvUsername.setText(human.getName());
            labelPhone.setEndText(human.getMobile());
        }
    }


    /**
     * 查询会员信息，s
     */
    private void queryPhone(String phoneNumber) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Map<String, String> options = new HashMap<>();
        options.put("mobile", phoneNumber);
        SysHttpManager.getInstance().getHumanByIdentity(options,
                new Subscriber<Human>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(View.GONE);
                        ZLogger.e(String.format("查询会员信息失败:%s", e.toString()));
                        DialogUtil.showHint("查询会员信息失败，请退出重试");
                    }

                    @Override
                    public void onNext(Human human) {
                        progressBar.setVisibility(View.GONE);


                        if (human == null) {
                            DialogUtil.showHint("未查到会员信息，请退出重试");
                        } else {
                            saveHumanInfo(human);
                        }
                    }
                });
    }


    /**
     * 提交
     */
    private void submit() {
        btnSubmit.setEnabled(false);

        //验证手机号和会员信息
        if (mHuman == null) {
            btnSubmit.setEnabled(true);
            DialogUtil.showHint("用户信息加载失败，请退出重试");
            return;
        }

        String cardNumber = this.labelCardNumber.getInput();
        if (StringUtils.isEmpty(cardNumber)) {
            DialogUtil.showHint("请输入卡面号");
//            btnSubmit.setEnabled(true);
            thirdStep();
            return;
        }
        //卡面号10位数字：0512000748
        if (cardNumber.length() != 10) {
            DialogUtil.showHint("卡面号不正确,请重新输入");
            thirdStep();
            return;
        }


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

        doSubmitWork(cardNumber, cardId2);
    }

    private void doSubmitWork(final String shortNo, final String cardId){

        if (confirmDialog == null){
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
                options.put("ownerId", String.valueOf(mHuman.getId()));
                options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
                CommonUserAccountHttpManager.getInstance().activateAccount(options,
                        new Subscriber<UserAccount>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                ZLogger.e(String.format("开卡失败:%s", e.toString()));
                                btnSubmit.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onNext(UserAccount activateAccountResult) {
                                btnSubmit.setEnabled(true);
                                if (activateAccountResult == null) {
                                    DialogUtil.showHint("开卡失败");
                                    if (mOnInitCardListener != null){
                                        mOnInitCardListener.onFailed();
                                    }
                                } else {
                                    ZLogger.df(String.format("开卡成功:%d-%d",
                                            activateAccountResult.getId(), activateAccountResult.getOwnerId()));
                                    dismiss();
                                    if (mOnInitCardListener != null){
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
