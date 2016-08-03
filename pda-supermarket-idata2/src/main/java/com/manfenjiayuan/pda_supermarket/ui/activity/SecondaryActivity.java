package com.manfenjiayuan.pda_supermarket.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.bingshanguxue.pda.IData95Activity;
import com.bingshanguxue.pda.bizz.InvRecvInspectFragment;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.invconvert.InvConvertToFragment;
import com.manfenjiayuan.pda_supermarket.ui.invio.InvIoGoodsInspectFragment;
import com.manfenjiayuan.pda_supermarket.ui.invloss.InvLossInspectFragment;
import com.manfenjiayuan.pda_supermarket.ui.invreceive.CreateInvReceiveOrderFragment;
import com.bingshanguxue.pda.bizz.InvSendOrderListFragment;
import com.manfenjiayuan.pda_supermarket.ui.invreturn.InvReturnGoodsInspectFragment;
import com.mfh.framework.uikit.BackHandledInterface;
import com.mfh.framework.uikit.base.BaseFragment;

/**
 * 二级页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SecondaryActivity extends IData95Activity implements BackHandledInterface {

    public static final String EXTRA_KEY_FRAGMENT_TYPE = "EXTRA_KEY_FRAGMENT_TYPE";
    public static final int FRAGMENT_TYPE_DISTRIBUTION_INSPECT = 2;//验货
    public static final int FT_INVRETURN_INSPECTGOODS = 0x03;//退货验货
    public static final int FT_INVIO_INSPECTGOODS = 0x04;//出入库验货
    public static final int FRAGMENT_TYPE_INV_SENDORDER = 0x06;//采购订单列表
    public static final int FRAGMENT_TYPE_INV_RECVDORDER_CREATE = 0x07;//新建采购收货单
    public static final int FT_SKUGOODS_CONVERT_TO = 0x08;//转换成商品
    public static final int FT_INVLOSS_INSPECTGOODS = 0x09;//报损验货

    /**
     * 0: 快递代收
     */
    private int serviceType = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, SecondaryActivity.class);
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

        if (mBackHandedFragment == null || !mBackHandedFragment.onBackPressed()) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                super.onBackPressed();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    private BaseFragment mBackHandedFragment;

    @Override
    public void setSelectedFragment(BaseFragment selectedFragment) {
        this.mBackHandedFragment = selectedFragment;
    }

    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
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
     */
    private void initFragments() {
        if (serviceType == FRAGMENT_TYPE_DISTRIBUTION_INSPECT) {
            InvRecvInspectFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvRecvInspectFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvRecvInspectFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (serviceType == FT_INVRETURN_INSPECTGOODS) {
            InvReturnGoodsInspectFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvReturnGoodsInspectFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvReturnGoodsInspectFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (serviceType == FT_INVIO_INSPECTGOODS) {
            InvIoGoodsInspectFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvIoGoodsInspectFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvIoGoodsInspectFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (serviceType == FRAGMENT_TYPE_INV_SENDORDER) {
            InvSendOrderListFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvSendOrderListFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvSendOrderListFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (serviceType == FRAGMENT_TYPE_INV_RECVDORDER_CREATE) {
            CreateInvReceiveOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = CreateInvReceiveOrderFragment.newInstance(intent.getExtras());
            } else {
                fragment = CreateInvReceiveOrderFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (serviceType == FT_SKUGOODS_CONVERT_TO) {
            InvConvertToFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvConvertToFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvConvertToFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (serviceType == FT_INVLOSS_INSPECTGOODS) {
            InvLossInspectFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvLossInspectFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvLossInspectFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, commodityApplyFragment).show(commodityApplyFragment)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
