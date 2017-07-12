package com.mfh.litecashier.components.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.model.PaymentInfo;
import com.bingshanguxue.cashier.pay.BasePayFragment;
import com.bingshanguxue.cashier.pay.PayStep1Event;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.CommonUserAccountHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.dialog.EnterPasswordDialog;
import com.mfh.litecashier.ui.widget.PayProcessView;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

import static com.bingshanguxue.cashier.pay.BasePayStepFragment.EXTRA_KEY_PAY_SUBTYPE;

/**
 * 会员卡支付
 * Created by bingshanguxue on 15/8/31.
 */
public class PayByVipFragment extends BasePayFragment {
    @BindView(R.id.button_submit)
    Button btnSubmit;
    @BindView(R.id.payProcessView)
    PayProcessView payProcessView;

    private Human mHuman;
    /**
     * 0:会员卡，1:付款码，2:手机号
     */
    private int paySubType = 2;
    private EnterPasswordDialog mEnterPasswordDialog = null;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_vip_v2;
    }

    @Override
    protected int getPayType() {
        return WayType.VIP;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);

//for Fragment.instantiate
        Bundle args = getArguments();
        if (args != null) {
            mHuman = (Human) args.getSerializable(EXTRA_KEY_MEMBERINFO);
            paySubType = args.getInt(EXTRA_KEY_PAY_SUBTYPE);
        }

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
    }

    @Override
    public void onDeactiveMode() {
        super.onDeactiveMode();
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
                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT);
                        calculateCharge();
                    }
                }
            }
        };
        //使用LocalBroadcastManager.getInstance(getActivity())反而不行，接收不到。
        getActivity().registerReceiver(receiver, intentFilter);
    }

    /**
     * 输入支付密码
     */
    private void enterPayPassword() {
        if (mEnterPasswordDialog == null) {
            mEnterPasswordDialog = new EnterPasswordDialog(getActivity());
            mEnterPasswordDialog.setCancelable(true);
            mEnterPasswordDialog.setCanceledOnTouchOutside(true);
        }
        mEnterPasswordDialog.init("支付密码", EnterPasswordDialog.TARGET_CASHIER_VIP_PAY,
                new EnterPasswordDialog.OnEnterPasswordListener() {
                    @Override
                    public void onSubmit(int target, String password) {
                        payByAccountPassword(password);
                    }

                    @Override
                    public void onCancel(int target) {
                        btnSubmit.setEnabled(true);
                    }
                });
        mEnterPasswordDialog.show();
    }

    private void payByAccountPassword(String password) {
        if (StringUtils.isEmpty(password)) {
            DialogUtil.showHint("请输入支付密码");
            btnSubmit.setEnabled(true);
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("humanId", String.valueOf(mHuman.getId()));
        if (!StringUtils.isEmpty(password)) {
            options.put("accountPassword", password);
        }
        options.put("amount", MUtils.formatDouble(handleAmount, ""));
        options.put("bizType", bizType);
        options.put("orderId", outTradeNo);
        options.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        payDirect(options);
    }


    /**
     * 会员支付
     */
    @OnClick(R.id.button_submit)
    @Override
    public void submitOrder() {
        super.submitOrder();
        btnSubmit.setEnabled(false);

        if (paySubType == 2) {
            enterPayPassword();
        } else {
            Map<String, String> options = new HashMap<>();
            options.put("humanId", String.valueOf(mHuman.getId()));
//        if (!StringUtils.isEmpty(vipCardId)) {
//            options.put("cardNo", vipCardId);
//        }
            options.put("amount", MUtils.formatDouble(handleAmount, ""));
            options.put("bizType", bizType);
            options.put("orderId", outTradeNo);
            options.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
            options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

            payDirect(options);
        }
    }

    /**
     * 会员支付
     */
    private void payDirect(Map<String, String> options) {
        payProcessView.setState(PayProcessView.STATE_PROCESS, "正在发送支付请求");

        CommonUserAccountHttpManager.getInstance().payDirect(options, new MValueSubscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                PaymentInfo paymentInfo = PaymentInfo.create(outTradeNo, payType,
                        PosOrderPayEntity.PAY_STATUS_FAILED,
                        handleAmount, handleAmount, 0D,
                        null);
                payProcessView.setState(PayProcessView.STATE_FAILED, e.getMessage());
                btnSubmit.setEnabled(true);

                Bundle args = new Bundle();
                args.putSerializable(PayStep1Event.KEY_PAYMENT_INFO, paymentInfo);
                args.putString(PayStep1Event.KEY_ERROR_MESSAGE, e.getMessage());
                EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FAILED, args));
            }

            @Override
            public void onValue(String data) {
                super.onValue(data);

                ZLogger.i(String.format("%s %s 支付成功", outTradeNo,
                        WayType.getWayTypeName(payType)));
                Double balance = 0D;
                if (data != null) {
                    balance = Double.valueOf(data);
                }
//                    bPayProcessing = false;
//                        Human human = cashierOrderInfo.getVipMember();
                PaymentInfo paymentInfo = PaymentInfo.create(outTradeNo, payType,
                        PosOrderPayEntity.PAY_STATUS_FINISH,
                        handleAmount, handleAmount,
                        balance,
                        null);
                payProcessView.setState(PayProcessView.STATE_SUCCESS, data);

                Bundle args = new Bundle();
                args.putSerializable(PayStep1Event.KEY_PAYMENT_INFO, paymentInfo);
                EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FINISHED, args));

                btnSubmit.setEnabled(true);
            }

        });
    }


}
