package com.manfenjiayuan.pda_supermarket.ui.store;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.cashier.model.CashierOrderInfo;
import com.manfenjiayuan.pda_supermarket.ui.pay.PayActionEvent;
import com.manfenjiayuan.pda_supermarket.ui.pay.order.PayStep1Fragment;
import com.manfenjiayuan.pda_supermarket.ui.pay.order.PayStep2Fragment;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.bean.Human;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;



/**
 * 收银订单支付页面
 * Created by bingshanguxue on 15/8/30.
 */
public class CashierPayActivity extends BaseActivity {

    public static final String EXTRA_KEY_CASHIER_ORDERINFO = "cashierOrderInfo";
    public static final String EXTRA_KEY_IS_CLEAR_ORDER = "isClearOrder";

    private PayStep1Fragment mPayStep1Fragment;//
    private PayStep2Fragment mPayStep2Fragment;//

    private CashierOrderInfo cashierOrderInfo = null;

    private int curStep = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, CashierPayActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_fragment;
    }

//    @Override
//    protected boolean isBackKeyEnabled() {
//        return false;
//    }

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
    public void showStep1(CashierOrderInfo cashierOrderInfo) {
        curStep = 0;
        Intent intent = this.getIntent();
        intent.putExtra(EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
        if (mPayStep1Fragment == null) {
            mPayStep1Fragment = PayStep1Fragment.newInstance(intent.getExtras());
        }
        else{
            mPayStep1Fragment.setArguments(intent.getExtras());
        }

        getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseGoodsFragment).show(purchaseGoodsFragment)
                .replace(R.id.fragment_container, mPayStep1Fragment)
                .commit();
    }

    public void showStep2(int payType, int paySubType, String cardId, Human memberInfo) {
        if (curStep == 1){
            ZLogger.df("已经是会员支付页面，跳转无效。");
            return;
        }
        ZLogger.df("准备跳转到会员支付页面");
        curStep = 1;
        if (cashierOrderInfo != null) {
            cashierOrderInfo.vipPrivilege(memberInfo);
        }

        Bundle args = new Bundle();
        args.putSerializable(PayStep2Fragment.EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
        args.putInt(PayStep2Fragment.EXTRA_KEY_PAYTYPE, payType);
        args.putInt(PayStep2Fragment.EXTRA_KEY_PAY_SUBTYPE, paySubType);
        args.putString(PayStep2Fragment.EXTRA_KEY_VIP_CARID, cardId);
        if (mPayStep2Fragment == null) {
            mPayStep2Fragment = PayStep2Fragment.newInstance(args);
        }
        else{
            mPayStep2Fragment.setArguments(args);
        }

        getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseGoodsFragment).show(purchaseGoodsFragment)
                .replace(R.id.fragment_container, mPayStep2Fragment)
                .commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PayActionEvent event) {
        Bundle args = event.getArgs();
        ZLogger.df(String.format("PayActionEvent:%d\n%s",
                event.getAction(), StringUtils.decodeBundle(args)));
        switch (event.getAction()){
            case PayActionEvent.PAY_ACTION_VIP_DETECTED:{
                if (args != null){
                    Human memberInfo = (Human) args.getSerializable(PayActionEvent.KEY_MEMBERINFO);
                    int payType = args.getInt(PayActionEvent.KEY_PAY_TYPE);
                    int paySubType = args.getInt(PayActionEvent.KEY_PAY_SUBTYPE);
                    String cardId = args.getString(PayActionEvent.KEY_CARD_ID);
                    showStep2(payType, paySubType, cardId, memberInfo);
                }
            }
            break;
        }
    }

}
