package com.manfenjiayuan.pda_supermarket.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.scanner.IData95Activity;
import com.manfenjiayuan.pda_supermarket.ui.invconvert.InvConvertToFragment;
import com.manfenjiayuan.pda_supermarket.ui.invreturn.InvReturnGoodsInspectFragment;
import com.manfenjiayuan.pda_supermarket.ui.fragment.receipt.CreateNewReceiveOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.fragment.receipt.DistributionInspectFragment;
import com.manfenjiayuan.pda_supermarket.ui.fragment.receipt.InvSendOrderListFragment;
import com.manfenjiayuan.pda_supermarket.ui.fragment.receipt.ReceiveMSendOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.fragment.receipt.ReceiveSendOrderFragment;
import com.mfh.framework.uikit.BackHandledInterface;
import com.mfh.framework.uikit.base.BaseFragment;

/**
 * 二级页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SecondaryActivity extends IData95Activity implements BackHandledInterface {

    public static final String EXTRA_KEY_FRAGMENT_TYPE = "EXTRA_KEY_FRAGMENT_TYPE";
    public static final int FRAGMENT_TYPE_DISTRIBUTION_SIGN     = 4;//签收
    public static final int FRAGMENT_TYPE_DISTRIBUTION_INSPECT  = 5;//验货
    public static final int FT_INVRETURN_INSPECTGOODS  = 6;//退货验货

    public static final int FT_RECEIVEORDER_INVIOORDER          = 11;//发货单收货
    public static final int FRAGMENT_TYPE_INV_SENDORDER         = 12;//采购订单列表
    public static final int FRAGMENT_TYPE_INV_RECVDORDER_CREATE = 13;//新建采购收货单
    public static final int FT_SKUGOODS_CONVERT_TO = 14;//转换成商品


    /**
     * 0: 快递代收
     * */
    private int serviceType = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, SecondaryActivity.class);
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
//            else{
//                this.setTheme(R.style.AppTheme_NoTitleBar);
//            }

            serviceType = intent.getIntExtra(EXTRA_KEY_FRAGMENT_TYPE, -1);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     * */
    private void initFragments(){
        if (serviceType == FRAGMENT_TYPE_DISTRIBUTION_SIGN){
            ReceiveSendOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = ReceiveSendOrderFragment.newInstance(intent.getExtras());
            }else{
                fragment = ReceiveSendOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_DISTRIBUTION_INSPECT){
            DistributionInspectFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = DistributionInspectFragment.newInstance(intent.getExtras());
            }else{
                fragment = DistributionInspectFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
        else if(serviceType == FT_INVRETURN_INSPECTGOODS){
            InvReturnGoodsInspectFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = InvReturnGoodsInspectFragment.newInstance(intent.getExtras());
            }else{
                fragment = InvReturnGoodsInspectFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
        else if(serviceType == FT_RECEIVEORDER_INVIOORDER){
            ReceiveMSendOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = ReceiveMSendOrderFragment.newInstance(intent.getExtras());
            }else{
                fragment = ReceiveMSendOrderFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_INV_SENDORDER){
            InvSendOrderListFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = InvSendOrderListFragment.newInstance(intent.getExtras());
            }else{
                fragment = InvSendOrderListFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_INV_RECVDORDER_CREATE){
            CreateNewReceiveOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = CreateNewReceiveOrderFragment.newInstance(intent.getExtras());
            }else{
                fragment = CreateNewReceiveOrderFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
        else if(serviceType == FT_SKUGOODS_CONVERT_TO){
            InvConvertToFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = InvConvertToFragment.newInstance(intent.getExtras());
            }else{
                fragment = InvConvertToFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
