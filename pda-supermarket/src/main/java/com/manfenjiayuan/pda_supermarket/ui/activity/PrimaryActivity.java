package com.manfenjiayuan.pda_supermarket.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.scanner.IData95Activity;
import com.manfenjiayuan.pda_supermarket.ui.fragment.BindGoods2TagFragment;
import com.manfenjiayuan.pda_supermarket.ui.fragment.CreateInvLossOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.fragment.GoodsFragment;
import com.manfenjiayuan.pda_supermarket.ui.fragment.PackageFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.invconvert.InvConvertFromFragment;
import com.manfenjiayuan.pda_supermarket.ui.invreturn.CreateInvReturnOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.fragment.receipt.InvRecvOrderSplashFragment;
import com.manfenjiayuan.pda_supermarket.ui.fragment.stocktake.InventoryCheckFragment;
import com.mfh.framework.uikit.BackHandledInterface;
import com.mfh.framework.uikit.base.BaseFragment;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PrimaryActivity extends IData95Activity implements BackHandledInterface {

    public static final String EXTRA_KEY_SERVICE_TYPE = "EXTRA_KEY_SERVICE_TYPE";
    public static final int FRAGMENT_TYPE_NONE = -1;
    public static final int FRAGMENT_TYPE_GOODS         = 1;
    public static final int FRAGMENT_TYPE_PACKAGE = 2;
    public static final int FRAGMENT_TYPE_INVENTORY_CHECK = 3;//盘点订单列表
    public static final int FRAGMENT_TYPE_DISTRIBUTION = 5;
    public static final int FT_BIND_GOODS_2_TAGS      = 10;//货架绑定商品
    public static final int FT_CREATE_INV_RETURNORDER      = 11;//退货
    public static final int FT_CREATE_INV_LOSSORDER      = 12;//报损
    public static final int FT_INV_CONVERT      = 13;//库存转换


    /**
     * 0: 快递代收
     * */
    private int serviceType = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, PrimaryActivity.class);
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
    protected boolean finishScannerWhenDestroyEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        hideSystemUI();

        handleIntent();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        hideSystemUI();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        if(mBackHandedFragment == null || !mBackHandedFragment.onBackPressed()){
            if(getSupportFragmentManager().getBackStackEntryCount() == 0){
                super.onBackPressed();
            }else{
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    private BaseFragment mBackHandedFragment;
    @Override
    public void setSelectedFragment(BaseFragment selectedFragment) {
        this.mBackHandedFragment = selectedFragment;
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
            PackageFragment packageFragment = new PackageFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, packageFragment)
//                    .add(R.id.fragment_container, packageFragment).show(packageFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_GOODS){
            GoodsFragment goodsFragment = new GoodsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, goodsFragment)
//                    .add(R.id.fragment_container, goodsFragment).show(goodsFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_DISTRIBUTION){
            InvRecvOrderSplashFragment invRecvOrderSplashFragment = new InvRecvOrderSplashFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, invRecvOrderSplashFragment)
//                    .add(R.id.fragment_container, invRecvOrderSplashFragment).show(invRecvOrderSplashFragment)
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
        else if(serviceType == FT_BIND_GOODS_2_TAGS){
            BindGoods2TagFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = BindGoods2TagFragment.newInstance(intent.getExtras());
            }else{
                fragment = BindGoods2TagFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, mBindGoods2TagFragment).show(mBindGoods2TagFragment)
                    .commit();
        }
        else if(serviceType == FT_CREATE_INV_RETURNORDER){
            CreateInvReturnOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = CreateInvReturnOrderFragment.newInstance(intent.getExtras());
            }else{
                fragment = CreateInvReturnOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, mBindGoods2TagFragment).show(mBindGoods2TagFragment)
                    .commit();
        }
        else if(serviceType == FT_CREATE_INV_LOSSORDER){
            CreateInvLossOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = CreateInvLossOrderFragment.newInstance(intent.getExtras());
            }else{
                fragment = CreateInvLossOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, mBindGoods2TagFragment).show(mBindGoods2TagFragment)
                    .commit();
        }
        else if(serviceType == FT_INV_CONVERT){
            InvConvertFromFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = InvConvertFromFragment.newInstance(intent.getExtras());
            }else{
                fragment = InvConvertFromFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, mBindGoods2TagFragment).show(mBindGoods2TagFragment)
                    .commit();
        }
    }
}
