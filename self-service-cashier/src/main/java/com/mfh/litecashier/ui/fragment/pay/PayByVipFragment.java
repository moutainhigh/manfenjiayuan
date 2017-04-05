package com.mfh.litecashier.ui.fragment.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.pay.BasePayFragment;
import com.bingshanguxue.cashier.pay.PayActionEvent;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import rx.Subscriber;

/**
 * 会员卡支付
 * Created by bingshanguxue on 15/8/31.
 */
public class PayByVipFragment extends BasePayFragment {
    @BindView(R.id.et_barcode)
    EditText etBarCode;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_vip;
    }

    @Override
    protected int getPayType() {
        return WayType.VIP;
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
//        EventBus.getDefault().post(new MfPayEvent(CashierConstants.PAYTYPE_MFCARD, MfPayEvent.EVENT_ID_QEQUEST_HANDLE_AMOUNT));
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
                        DeviceUtils.showSoftInput(CashierApp.getAppContext(), etBarCode);
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

    @Override
    protected void submitOrder() {
//        if (bPayProcessing) {
//            ZLogger.df("正在进行账户支付，不用重复发起请求");
//            return;
//        }

        onDeactiveMode();
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询用户信息", true);
        String codeA = etBarCode.getText().toString();
        if (StringUtils.isEmpty(codeA)) {
            validateFailed("参数无效");
            return;
        }
        ZLogger.d("codeA=" + codeA);

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            validateFailed(getString(R.string.toast_network_error));
            return;
        }

        Map<String, String> options = new HashMap<>();

        //长度为8(466CAF31) ，会员卡芯片号
        if (codeA.length() == 8) {
            final String cardNo = MUtils.parseCardId(codeA);
            if (StringUtils.isEmpty(cardNo)) {
                validateFailed("芯片号无效");
                return;
            }

            options.put("cardNo", cardNo);
            options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
            loadHuman(0, cardNo, options);
        }
        //长度为11(15250065084)，手机号
        else if (codeA.length() == 11) {
            options.put("mobile", codeA);
            options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
            loadHuman(2, null, options);
        }
        //长度为15(000000000712878)，微信付款码
        else if (codeA.length() == 15) {
            String humanId = MUtils.parseMfPaycode(codeA);
            if (StringUtils.isEmpty(humanId)) {
                validateFailed("付款码无效");
                return;
            }

            options.put("humanId", humanId);
            options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
            loadHuman(1, null, options);
        } else {
            validateFailed("参数无效");
        }
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
        ZLogger.df(String.format("会员验证成功,subPayType=%d, carId=%s, \nmemberInfo=%s",
                subPayType, cardId, JSON.toJSONString(memberInfo)));
        hideProgressDialog();
        Bundle args = new Bundle();
        args.putSerializable(PayActionEvent.KEY_MEMBERINFO, memberInfo);
        args.putInt(PayActionEvent.KEY_PAY_TYPE, payType);
        args.putInt(PayActionEvent.KEY_PAY_SUBTYPE, subPayType);
        if (!StringUtils.isEmpty(cardId)) {
            args.putString(PayActionEvent.KEY_CARD_ID, cardId);
        }
        EventBus.getDefault().post(new PayActionEvent(PayActionEvent.PAY_ACTION_VIP_DETECTED, args));

        etBarCode.getText().clear();
    }

    /**
     * 加载会员信息
     * */
    private void loadHuman(final int subPayType, final String cardId, Map<String, String> options){
        RxHttpManager.getInstance().getCustomerByOther(options,
                new Subscriber<Human>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        validateFailed(e.toString());

                    }

                    @Override
                    public void onNext(Human human) {
                        if (human == null){
                            validateFailed("未查询到结果");
                        }
                        else {
                            validateSuccess(subPayType, cardId, human);
                        }
                    }
                });
    }


}
