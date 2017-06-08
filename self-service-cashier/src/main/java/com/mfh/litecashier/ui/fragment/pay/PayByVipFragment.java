package com.mfh.litecashier.ui.fragment.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.pay.BasePayFragment;
import com.bingshanguxue.cashier.pay.PayActionEvent;
import com.bingshanguxue.cashier.pay.PayStep1Event;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.manfenjiayuan.business.presenter.CustomerPresenter;
import com.manfenjiayuan.business.view.ICustomerView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * 会员卡支付
 * Created by bingshanguxue on 15/8/31.
 */
public class PayByVipFragment extends BasePayFragment implements ICustomerView {
    @BindView(R.id.et_barcode)
    EditText etBarCode;

    private CustomerPresenter mCustomerPresenter;
    private NumberInputDialog customerDialog = null;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_vip;
    }

    @Override
    protected int getPayType() {
        return WayType.VIP;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCustomerPresenter = new CustomerPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);

        initBarCodeInput();

        onActiveMode();
    }

    @Override
    public void onResume() {
        super.onResume();
//        etBarCode.requestFocus();

        //TODO,主动去请求当前价格
        EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_WAYTYPE_UPDATED, null));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActiveMode() {
        super.onActiveMode();
        etBarCode.setEnabled(true);
        etBarCode.getText().clear();
        etBarCode.requestFocus();
    }

    @Override
    public void onDeactiveMode() {
        super.onDeactiveMode();
        etBarCode.setEnabled(false);
    }

    /**
     * 注册监听器
     */
    @Override
    protected void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BA_HANDLE_AMOUNT_CHANGED_VIP);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ZLogger.d("onReceive.action=" + intent.getAction());
                if (intent.getAction().equals(Constants.BA_HANDLE_AMOUNT_CHANGED_VIP)) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
//                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT, 0);
                        etBarCode.getText().clear();
                        etBarCode.requestFocus();

                        calculateCharge();
                    }
                }
            }
        };
        //使用LocalBroadcastManager.getInstance(getActivity())反而不行，接收不到。
        getActivity().registerReceiver(receiver, intentFilter);
    }

    private void initBarCodeInput() {
//        etBarCode.setHint("请将焦点定位到输入框并刷卡");
        etBarCode.setFocusable(true);
        etBarCode.setFocusableInTouchMode(true);//不自动获取EditText的焦点
//        etBarCode.setCursorVisible(false);//隐藏光标
        etBarCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()) {
                        queryCustomer();
//                        DeviceUtils.showSoftInput(CashierApp.getAppContext(), etBarCode);
                    } else {
                        DeviceUtils.hideSoftInput(CashierApp.getAppContext(), etBarCode);
                    }
                }
                etBarCode.requestFocus();
                etBarCode.setSelection(etBarCode.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etBarCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        submitOrder();
                    }
                    return true;
                }
//                return true;
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }

    /**
     * 加载会员信息
     */
    @Override
    protected void submitOrder() {
        submitOrder(etBarCode.getText().toString());
    }

    protected void submitOrder(String code) {
        onDeactiveMode();
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询用户信息", true);

        if (StringUtils.isEmpty(code)) {
            validateFailed("参数无效");
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            validateFailed(getString(R.string.toast_network_error));
            return;
        }

        mCustomerPresenter.getCustomerByOther(code);
    }

    /**
     * 验证失败
     */
    private void validateFailed(String msg) {
        hideProgressDialog();
        if (!StringUtils.isEmpty(msg)) {
            ZLogger.df(msg);
            DialogUtil.showHint(msg);
        }

        onActiveMode();
    }

    /**
     * 验证成功
     */
    private void validateSuccess(int subPayType, String cardId, Human memberInfo) {
        ZLogger.d(String.format("会员验证成功,subPayType=%d, carId=%s, \nmemberInfo=%s",
                subPayType, cardId, JSON.toJSONString(memberInfo)));
        hideProgressDialog();
        Bundle args = new Bundle();
        args.putSerializable(PayActionEvent.KEY_MEMBERINFO, memberInfo);
        args.putInt(PayActionEvent.KEY_PAY_TYPE, payType);
        args.putInt(PayActionEvent.KEY_PAY_SUBTYPE, subPayType);
        if (subPayType == 0 && !StringUtils.isEmpty(cardId)) {
            args.putString(PayActionEvent.KEY_CARD_ID, cardId);
        }
        EventBus.getDefault().post(new PayActionEvent(PayActionEvent.PAY_ACTION_VIP_DETECTED, args));

        etBarCode.getText().clear();
    }

    public void queryCustomer() {
        if (customerDialog == null) {
            customerDialog = new NumberInputDialog(getActivity());
            customerDialog.setCancelable(false);
            customerDialog.setCanceledOnTouchOutside(false);
        }

        customerDialog.initializeBarcode(EditInputType.TEXT, "搜索会员", "会员帐号", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        submitOrder(value);
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
        if (!customerDialog.isShowing()) {
            customerDialog.show();
        }
    }

    @Override
    public void onICustomerViewLoading() {
    }

    @Override
    public void onICustomerViewError(int type, String content, String errorMsg) {
        validateFailed(errorMsg);
    }

    @Override
    public void onICustomerViewSuccess(int type, String content, Human human) {
        if (human == null) {
            validateFailed("未查询到会员信息");
        } else {
            validateSuccess(type, content, human);
        }
    }


}
