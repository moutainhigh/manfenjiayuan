package com.manfenjiayuan.mixicook_vip.ui.order;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manfenjiayuan.business.bean.AccountPayResponse;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.commonuseraccount.CommonUserAccountApiImpl;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.gridpasswordview.GridPasswordView;


/**
 * 订单支付
 *
 * @author NAT.ZZN(bingshanguxue)
 */
public class ScOrderPaymentDialog extends CommonDialog {

    public interface OnResponseCallback {
        void onSuccess();
        void onCancel();
    }

    private OnResponseCallback mListener;
    private View rootView;
    private TextView tvTitle;
    private GridPasswordView mGridPasswordView;
//    private Button btnSubmit;
//    private Button btnNegative;
    private ProgressBar progressBar;

    private String orderIds;//订单编号

    private ScOrderPaymentDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ScOrderPaymentDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_scorder_payment, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        tvTitle.setText("管理员密码");
        mGridPasswordView = (GridPasswordView) rootView.findViewById(R.id.gridPasswordView);
        mGridPasswordView.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {

            }

            @Override
            public void onInputFinish(String psw) {
                submit();
            }
        });
        rootView.findViewById(R.id.key_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridPasswordView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_0));
            }
        });
        rootView.findViewById(R.id.key_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridPasswordView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_1));
            }
        });
        rootView.findViewById(R.id.key_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridPasswordView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_2));
            }
        });
        rootView.findViewById(R.id.key_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridPasswordView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_3));
            }
        });
        rootView.findViewById(R.id.key_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridPasswordView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_4));
            }
        });
        rootView.findViewById(R.id.key_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridPasswordView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_5));
            }
        });
        rootView.findViewById(R.id.key_6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridPasswordView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_6));
            }
        });
        rootView.findViewById(R.id.key_7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridPasswordView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_7));
            }
        });
        rootView.findViewById(R.id.key_8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridPasswordView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_8));
            }
        });
        rootView.findViewById(R.id.key_dot).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.key_9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridPasswordView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_9));
            }
        });
        rootView.findViewById(R.id.key_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridPasswordView.responseKeycodeDel();
//                mGridPasswordView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DEL));
            }
        });
        rootView.findViewById(R.id.key_del).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mGridPasswordView.clearPassword();
                return true;
            }
        });
        rootView.findViewById(R.id.button_header_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null){
                    mListener.onCancel();
                }
            }
        });
//        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
//        btnSubmit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                submit();
//
//            }
//        });
//        btnNegative = (Button) rootView.findViewById(R.id.button_footer_negative);
//        btnNegative.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mListener != null){
//                    mListener.onGuestSignIn();
//                }
//            }
//        });
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);

        setContent(rootView, 0);
    }

    public ScOrderPaymentDialog(Context context) {
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
//        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        return super.onKeyDown(keyCode, event);
//    }

    public void init(String title, String orderIds, OnResponseCallback callback) {
        this.mListener = callback;
        this.orderIds = orderIds;

        tvTitle.setText(title);
//        etPassword.setText(hintValue.toString());
//        etPassword.getText().clear();
//        etPassword.setSelection(etPassword.length());
//        etPassword.requestFocus();
        mGridPasswordView.clearPassword();

        progressBar.setVisibility(View.INVISIBLE);
//        btnSubmit.setEnabled(true);
    }

    private void submit() {
//        btnSubmit.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        String pwdStr = mGridPasswordView.getPassWord();//etPassword.getText().toString();
        if (StringUtils.isEmpty(pwdStr)) {
            progressBar.setVisibility(View.INVISIBLE);
//            btnSubmit.setEnabled(true);
            DialogUtil.showHint("请输入管理员密码");
            return;
        }

        if (pwdStr.length() < 6){
            progressBar.setVisibility(View.INVISIBLE);
//            btnSubmit.setEnabled(true);
            DialogUtil.showHint("密码长度不能小于6位");
            return;
        }

        if (!NetworkUtils.isConnect(getContext())) {
            progressBar.setVisibility(View.INVISIBLE);
//            btnSubmit.setEnabled(true);
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        CommonUserAccountApiImpl.scAccountPay(BizType.SC, orderIds,
                MfhLoginService.get().getCurrentGuId(),
                pwdStr, responseCallback);
    }

    NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<AccountPayResponse,
            NetProcessor.Processor<AccountPayResponse>>(
            new NetProcessor.Processor<AccountPayResponse>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("processFailure: " + errMsg);
                    progressBar.setVisibility(View.INVISIBLE);
//                    btnSubmit.setEnabled(true);
                    mGridPasswordView.clearPassword();
                    DialogUtil.showHint(errMsg);
                }

                @Override
                public void processResult(IResponseData rspData) {
                    progressBar.setVisibility(View.INVISIBLE);
//                    btnSubmit.setEnabled(true);
                    mGridPasswordView.clearPassword();

                    dismiss();

                    if (mListener != null) {
                        mListener.onSuccess();
                    }
                }
            }
            , AccountPayResponse.class
            , MfhApplication.getAppContext())
    {
    };

}
