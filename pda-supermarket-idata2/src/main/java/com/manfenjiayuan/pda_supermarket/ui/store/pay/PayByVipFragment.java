package com.manfenjiayuan.pda_supermarket.ui.store.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.R;
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

import butterknife.Bind;
import de.greenrobot.event.EventBus;


/**
 * 会员卡支付
 * Created by bingshanguxue on 15/8/31.
 */
public class PayByVipFragment extends BasePayFragment {
    @Bind(R.id.et_barCode)
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
                ZLogger.d(String.format("onReceive.action=%s\nextras:%s",
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


    private void initBarCodeInput() {
        etBarCode.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                submitOrder();
            }

            @Override
            public void onScan() {

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
        String codeA = etBarCode.getInput();
        if (StringUtils.isEmpty(codeA)) {
            validateFailed("参数无效");
            return;
        }
        ZLogger.d("codeA=" + codeA);

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            validateFailed(getString(R.string.toast_network_error));
            return;
        }

        //长度为15(000000000712878)，微信付款码
        if (codeA.length() == 15) {
            validateVipHumanId(codeA);
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

        etBarCode.clearInput();
    }

    /**
     * 验证会员卡芯片号
     */
    private void validateVipCard(String cardId) {
        final String cardId2 = parseCardId(cardId);
        if (StringUtils.isEmpty(cardId2)) {
            validateFailed("芯片号无效");
            return;
        }

        NetCallBack.NetTaskCallBack findMemberResponseCallback = new NetCallBack.NetTaskCallBack<Human,
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
                            validateSuccess(0, cardId2, memInfo);
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


        //加载用户信息
        UserApiImpl.findHumanByCard(cardId2, findMemberResponseCallback);
    }

    /**
     * 验证会员卡手机号
     */
    private void validateVipPhonenumber(String phoneNumber) {
        NetCallBack.NetTaskCallBack findMemberResponseCallback = new NetCallBack.NetTaskCallBack<Human,
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
                            validateSuccess(2, null, memInfo);
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

        UserApiImpl.findHumanByCard(phoneNumber, findMemberResponseCallback);
    }

    /**
     * 验证会员付款码
     */
    private void validateVipHumanId(String humanId) {
        //这样判断不严谨，会错误的把其他0处理掉
        int index = humanId.lastIndexOf("0");
//        String humanId2 = humanId.substring(index + 1, humanId.length());
        String humanId3 = humanId;
        while (humanId3.startsWith("0")) {
            humanId3 = humanId3.substring(1, humanId3.length());
        }
        ZLogger.df(String.format("验证会员微信付款码: <%s> --> <%s>",
                humanId, humanId3));
        if (StringUtils.isEmpty(humanId3)) {
            validateFailed("付款码无效");
            return;
        }

        NetCallBack.NetTaskCallBack findMemberResponseCallback = new NetCallBack.NetTaskCallBack<Human,
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

        UserApiImpl.findHumanByHumanId(humanId3, findMemberResponseCallback);
    }


}
