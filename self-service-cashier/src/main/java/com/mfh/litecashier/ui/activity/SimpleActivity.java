package com.mfh.litecashier.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.fragment.canary.OrderFlowFragment;
import com.mfh.litecashier.ui.fragment.canary.SettingsDailysettleFragment;
import com.mfh.litecashier.ui.fragment.canary.SettingsGoodsFragment;
import com.mfh.litecashier.ui.fragment.canary.SettingsTestFragment;
import com.mfh.litecashier.ui.fragment.inventory.InventoryCostFragment;
import com.mfh.litecashier.ui.fragment.online.OnlineFragment;
import com.mfh.litecashier.ui.fragment.orderflow.ExceptionOrdersFragment;
import com.mfh.litecashier.ui.fragment.orderflow.StoreOrderFlowFragment;
import com.mfh.litecashier.ui.fragment.purchase.IntelligentShopcartFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseFreshEvent;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseFreshFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseFruitFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseFruitShopcartFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseGoodsFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseShopcartFragment;
import com.mfh.litecashier.ui.fragment.purchase.manual.ManualPurchaseFragment;
import com.mfh.litecashier.ui.fragment.purchase.manual.ManualPurchaseShopcartFragment;
import com.mfh.litecashier.ui.fragment.settings.SettingsFragment;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SimpleActivity extends BaseActivity {
    public static final String EXTRA_KEY_SERVICE_TYPE = "EXTRA_KEY_SERVICE_TYPE";
    public static final String EXTRA_KEY_COURIER = "EXTRA_KEY_COURIER";

    public static final int FT_PURSHACE_STANDARD_GOODS = 0x01;//申请商品
    public static final int FT_PURCHASE_FRESH_GOODS = 0x02;//
    public static final int FT_PURCHASE_FRUIT_GOODS = 0x03;//
    public static final int FT_PURCHASE_FRUIT_SHOPCART = 0x04;//采购生鲜－购物车
    public static final int FT_PURCHASE_STANDARD_SHOPCART = 0x05;//采购商品－购物车
    public static final int FT_PURCHASE_MANUAL = 0x06;//手动订货
    public static final int FT_PURCHASE_MANUAL_SHOPCART = 0x07;//采购商品－购物车
    public static final int FT_PURCHASE_INTELLIGENT_SHOPCART = 0x08;//智能订货－购物车

    public static final int FT_INVENTORY = 0x11;//库存
    public static final int FT_ORDERFLOW = 0x12;//POS流水
    public static final int FT_ONLINE_ORDER = 0x14;//线上订单:(生鲜预定)
    public static final int FT_EXCEPTION_ORDERS = 0x15;//POS异常订单
    public static final int FT_RECEIPT = 0x16;//单据
    public static final int FT_SETTINGS = 0x17;//设置


    public static final int FT_CANARY_GOODS = 0x20;//商品
    public static final int FT_CANARY_ORDERFLOW = 0x21;//流水
    public static final int FT_CANARY_DAILYSETTLE = 0x22;//日结
    public static final int FT_CANARY_CANARY = 0x23;//金丝雀

    @Bind(R.id.toolbar)
    Toolbar toolbar;

//    MenuItem freshShopcartMenu = null;
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
                } else if (id == R.id.action_sync_products) {
                    EventBus.getDefault().post(new PurchaseFreshEvent(PurchaseFreshEvent.EVENT_ID_SYNC_START));
//                } else if (id == R.id.action_fresh_shopcart) {
//                    redirectToFruitShopcart();
                } else if (id == R.id.action_shopcart_standard) {
                    redirectToStandardShopcart();
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

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

//        startService(new Intent(this, Utf7ImeService.class));
        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
    }


    @Override
    protected void onResume() {
        super.onResume();

//        if (freshShopcartMenu != null) {
//            freshShopcartMenu.setEnabled(true);
//        }
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
            case FT_PURCHASE_FRESH_GOODS:
            case FT_PURCHASE_FRUIT_GOODS: {
                getMenuInflater().inflate(R.menu.menu_purchase_fresh, menu);
//                freshShopcartMenu = menu.findItem(R.id.action_fresh_shopcart);
            }
            break;
            case FT_PURSHACE_STANDARD_GOODS:
            case FT_PURCHASE_STANDARD_SHOPCART:
            case FT_PURCHASE_FRUIT_SHOPCART:
            case FT_ORDERFLOW:
            case FT_RECEIPT:
            case FT_EXCEPTION_ORDERS:
            case FT_SETTINGS:
            case FT_PURCHASE_MANUAL:
            case FT_PURCHASE_MANUAL_SHOPCART:
            case FT_PURCHASE_INTELLIGENT_SHOPCART:
            case FT_CANARY_GOODS:
            case FT_CANARY_ORDERFLOW:
            case FT_CANARY_CANARY:
            case FT_CANARY_DAILYSETTLE: {
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
            case FT_PURCHASE_FRESH_GOODS: {
                toolbar.setTitle("生鲜");
                PurchaseFreshFragment fragment = new PurchaseFreshFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_PURCHASE_FRUIT_GOODS: {
                toolbar.setTitle("水果");
                PurchaseFruitFragment fragment = new PurchaseFruitFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_PURSHACE_STANDARD_GOODS: {
                toolbar.setTitle("普货");
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
            case FT_PURCHASE_INTELLIGENT_SHOPCART: {
                toolbar.setTitle("智能订货");
                IntelligentShopcartFragment fragment = new IntelligentShopcartFragment();
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
            case FT_ONLINE_ORDER: {
                toolbar.setTitle("线上订单");
                OnlineFragment fragment = new OnlineFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_EXCEPTION_ORDERS: {
                toolbar.setTitle("异常订单");
                ExceptionOrdersFragment fragment = new ExceptionOrdersFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_PURCHASE_STANDARD_SHOPCART: {
                toolbar.setTitle("购物车");
                PurchaseShopcartFragment fragment = new PurchaseShopcartFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_PURCHASE_FRUIT_SHOPCART: {
                toolbar.setTitle("购物车");
                PurchaseFruitShopcartFragment fragment = new PurchaseFruitShopcartFragment();
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
            case FT_CANARY_DAILYSETTLE: {
                toolbar.setTitle("日结");
                SettingsDailysettleFragment fragment = new SettingsDailysettleFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_CANARY_CANARY: {
                toolbar.setTitle("测试");
                SettingsTestFragment fragment = new SettingsTestFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
        }
    }

    public void redirectToFruitShopcart() {
//        if (freshShopcartMenu != null) {
//            freshShopcartMenu.setEnabled(false);
//        }

//        DialogUtil.showHint("跳转到购物车");
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FT_PURCHASE_FRUIT_SHOPCART);

        Intent intent = new Intent(this, SimpleActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_APPLY_SHOPCART);
//        UIHelper.startActivity(this, SimpleActivity.class, extras);
    }

    public void redirectToStandardShopcart() {
        if (standardShopcartMenu != null) {
            standardShopcartMenu.setEnabled(false);
        }

//        DialogUtil.showHint("跳转到购物车");
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FT_PURCHASE_STANDARD_SHOPCART);

        Intent intent = new Intent(this, SimpleActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_APPLY_SHOPCART);
//        UIHelper.startActivity(this, SimpleActivity.class, extras);
    }
}
