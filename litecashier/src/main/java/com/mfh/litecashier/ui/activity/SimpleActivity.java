package com.mfh.litecashier.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseFreshFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseFreshShopcartFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseGoodsFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseShopcartFragment;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SimpleActivity extends BaseActivity {
    public static final String EXTRA_KEY_SERVICE_TYPE = "EXTRA_KEY_SERVICE_TYPE";
    public static final String EXTRA_KEY_COURIER = "EXTRA_KEY_COURIER";

    public static final int FRAGMENT_TYPE_COMMODITY_APPLY           = 0;//申请商品
    public static final int FT_PURCHASE_FRESH_GOODS     = 1;//采购生鲜
    public static final int FT_PURCHASE_FRESH_SHOPCART  = 2;//采购生鲜－购物车
    public static final int FRAGMENT_TYPE_COMMODITY_APPLY_SHOPCART  = 3;//采购商品－购物车

    /**
     * 0: 快递代收
     */
    private int serviceType = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, SimpleActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_service;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

//        startService(new Intent(this, Utf7ImeService.class));
        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
                this.setTheme(R.style.NewFlow);
            }

            serviceType = intent.getIntExtra(EXTRA_KEY_SERVICE_TYPE, -1);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     */
    private void initFragments() {
        if (serviceType == FRAGMENT_TYPE_COMMODITY_APPLY) {
            PurchaseGoodsFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = PurchaseGoodsFragment.newInstance(intent.getExtras());
            } else {
                fragment = PurchaseGoodsFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseGoodsFragment).show(purchaseGoodsFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (serviceType == FRAGMENT_TYPE_COMMODITY_APPLY_SHOPCART) {
            PurchaseShopcartFragment fragment = new PurchaseShopcartFragment();
            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (serviceType == FT_PURCHASE_FRESH_GOODS) {
            PurchaseFreshFragment fragment = new PurchaseFreshFragment();
            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (serviceType == FT_PURCHASE_FRESH_SHOPCART) {
            PurchaseFreshShopcartFragment fragment = new PurchaseFreshShopcartFragment();
            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
