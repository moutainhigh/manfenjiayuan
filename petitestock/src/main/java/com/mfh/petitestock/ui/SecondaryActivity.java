package com.mfh.petitestock.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.petitestock.R;
import com.mfh.petitestock.ui.fragment.receipt.CreateNewReceiveOrderFragment;
import com.mfh.petitestock.ui.fragment.receipt.DistributionInspectFragment;
import com.mfh.petitestock.ui.fragment.receipt.ReceiveSendOrderFragment;
import com.mfh.petitestock.ui.fragment.receipt.InvSendOrderListFragment;
import com.mfh.petitestock.ui.fragment.receipt.ReceiveMSendOrderFragment;

/**
 * 二级页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SecondaryActivity extends BaseActivity {

    public static final String EXTRA_KEY_FRAGMENT_TYPE = "EXTRA_KEY_FRAGMENT_TYPE";
    public static final int FRAGMENT_TYPE_INV_SENDORDER         = 1;//采购订单
    public static final int FRAGMENT_TYPE_INV_RECVDORDER_CREATE = 2;//新建采购收货单
    public static final int FRAGMENT_TYPE_RECEIVE_M_SENDORDER   = 3;//采购收货单:批发商发货单
    public static final int FRAGMENT_TYPE_DISTRIBUTION_SIGN     = 4;//签收
    public static final int FRAGMENT_TYPE_DISTRIBUTION_INSPECT  = 5;//验货


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
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
            ReceiveSendOrderFragment distributionDetailFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                distributionDetailFragment = ReceiveSendOrderFragment.newInstance(intent.getExtras());
            }else{
                distributionDetailFragment = ReceiveSendOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, distributionDetailFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_DISTRIBUTION_INSPECT){
            DistributionInspectFragment distributionInspectFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                distributionInspectFragment = DistributionInspectFragment.newInstance(intent.getExtras());
            }else{
                distributionInspectFragment = DistributionInspectFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, distributionInspectFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_INV_SENDORDER){
            InvSendOrderListFragment invSendOrderListFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                invSendOrderListFragment = InvSendOrderListFragment.newInstance(intent.getExtras());
            }else{
                invSendOrderListFragment = InvSendOrderListFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, invSendOrderListFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_INV_RECVDORDER_CREATE){
            CreateNewReceiveOrderFragment createNewReceiveOrderFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                createNewReceiveOrderFragment = CreateNewReceiveOrderFragment.newInstance(intent.getExtras());
            }else{
                createNewReceiveOrderFragment = CreateNewReceiveOrderFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, createNewReceiveOrderFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_RECEIVE_M_SENDORDER){
            ReceiveMSendOrderFragment receiveMSendOrderFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                receiveMSendOrderFragment = ReceiveMSendOrderFragment.newInstance(intent.getExtras());
            }else{
                receiveMSendOrderFragment = ReceiveMSendOrderFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, receiveMSendOrderFragment)
                    .commit();
        }
    }
}
