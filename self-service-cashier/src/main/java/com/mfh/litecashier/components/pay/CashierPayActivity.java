package com.mfh.litecashier.components.pay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.R;


/**
 * 收银支付页面
 * Created by bingshanguxue on 17/07/05.
 */
public class CashierPayActivity extends BaseActivity {

    private CashierPayFragment mCashierPayFragment;

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

//        EventBus.getDefault().register(this);

        Intent intent = this.getIntent();
        if (mCashierPayFragment == null) {
            mCashierPayFragment = CashierPayFragment.newInstance(intent.getExtras());
        } else {
            mCashierPayFragment.setArguments(intent.getExtras());
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mCashierPayFragment)
                .commit();    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
                this.setTheme(R.style.NewFlow);
            }
        }
    }

}
