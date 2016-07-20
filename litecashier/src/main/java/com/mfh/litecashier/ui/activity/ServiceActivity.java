package com.mfh.litecashier.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.fragment.inventory.CreateInventoryIOOrderFragment;
import com.mfh.litecashier.ui.fragment.inventory.CreateInventoryTransOrderFragment;
import com.mfh.litecashier.ui.fragment.purchase.CreatePurchaseReceiptOrderFragment;
import com.mfh.litecashier.ui.fragment.purchase.CreatePurchaseReturnOrderFragment;
import com.mfh.litecashier.ui.fragment.purchase.MallFragment;
import com.mfh.litecashier.ui.fragment.cashier.StockDetailFragment;
import com.mfh.litecashier.ui.fragment.cashier.StockInFragment;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ServiceActivity extends BaseActivity {
    public static final String EXTRA_KEY_SERVICE_TYPE = "EXTRA_KEY_SERVICE_TYPE";
    public static final String EXTRA_KEY_COURIER = "EXTRA_KEY_COURIER";

    public static final int FRAGMENT_TYPE_STOCK_IN = 0x00;
    public static final int FRAGMENT_TYPE_STOCK_DETAIL = 0x02;
    public static final int FRAGMENT_TYPE_COMMODITY_CENTER = 0x05;  //商品中心
    public static final int FRAGMENT_TYPE_CREATE_PURCHASE_RECEIPT_ORDER = 0x10;//新建采购收货单
    public static final int FRAGMENT_TYPE_CREATE_PURCHASE_RETURN_ORDER  = 0x11;//新建采购退货单
    public static final int FRAGMENT_TYPE_CREATE_INVENTORY_ALLOCATION_ORDER = 0x20;//新建库存调拨单
    public static final int FRAGMENT_TYPE_CREATE_INVENTORY_IO_ORDER     = 0x21;     //新建库存出入库单

    /**
     * 0: 快递代收
     */
    private int serviceType = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, ServiceActivity.class);
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
//                this.setTheme(R.style.NewFlow);
            }

            serviceType = intent.getIntExtra(EXTRA_KEY_SERVICE_TYPE, -1);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     */
    private void initFragments() {
        if (serviceType == FRAGMENT_TYPE_STOCK_IN) {
            StockInFragment stockInFragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                stockInFragment = StockInFragment.newInstance(intent.getExtras());
            } else {
                stockInFragment = StockInFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, stockInFragment).show(stockInFragment)
                    .commit();
        } else if (serviceType == FRAGMENT_TYPE_STOCK_DETAIL) {
            StockDetailFragment stockDetailFragment = new StockDetailFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, stockDetailFragment).show(stockDetailFragment)
                    .commit();
        } else if (serviceType == FRAGMENT_TYPE_COMMODITY_CENTER) {
            MallFragment mallFragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                mallFragment = MallFragment.newInstance(intent.getExtras());
            } else {
                mallFragment = MallFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mallFragment).show(mallFragment)
                    .commit();
        }
        //新建采购收货单
        else if (serviceType == FRAGMENT_TYPE_CREATE_PURCHASE_RECEIPT_ORDER) {
            CreatePurchaseReceiptOrderFragment createPurchaseReceiptOrderFragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                createPurchaseReceiptOrderFragment = CreatePurchaseReceiptOrderFragment.newInstance(intent.getExtras());
            } else {
                createPurchaseReceiptOrderFragment = CreatePurchaseReceiptOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, createPurchaseReceiptOrderFragment).show(createPurchaseReceiptOrderFragment)
                    .commit();
        }
        //新建采购退货单
        else if (serviceType == FRAGMENT_TYPE_CREATE_PURCHASE_RETURN_ORDER) {
            CreatePurchaseReturnOrderFragment createPurchaseReturnOrderFragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                createPurchaseReturnOrderFragment = CreatePurchaseReturnOrderFragment.newInstance(intent.getExtras());
            } else {
                createPurchaseReturnOrderFragment = CreatePurchaseReturnOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, createPurchaseReturnOrderFragment).show(createPurchaseReturnOrderFragment)
                    .commit();
        }
        //新建库存调拨单
        else if (serviceType == FRAGMENT_TYPE_CREATE_INVENTORY_ALLOCATION_ORDER) {
            CreateInventoryTransOrderFragment createInventoryAllocationOrderFragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                createInventoryAllocationOrderFragment = CreateInventoryTransOrderFragment.newInstance(intent.getExtras());
            } else {
                createInventoryAllocationOrderFragment = CreateInventoryTransOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, createInventoryAllocationOrderFragment).show(createInventoryAllocationOrderFragment)
                    .commit();
        }
        //新建库存出入库单
        else if (serviceType == FRAGMENT_TYPE_CREATE_INVENTORY_IO_ORDER) {
            CreateInventoryIOOrderFragment createInventoryIOOrderFragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                createInventoryIOOrderFragment = CreateInventoryIOOrderFragment.newInstance(intent.getExtras());
            } else {
                createInventoryIOOrderFragment = CreateInventoryIOOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, createInventoryIOOrderFragment).show(createInventoryIOOrderFragment)
                    .commit();
        }
    }
}
