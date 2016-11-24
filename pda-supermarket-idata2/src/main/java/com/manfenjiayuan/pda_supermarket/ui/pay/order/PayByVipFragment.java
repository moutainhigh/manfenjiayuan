package com.manfenjiayuan.pda_supermarket.ui.pay.order;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.pay.PayActionEvent;
import com.manfenjiayuan.pda_supermarket.ui.pay.PayEvent;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.account.UserApiImpl;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import butterknife.BindView;
import de.greenrobot.event.EventBus;


/**
 * 会员卡支付
 * Created by bingshanguxue on 15/8/31.
 */
public class PayByVipFragment extends BasePayFragment {
    @BindView(R.id.et_barCode)
    EditLabelView etBarCode;

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
        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);

        initBarCodeInput();

        onActiveMode();
    }

    @Override
    protected void onScanCode(String code) {
        if (!isAcceptBarcodeEnabled) {
            return;
        }
        isAcceptBarcodeEnabled = false;
        etBarCode.setInput(code);
        etBarCode.requestFocusEnd();
        if (!StringUtils.isEmpty(code)) {
            submitOrder();
        } else {
            isAcceptBarcodeEnabled = true;
        }
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
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActiveMode() {
        super.onActiveMode();
        etBarCode.setEnabled(true);
        etBarCode.clearInput();
        etBarCode.requestFocusEnd();
        isAcceptBarcodeEnabled = true;
    }

    @Override
    public void onDeactiveMode() {
        super.onDeactiveMode();
        etBarCode.setEnabled(false);
        isAcceptBarcodeEnabled = false;
    }

    /**
     * 注册监听器
     */
    @Override
    protected void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BA_HANDLE_AMOUNT_CHANGED_VIP);
        intentFilter.addAction(Constants.BA_HANDLE_SCANBARCODE);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                Bundle extras = intent.getExtras();
                ZLogger.d(String.format("onReceive.action=%s\n%s",
                        action, StringUtils.decodeBundle(extras)));
                if (StringUtils.isEmpty(action) || extras == null){
                    return;
                }

                if (intent.getAction().equals(Constants.BA_HANDLE_AMOUNT_CHANGED_VIP)) {
                    if (extras.containsKey(EXTRA_KEY_HANDLE_AMOUNT)) {
                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT, 0);
                        etBarCode.clearInput();
                        etBarCode.requestFocusEnd();
                        calculateCharge();
                    }
                }
                else if (Constants.BA_HANDLE_SCANBARCODE.equals(intent.getAction())){
                    int wayType = extras.getInt(EXTRA_KEY_WAYTYPE, WayType.NA);
                    if (payType == wayType){
                        onScanCode(extras.getString(EXTRA_KEY_SCANCODE));
                    }
                }

            }
        };
        //使用LocalBroadcastManager.getInstance(getActivity())反而不行，接收不到。
        getActivity().registerReceiver(receiver, intentFilter);
    }

    public void onEventMainThread(PayEvent event) {
        int action = event.getAction();
        Bundle extras = event.getArgs();
        ZLogger.d(String.format("PayEvent:%d\n%s",
                action, StringUtils.decodeBundle(extras)));
        if (extras == null){
            return;
        }

        switch (event.getAction()) {
            case PayEvent.EVENT_ID_SCAN_PAYCODE: {
                int wayType = extras.getInt(EXTRA_KEY_WAYTYPE, WayType.NA);
                if (payType == wayType){
                    onScanCode(extras.getString(EXTRA_KEY_SCANCODE));
                }
            }
            break;
        }
    }


    private void initBarCodeInput() {
        etBarCode.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER},
                new EditLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            submitOrder();
                        }
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

        String humanId = MUtils.parseMfPaycode(etBarCode.getInput());
        if (StringUtils.isEmpty(humanId)) {
            validateFailed("请扫描会员付款码");
            return;
        }

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            validateFailed(getString(R.string.toast_network_error));
            return;
        }
        UserApiImpl.findHumanByHumanId(humanId, findMemberResponseCallback);
    }

    /**
     * 验证会员付款码
     */
    private NetCallBack.NetTaskCallBack findMemberResponseCallback = new NetCallBack.NetTaskCallBack<Human,
            NetProcessor.Processor<Human>>(
            new NetProcessor.Processor<Human>() {
                @Override
                public void processResult(final IResponseData rspData) {
                    Human memInfo = null;
                    if (rspData != null) {
                        RspBean<Human> retValue = (RspBean<Human>) rspData;
                        memInfo = retValue.getValue();
                    }

                    if (memInfo == null) {
                        validateFailed("未查询到结果");
                    } else {
                        validateSuccess(1, null, memInfo);
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    validateFailed(errMsg);
                }
            }
            , Human.class
            , AppContext.getAppContext()) {
    };


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

        etBarCode.clearInput();
    }





}
