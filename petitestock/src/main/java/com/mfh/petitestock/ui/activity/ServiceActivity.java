package com.mfh.petitestock.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.petitestock.R;
import com.mfh.petitestock.ui.fragment.ChainGoodsFragment;
import com.mfh.petitestock.ui.fragment.CommodityFragment;
import com.mfh.petitestock.ui.fragment.shelves.GoodsShelvesFragment;
import com.mfh.petitestock.ui.fragment.OfficeListFragment;
import com.mfh.petitestock.ui.fragment.PackageFragment;
import com.mfh.petitestock.ui.fragment.receipt.InvRecvOrderSplashFragment;
import com.mfh.petitestock.ui.fragment.stocktake.InventoryCheckFragment;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ServiceActivity extends BaseActivity {

    public static final String EXTRA_KEY_SERVICE_TYPE = "EXTRA_KEY_SERVICE_TYPE";
    public static final int FRAGMENT_TYPE_NONE = -1;
    public static final int FRAGMENT_TYPE_PACKAGE = 0;
    public static final int FRAGMENT_TYPE_STOCK = 1;
    public static final int FRAGMENT_TYPE_GOODS         = 3;
    public static final int FRAGMENT_TYPE_OFFICELIST = 4;
    public static final int FRAGMENT_TYPE_DISTRIBUTION = 5;
    public static final int FRAGMENT_TYPE_INVENTORY_CHECK = 6;//盘点订单列表
    public static final int FT_WHOLESALER_GOODS             = 7;//商品
    public static final int FT_WHOLESALER_GOODSSHELVES      = 8;//货架绑定商品


    private PackageFragment packageFragment;
    private OfficeListFragment officeListFragment;
    private InvRecvOrderSplashFragment invRecvOrderSplashFragment;
//    private GoodsShelvesFragment mGoodsShelvesFragment;

    /**
     * 0: 快递代收
     * */
    private int serviceType = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, ServiceActivity.class);
        if (extras != null){
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_service;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
    }


    private void handleIntent(){
        Intent intent = this.getIntent();
        if(intent != null){
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
                this.setTheme(R.style.NewFlow);
            }

            serviceType = intent.getIntExtra(EXTRA_KEY_SERVICE_TYPE, FRAGMENT_TYPE_NONE);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     * */
    private void initFragments(){
////        fragments = new Fragment[]{serviceFragment, surroundFragment};
//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.fragment_container, stockInFragment)
//                .add(R.id.fragment_container, stockOutFragment).hide(stockInFragment).hide(stockOutFragment)
//                .commit();

        if(serviceType == FRAGMENT_TYPE_PACKAGE){
            packageFragment = new PackageFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, packageFragment)
//                    .add(R.id.fragment_container, packageFragment).show(packageFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_GOODS){
            CommodityFragment commodityFragment = new CommodityFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, commodityFragment)
//                    .add(R.id.fragment_container, commodityFragment).show(commodityFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_OFFICELIST){
            officeListFragment = new OfficeListFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, officeListFragment)
//                    .add(R.id.fragment_container, officeListFragment).show(officeListFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_DISTRIBUTION){
//            InvSendOrderListFragment distributionListFragment = new InvSendOrderListFragment();
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, distributionListFragment).show(distributionListFragment)
//                    .commit();
            invRecvOrderSplashFragment = new InvRecvOrderSplashFragment();
            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, invRecvOrderSplashFragment)
                    .add(R.id.fragment_container, invRecvOrderSplashFragment).show(invRecvOrderSplashFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_INVENTORY_CHECK){
            InventoryCheckFragment inventoryCheckFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                inventoryCheckFragment = InventoryCheckFragment.newInstance(intent.getExtras());
            }else{
                inventoryCheckFragment = InventoryCheckFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, inventoryCheckFragment)
//                    .add(R.id.fragment_container, inventoryCheckFragment).show(inventoryCheckFragment)
                    .commit();
        }

        else if(serviceType == FT_WHOLESALER_GOODS){
            ChainGoodsFragment chainGoodsFragment = new ChainGoodsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, chainGoodsFragment)
//                    .add(R.id.fragment_container, commodityFragment).show(commodityFragment)
                    .commit();
        }
        else if(serviceType == FT_WHOLESALER_GOODSSHELVES){
            GoodsShelvesFragment mGoodsShelvesFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                mGoodsShelvesFragment = GoodsShelvesFragment.newInstance(intent.getExtras());
            }else{
                mGoodsShelvesFragment = GoodsShelvesFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mGoodsShelvesFragment)
//                    .add(R.id.fragment_container, mGoodsShelvesFragment).show(mGoodsShelvesFragment)
                    .commit();
        }
    }
}
