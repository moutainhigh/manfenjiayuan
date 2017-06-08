package com.mfh.litecashier.ui.fragment.topup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.manfenjiayuan.business.presenter.CustomerPresenter;
import com.manfenjiayuan.business.view.ICustomerView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * 会员卡支付
 * Created by bingshanguxue on 15/8/31.
 */
public class CustomerTopupFragment extends BaseFragment implements ICustomerView {
    @BindView(R.id.et_barcode)
    EditText etBarCode;

    private CustomerPresenter mCustomerPresenter;
    private NumberInputDialog customerDialog = null;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_vip;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCustomerPresenter = new CustomerPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initBarCodeInput();

        onActiveMode();
    }

    @Override
    public void onResume() {
        super.onResume();
//        etBarCode.requestFocus();

        //TODO,主动去请求当前价格
//        EventBus.getDefault().post(new MfPayEvent(CashierConstants.PAYTYPE_MFCARD, MfPayEvent.EVENT_ID_QEQUEST_HANDLE_AMOUNT));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onActiveMode() {
        etBarCode.setEnabled(true);
        etBarCode.getText().clear();
        etBarCode.requestFocus();
    }

    public void onDeactiveMode() {
        etBarCode.setEnabled(false);
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
                        submitOrder(etBarCode.getText().toString());
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

    /**
     * 加载会员信息
     */
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
            ZLogger.d(String.format("会员验证成功,\nmemberInfo=%s",
                    JSONObject.toJSONString(human)));
            onActiveMode();
            hideProgressDialog();
            Bundle args = new Bundle();
            args.putInt(TopupActionEvent.KEY_TOPUP_TYPE, 1);
            args.putLong(TopupActionEvent.KEY_PAY_HUMANID, human.getId());
            EventBus.getDefault().post(new TopupActionEvent(TopupActionEvent.TOPUP_CUSTOMER, args));
        }
    }


}
