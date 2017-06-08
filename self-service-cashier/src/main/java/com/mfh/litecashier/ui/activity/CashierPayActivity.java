package com.mfh.litecashier.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.bingshanguxue.cashier.pay.PayActionEvent;
import com.bingshanguxue.cashier.v1.CashierOrderInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.fragment.components.ExchangeScoreFragment;
import com.mfh.litecashier.ui.fragment.pay.PayStep1Fragment;
import com.mfh.litecashier.ui.fragment.pay.PayStep2Fragment;
import com.mfh.litecashier.ui.fragment.topup.TransferFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * 收银订单支付页面
 * Created by bingshanguxue on 15/8/30.
 */
public class CashierPayActivity extends BaseActivity {

    public static final String EXTRA_KEY_CASHIER_ORDERINFO = "cashierOrderInfo";

    private PayStep1Fragment mPayStep1Fragment;
    private PayStep2Fragment mPayStep2Fragment;
    private TransferFragment mTransferFragment;
    private ExchangeScoreFragment mExchangeScoreFragment;

    private CashierOrderInfo cashierOrderInfo = null;

    private int curStep = 0;
    private int payType;
    private int paySubType;
    private String cardId;
    private Human memberInfo;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, CashierPayActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_cashierpay;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected boolean isFullscreenEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        EventBus.getDefault().register(this);

        if (cashierOrderInfo == null) {
            DialogUtil.showHint("订单结算信息无效");
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        showStep1(cashierOrderInfo);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
                this.setTheme(R.style.NewFlow);
            }

