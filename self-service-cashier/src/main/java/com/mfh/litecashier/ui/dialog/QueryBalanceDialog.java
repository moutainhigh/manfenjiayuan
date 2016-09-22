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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.framework.api.account.CommonUserAccountApi;
import com.mfh.framework.api.account.UserAccount;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;

import java.io.ByteArrayOutputStream;

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
public class QueryBalanceDialog extends CommonDialog {

    private View rootView;
    private ImageButton btnClose;
    private EditText etCardNo;
    private TextView tvTitle, tvCash, tvScore;
//    private AvatarView ivHeader;
    private Button btnSubmit;

    private ProgressBar progressBar;

    private Long userTmpId = null;

    public interface OnValidateListener{
        void onSuccess(String phonenumber);
        void onError();
    }
    private OnValidateListener mOnValidateListener;

    private QueryBalanceDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private QueryBalanceDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_query_balance, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
//        ivHeader = (AvatarView) rootView.findViewById(R.id.iv_header);
        etCardNo = (EditText) rootView.findViewById(R.id.et_card_id);
//        tvUsername = (TextView) rootView.findViewById(R.id.tv_username);
        tvCash = (TextView) rootView.findViewById(R.id.tv_cash);
        tvScore = (TextView) rootView.findViewById(R.id.tv_score);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);

        tvTitle.setText("余额查询");
//        ivHeader.setBorderWidth(3);
//        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));
        etCardNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        loadUserAccount(etCardNo.getText().toString());
                    }
                    return true;
                }
//                return true;
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnSubmit.setText("确定");
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setContent(rootView, 0);
    }

    public QueryBalanceDialog(Context context) {
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
        etCardNo.getText().clear();
        etCardNo.requestFocus();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        etCardNo.getText().clear();
    }

    public void initialize(OnValidateListener onValidateListener) {
        this.mOnValidateListener = onValidateListener;
        etCardNo.getText().clear();
        etCardNo.requestFocus();
    }


    private void refresh(UserAccount userAccount){
        if (userAccount != null){
            tvCash.setText(MUtils.formatDouble("余额：", "", userAccount.getCur_cash(), "", null, null));
            tvScore.setText(String.format("积分：%d", userAccount.getCur_score()));
        }
        else{
            tvCash.setText("余额：暂无数据");
            tvScore.setText("积分：暂无数据");
        }

        etCardNo.getText().clear();
        etCardNo.requestFocus();
    }

    private void loadUserAccount(String cardId){
        refresh(null);

        final String cardId2 = parseCardId(cardId);
        if (StringUtils.isEmpty(cardId2)) {
            DialogUtil.showHint("芯片号无效");
            return;
        }

        if (!NetworkUtils.isConnect(getContext())){
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        CommonUserAccountApi.getUserAccountByCardId(cardId2, responseCallback);
    }

    /**
     * 解析卡芯片号，十六进制转换为十进制
     * 十六进制：466CAF31 (8位)
     * 十进制：1181527857 (10位)
     */
    private String parseCardId(String rawData) {
        if (StringUtils.isEmpty(rawData)) {
            return null;
        }
        try {
            return String.valueOf(Long.parseLong(rawData, 16));
        } catch (Exception e) {
            ZLogger.e(String.format("parseCardId failed, %s", e.toString()));
            return null;
        }
    }

    //回调
    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<UserAccount,
            NetProcessor.Processor<UserAccount>>(
            new NetProcessor.Processor<UserAccount>() {
                @Override
                public void processResult(IResponseData rspData) {
                    try {
                        RspBean<UserAccount> retValue = (RspBean<UserAccount>) rspData;
                        refresh(retValue.getValue());
                    } catch (Exception ex) {
                        ZLogger.e("parseUserProfile, " + ex.toString());
                    } finally {
//                        loadingImageView.toggle(false);
                        progressBar.setVisibility(View.GONE);
//                        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
//                    loadingImageView.toggle(false);
                    progressBar.setVisibility(View.GONE);
                    DialogUtil.showHint(errMsg);
//                    emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
                }


            }
            , UserAccount.class
            , MfhApplication.getAppContext()) {
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
