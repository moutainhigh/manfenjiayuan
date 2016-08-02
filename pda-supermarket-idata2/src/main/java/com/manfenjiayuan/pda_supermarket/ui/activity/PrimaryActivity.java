package com.manfenjiayuan.pda_supermarket.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.bingshanguxue.pda.IData95Activity;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.fragment.BindGoods2TagFragment;
import com.manfenjiayuan.pda_supermarket.ui.invloss.CreateInvLossOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.fragment.PackageFragment;
import com.manfenjiayuan.pda_supermarket.ui.fragment.invconvert.InvConvertFromFragment;
import com.manfenjiayuan.pda_supermarket.ui.invreturn.CreateInvReturnOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.invreceive.InvRecvOrderSplashFragment;
import com.manfenjiayuan.pda_supermarket.ui.invcheck.InvCheckListFragment;
import com.manfenjiayuan.pda_supermarket.ui.goods.ChainGoodsSkuFragment;
import com.manfenjiayuan.pda_supermarket.ui.goods.ScGoodsSkuFragment;
import com.manfenjiayuan.pda_supermarket.ui.invio.CreateInvIoOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.invlabel.InvLabelFragment;
import com.mfh.framework.api.invIoOrder.InvIoOrderApi;
import com.mfh.framework.uikit.BackHandledInterface;
import com.mfh.framework.uikit.base.BaseFragment;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PrimaryActivity extends IData95Activity implements BackHandledInterface {

    public static final String EXTRA_KEY_SERVICE_TYPE = "EXTRA_KEY_SERVICE_TYPE";
    public static final int FRAGMENT_TYPE_NONE = 0x00;
    public static final int FRAGMENT_TYPE_GOODS         = 0x01;
    public static final int FRAGMENT_TYPE_GOODS_PROVIDER         = 0x02;
    public static final int FRAGMENT_TYPE_PACKAGE = 0x03;
    public static final int FRAGMENT_TYPE_INVENTORY_CHECK = 0x04;//盘点订单列表
    public static final int FRAGMENT_TYPE_DISTRIBUTION = 0x05;
    public static final int FT_BIND_GOODS_2_TAGS      = 0x10;//货架绑定商品
    public static final int FT_CREATE_INV_RETURNORDER      = 0x11;//退货
    public static final int FT_CREATE_INV_LOSSORDER      = 0x12;//报损
    public static final int FT_INV_CONVERT      = 0x13;//库存转换
    public static final int FT_INVIO_IN         = 0x21;//入库
    public static final int FT_INVIO_OUT         = 0x22;//出库
    public static final int FT_PRINT_PRICETAGS         = 0x23;//价签


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
    protected void onCreate(Bundle savedInstanceState) {

//        hideSystemUI();

        handleIntent();

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
            PackageFragment fragment = new PackageFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, packageFragment).show(packageFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_GOODS){
            ScGoodsSkuFragment fragment = new ScGoodsSkuFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, goodsFragment).show(goodsFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_GOODS_PROVIDER){
            ChainGoodsSkuFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = ChainGoodsSkuFragment.newInstance(intent.getExtras());
            }else{
                fragment = ChainGoodsSkuFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, goodsFragment).show(goodsFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_DISTRIBUTION){
            InvRecvOrderSplashFragment fragment = new InvRecvOrderSplashFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, invRecvOrderSplashFragment).show(invRecvOrderSplashFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_INVENTORY_CHECK){
            InvCheckListFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = InvCheckListFragment.newInstance(intent.getExtras());
            }else{
                fragment = InvCheckListFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
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
        else if(serviceType == FT_INVIO_IN){
            CreateInvIoOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                intent.putExtra(CreateInvIoOrderFragment.EXTRA_KEY_ORDER_TYPE,
                        InvIoOrderApi.ORDER_TYPE_IN);
                intent.putExtra(CreateInvIoOrderFragment.EXTRA_KEY_STORE_TYPE,
                        InvIoOrderApi.STORE_TYPE_RETAIL);
                fragment = CreateInvIoOrderFragment.newInstance(intent.getExtras());
            }else{
                fragment = CreateInvIoOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, mBindGoods2TagFragment).show(mBindGoods2TagFragment)
                    .commit();
        }
        else if(serviceType == FT_INVIO_OUT){
            CreateInvIoOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                intent.putExtra(CreateInvIoOrderFragment.EXTRA_KEY_ORDER_TYPE,
                        InvIoOrderApi.ORDER_TYPE_OUT);
                intent.putExtra(CreateInvIoOrderFragment.EXTRA_KEY_STORE_TYPE,
                        InvIoOrderApi.STORE_TYPE_RETAIL);
                fragment = CreateInvIoOrderFragment.newInstance(intent.getExtras());
            }else{
                fragment = CreateInvIoOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, mBindGoods2TagFragment).show(mBindGoods2TagFragment)
                    .commit();
        }
        else if(serviceType == FT_PRINT_PRICETAGS){
            InvLabelFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = InvLabelFragment.newInstance(intent.getExtras());
            }else{
                fragment = InvLabelFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, mBindGoods2TagFragment).show(mBindGoods2TagFragment)
                    .commit();
        }
    }
}