            cashierOrderInfo = (CashierOrderInfo) intent.getSerializableExtra(EXTRA_KEY_CASHIER_ORDERINFO);
        }
    }

    /**
     * 显示
     */
    private void showStep1(CashierOrderInfo cashierOrderInfo) {
        curStep = 0;
        Intent intent = this.getIntent();
        intent.putExtra(EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
        if (mPayStep1Fragment == null) {
            mPayStep1Fragment = PayStep1Fragment.newInstance(intent.getExtras());
        } else {
            mPayStep1Fragment.setArguments(intent.getExtras());
        }

        getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseGoodsFragment).show(purchaseGoodsFragment)
                .replace(R.id.fragment_container, mPayStep1Fragment)
                .commit();
    }

    private void showStep2(int payType, int paySubType, String cardId, Human memberInfo, boolean isNeedReloadVIP) {
        if (curStep == 1) {
            ZLogger.d("已经是会员支付页面，跳转无效。");
            return;
        }
        ZLogger.df("准备跳转到会员支付页面");
        curStep = 1;
        this.payType = payType;
        this.paySubType = paySubType;
        this.cardId = cardId;
        this.memberInfo = memberInfo;

        if (cashierOrderInfo != null) {
            cashierOrderInfo.vipPrivilege(memberInfo);
        }

        mTransferFragment = null;

        Bundle args = new Bundle();
        args.putSerializable(PayStep2Fragment.EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
        args.putInt(PayStep2Fragment.EXTRA_KEY_PAYTYPE, payType);
        args.putInt(PayStep2Fragment.EXTRA_KEY_PAY_SUBTYPE, paySubType);
        args.putString(PayStep2Fragment.EXTRA_KEY_VIP_CARID, cardId);
        args.putBoolean(PayStep2Fragment.EXTRA_KEY_IS_RELOAD_VIP, isNeedReloadVIP);
        if (mPayStep2Fragment == null) {
            mPayStep2Fragment = PayStep2Fragment.newInstance(args);
        } else {
            mPayStep2Fragment.setArguments(args);
        }

        getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseGoodsFragment).show(purchaseGoodsFragment)
                .replace(R.id.fragment_container, mPayStep2Fragment)
                .commit();

//        try {
//            ActivityRoute.redirect2ExchangeScore(this, null);
//        } catch (Exception e) {
//            ZLogger.d("redirect2ExchangeScore 失败： " + e.toString());
//        }
    }

    /**
     * 显示
     */
    private void showStep3(Human human) {
        curStep = 2;
        Intent intent = this.getIntent();
        intent.putExtra(TransferFragment.EXTRA_HUMAN, human);
        intent.putExtra(TransferFragment.EXTRA_IS_PAY_ACTION,
                true);

//        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE,
//                SimpleDialogActivity.DT_MIDDLE);
//        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE,
//                SimpleDialogActivity.FT_CUSTOMER_TOPUP);
//        if (human != null) {
//            extras.putSerializable(TransferFragment.EXTRA_HUMAN,
//                    human);
//        }
//        UIHelper.startActivity(context, SimpleDialogActivity.class, extras);

//        ActivityRoute.redirect2Transfer(CashierPayActivity.this, human);
        if (mTransferFragment == null) {
            mTransferFragment = TransferFragment.newInstance(intent.getExtras());
        } else {
            mTransferFragment.setArguments(intent.getExtras());
        }

        getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseGoodsFragment).show(purchaseGoodsFragment)
                .replace(R.id.fragment_container, mTransferFragment)
                .commit();
    }

    /**
     * 显示
     */
    private void showStep4(Human human) {
        curStep = 3;
        Intent intent = this.getIntent();
        intent.putExtra(ExchangeScoreFragment.EXTRA_HUMAN, human);
        intent.putExtra(ExchangeScoreFragment.EXTRA_IS_PAY_ACTION,
                true);
        if (mExchangeScoreFragment == null) {
            mExchangeScoreFragment = ExchangeScoreFragment.newInstance(intent.getExtras());
        } else {
            mExchangeScoreFragment.setArguments(intent.getExtras());
        }

        getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseGoodsFragment).show(purchaseGoodsFragment)
                .replace(R.id.fragment_container, mExchangeScoreFragment)
                .commit();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PayActionEvent event) {
        Bundle args = event.getArgs();
        ZLogger.df(String.format("PayActionEvent:%d\n%s",
                event.getAction(), StringUtils.decodeBundle(args)));
        switch (event.getAction()) {
            case PayActionEvent.PAY_ACTION_VIP_DETECTED: {
                if (args != null) {
                    Human memberInfo = (Human) args.getSerializable(PayActionEvent.KEY_MEMBERINFO);
                    int payType = args.getInt(PayActionEvent.KEY_PAY_TYPE);
                    int paySubType = args.getInt(PayActionEvent.KEY_PAY_SUBTYPE);
                    String cardId = args.getString(PayActionEvent.KEY_CARD_ID);
                    showStep2(payType, paySubType, cardId, memberInfo, false);
                }
            }
            break;
            case PayActionEvent.PAY_ACTION_VIP_UPDATED: {
                boolean isNeedReloadVIP = false;
                if (args != null) {
                    isNeedReloadVIP = args.getBoolean(PayStep2Fragment.EXTRA_KEY_IS_RELOAD_VIP);
                }
                showStep2(payType, paySubType, cardId, memberInfo, isNeedReloadVIP);
            }
            break;
            case PayActionEvent.PAY_ACTION_CUSTOMER_TOPUP: {
                if (args != null) {
                    Human memberInfo = (Human) args.getSerializable(PayActionEvent.KEY_MEMBERINFO);
                    try {
//                        ActivityRoute.redirect2Transfer(CashierPayActivity.this, memberInfo);
                        showStep3(memberInfo);

                    } catch (Exception e) {
                        ZLogger.d("redirect2Transfer 失败： " + e.toString());
                    }
                }
            }
            break;
            case PayActionEvent.PAY_ACTION_CUSTOMER_SCORE: {
                if (args != null) {
                    Human memberInfo = (Human) args.getSerializable(PayActionEvent.KEY_MEMBERINFO);
                    try {
//                        ActivityRoute.redirect2ExchangeScore(CashierPayActivity.this, memberInfo);
                        showStep4(memberInfo);

                    } catch (Exception e) {
                        ZLogger.d("redirect2ExchangeScore 失败： " + e.toString());
                    }
                }
            }
            break;
        }
    }


}
