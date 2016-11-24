package com.mfh.litecashier.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.fragment.canary.OrderFlowFragment;
import com.mfh.litecashier.ui.fragment.canary.SettingsGoodsFragment;
import com.mfh.litecashier.ui.fragment.goods.BackendCategoryFragment;
import com.mfh.litecashier.ui.fragment.inventory.InventoryCostFragment;
import com.mfh.litecashier.ui.fragment.online.OnlineFragment;
import com.mfh.litecashier.ui.fragment.orderflow.StoreOrderFlowFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseFragment;
import com.mfh.litecashier.ui.fragment.purchase.manual.ManualPurchaseFragment;
import com.mfh.litecashier.ui.fragment.purchase.manual.ManualPurchaseShopcartFragment;
import com.mfh.litecashier.ui.fragment.settings.SettingsFragment;

import butterknife.BindView;


/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SimpleActivity extends BaseActivity {
    public static final String EXTRA_KEY_SERVICE_TYPE = "EXTRA_KEY_SERVICE_TYPE";

    public static final int FT_PURCHASE_MANUAL = 0x06;//手动订货
    public static final int FT_PURCHASE_MANUAL_SHOPCART = 0x07;//采购商品－购物车

    public static final int FT_INVENTORY = 0x11;//库存
    public static final int FT_ORDERFLOW = 0x12;//POS流水
    public static final int FT_ONLINE_ORDER = 0x14;//线上订单:(生鲜预定)
    public static final int FT_RECEIPT = 0x16;//单据
    public static final int FT_SETTINGS = 0x17;//设置
    public static final int FT_GOODS_LIST = 0x18;//商品列表

    public static final int FT_CANARY_GOODS = 0x20;//商品
    public static final int FT_CANARY_ORDERFLOW = 0x21;//流水
    public static final int FT_CANARY_CANARY = 0x23;//金丝雀

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    MenuItem standardShopcartMenu = null;
    /**
     * 0: 快递代收
     */
    private int fragmentType = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, SimpleActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_simple;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return true;
    }

    @Override
    protected boolean isFullscreenEnabled() {
        return true;
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if (fragmentType != FT_ONLINE_ORDER) {
            toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
            toolbar.setNavigationOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SimpleActivity.this.onBackPressed();
                        }
                    });
        }
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_close) {
                    finish();
                }
                return true;
            }
        });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_normal);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

//        startService(new Intent(this, Utf7ImeService.class));
        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (standardShopcartMenu != null) {
            standardShopcartMenu.setEnabled(true);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        switch (fragmentType) {
            case FT_ORDERFLOW:
            case FT_RECEIPT:
            case FT_SETTINGS:
            case FT_PURCHASE_MANUAL:
            case FT_PURCHASE_MANUAL_SHOPCART:
            case FT_CANARY_GOODS:
            case FT_CANARY_ORDERFLOW:
            case FT_GOODS_LIST:{
                getMenuInflater().inflate(R.menu.menu_empty, menu);
            }
            break;
            case FT_INVENTORY: {
                getMenuInflater().inflate(R.menu.menu_purchase_standard, menu);
                standardShopcartMenu = menu.findItem(R.id.action_shopcart_standard);
            }
            break;
            case FT_ONLINE_ORDER:
            default: {
                getMenuInflater().inflate(R.menu.menu_normal, menu);
            }
            break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
//                this.setTheme(R.style.NewFlow);
            }

            fragmentType = intent.getIntExtra(EXTRA_KEY_SERVICE_TYPE, -1);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     */
    private void initFragments() {
        switch (fragmentType) {
            case FT_ONLINE_ORDER: {
                toolbar.setTitle("线上订单");
                OnlineFragment fragment = new OnlineFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_GOODS_LIST: {
                toolbar.setTitle("商品列表");
                BackendCategoryFragment fragment = new BackendCategoryFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_PURCHASE_MANUAL: {
                toolbar.setTitle("手动订货");
                ManualPurchaseFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = ManualPurchaseFragment.newInstance(intent.getExtras());
                } else {
                    fragment = ManualPurchaseFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseGoodsFragment).show(purchaseGoodsFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_PURCHASE_MANUAL_SHOPCART: {
                toolbar.setTitle("购物车");
                ManualPurchaseShopcartFragment fragment = new ManualPurchaseShopcartFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;

            case FT_INVENTORY: {
                toolbar.setTitle("库存成本");
                InventoryCostFragment fragment = new InventoryCostFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_ORDERFLOW: {
                toolbar.setTitle("流水");
                StoreOrderFlowFragment fragment = new StoreOrderFlowFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_RECEIPT: {
                toolbar.setTitle("单据");
                PurchaseFragment fragment = new PurchaseFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_SETTINGS: {
                toolbar.setTitle("设置");
                SettingsFragment fragment = new SettingsFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_CANARY_GOODS: {
                toolbar.setTitle("商品");
                SettingsGoodsFragment fragment = new SettingsGoodsFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_CANARY_ORDERFLOW: {
                toolbar.setTitle("流水");
                OrderFlowFragment fragment = new OrderFlowFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
        }
    }

}
