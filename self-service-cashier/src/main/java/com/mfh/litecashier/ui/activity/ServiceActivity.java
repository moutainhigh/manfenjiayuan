package com.mfh.litecashier.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.fragment.GrouponFragment;
import com.mfh.litecashier.ui.fragment.cashier.StockDetailFragment;
import com.mfh.litecashier.ui.fragment.inventory.CreateInventoryIOOrderFragment;
import com.mfh.litecashier.ui.fragment.inventory.CreateInventoryTransOrderFragment;
import com.mfh.litecashier.ui.fragment.purchase.MallFragment;

import butterknife.Bind;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ServiceActivity extends BaseActivity {
    public static final String EXTRA_KEY_SERVICE_TYPE = "EXTRA_KEY_SERVICE_TYPE";
    public static final String EXTRA_KEY_COURIER = "EXTRA_KEY_COURIER";

    public static final int FRAGMENT_TYPE_STOCK_DETAIL = 0x02;
    public static final int FRAGMENT_TYPE_COMMODITY_CENTER = 0x05;  //商品中心
    public static final int FRAGMENT_TYPE_CREATE_INVENTORY_ALLOCATION_ORDER = 0x20;//新建库存调拨单
    public static final int FRAGMENT_TYPE_CREATE_INVENTORY_IO_ORDER     = 0x21;     //新建库存出入库单
    public static final int FT_GROUPON_DETAIL = 0x22;  //团购详情页


    @Bind(R.id.toolbar)
    Toolbar toolbar;

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
        return R.layout.activity_simple;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
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

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

//        startService(new Intent(this, Utf7ImeService.class));
        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_normal, menu);

        return super.onCreateOptionsMenu(menu);
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
        if (serviceType == FRAGMENT_TYPE_STOCK_DETAIL) {
            toolbar.setTitle("包裹明细");
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
        //团购详情页
        else if (serviceType == FT_GROUPON_DETAIL) {
            toolbar.setTitle("团购");
            GrouponFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = GrouponFragment.newInstance(intent.getExtras());
            } else {
                fragment = GrouponFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment).show(fragment)
                    .commit();

        }
    }
}
