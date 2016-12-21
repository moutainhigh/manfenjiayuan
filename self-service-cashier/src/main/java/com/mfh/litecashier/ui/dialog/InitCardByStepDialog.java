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
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.account.UserApiImpl;
import com.mfh.framework.api.commonuseraccount.CommonUserAccountApiImpl;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.ActivateAccountResult;

import java.io.ByteArrayOutputStream;

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
        UserApiImpl.findHumanByPhone(phoneNumber, findMemberResponseCallback);
    }

    //查询会员信息
    private NetCallBack.NetTaskCallBack findMemberResponseCallback = new NetCallBack.NetTaskCallBack<Human,
            NetProcessor.Processor<Human>>(
            new NetProcessor.Processor<Human>() {
                @Override
                public void processResult(final IResponseData rspData) {
                    progressBar.setVisibility(View.GONE);
                    if (rspData == null) {
                        DialogUtil.showHint("未查到会员信息，请退出重试");
                    } else {
                        RspBean<Human> retValue = (RspBean<Human>) rspData;
                        saveHumanInfo(retValue.getValue());
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    progressBar.setVisibility(View.GONE);
                    ZLogger.e(String.format("查询会员信息失败:%s", errMsg));
                    DialogUtil.showHint("查询会员信息失败，请退出重试");
                }
            }
            , Human.class
            , CashierApp.getAppContext()) {
    };


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

//                DialogUtil.showHint("开卡");
                progressBar.setVisibility(View.VISIBLE);
                CommonUserAccountApiImpl.activateAccount(shortNo, cardId, mHuman.getId(), submitCallback);
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

    //开卡
    private NetCallBack.NetTaskCallBack submitCallback = new NetCallBack.NetTaskCallBack<ActivateAccountResult,
            NetProcessor.Processor<ActivateAccountResult>>(
            new NetProcessor.Processor<ActivateAccountResult>() {
                @Override
                public void processResult(final IResponseData rspData) {
                    ActivateAccountResult result = null;
                    if (rspData != null) {
                        RspBean<ActivateAccountResult> retValue = (RspBean<ActivateAccountResult>) rspData;
                        result = retValue.getValue();
                    }

                    btnSubmit.setEnabled(true);
                    if (result == null) {
                        DialogUtil.showHint("开卡失败");
                        if (mOnInitCardListener != null){
                            mOnInitCardListener.onFailed();
                        }
                    } else {
                        ZLogger.df(String.format("开卡成功:%d-%d", result.getId(), result.getOwnerId()));
                        dismiss();
                        if (mOnInitCardListener != null){
                            mOnInitCardListener.onSuccess();
                        }
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //{"code":"1","msg":"1181527857该卡已被他人使用，请重新确认！","version":"1","data":null}
                    ZLogger.e(String.format("开卡失败:%s", errMsg));
//                    DialogUtil.showHint("未查到会员信息");
//                    refreshHumanInfo(null);
                    btnSubmit.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                }
            }
            , ActivateAccountResult.class
            , CashierApp.getAppContext()) {
    };


    //16进制字符串转换为String
    private String hexString = "0123456789ABCDEF";

    public String decode(String bytes) {
        if (bytes.length() != 30) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                    .indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

    // 字符序列转换为16进制字符串
    private static String bytesToHexString(byte[] src, boolean isPrefix) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isPrefix) {
            stringBuilder.append("0x");
        }
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (byte aSrc : src) {
            buffer[0] = Character.toUpperCase(Character.forDigit(
                    (aSrc >>> 4) & 0x0F, 16));
            buffer[1] = Character.toUpperCase(Character.forDigit(aSrc & 0x0F,
                    16));
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }
}
