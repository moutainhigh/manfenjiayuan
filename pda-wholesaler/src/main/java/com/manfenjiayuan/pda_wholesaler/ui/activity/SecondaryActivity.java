package com.manfenjiayuan.pda_wholesaler.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.bingshanguxue.pda.IData95Activity;
import com.bingshanguxue.pda.bizz.InvSendOrderListFragment;
import com.bingshanguxue.pda.bizz.invio.InvIoGoodsInspectFragment;
import com.bingshanguxue.pda.bizz.invrecv.InvRecvInspectFragment;
import com.bingshanguxue.pda.bizz.invreturn.InvReturnGoodsInspectFragment;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.CreateInvReceiveOrderFragment;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.InvCheckHistoryFragment;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.InvLossInspectFragment;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.shelves.GoodsShelvesHistoryFragment;
import com.mfh.framework.uikit.BackHandledInterface;
import com.mfh.framework.uikit.base.BaseFragment;

/**
 * 二级页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SecondaryActivity extends IData95Activity implements BackHandledInterface {

    public static final String EXTRA_KEY_FRAGMENT_TYPE = "EXTRA_KEY_FRAGMENT_TYPE";
    public static final int FRAGMENT_TYPE_INV_SENDORDER = 0x01;//门店采购订单
    public static final int FRAGMENT_TYPE_INV_RECVDORDER_CREATE = 0x02;//新建采购收货单
    public static final int FRAGMENT_TYPE_DISTRIBUTION_SIGN = 0x03;//签收
    public static final int FRAGMENT_TYPE_DISTRIBUTION_INSPECT = 0x05;//验货

    public static final int FT_INVIO_INSPECTGOODS = 0x04;//出入库验货
    public static final int FT_INVIO_PICK_GOODS = 0x20;//发货－拣货
    public static final int FT_INVRETURN_INSPECTGOODS = 0x21;//退货验货
    public static final int FT_INVLOSS_INSPECTGOODS = 0x25;//退货验货
    public static final int FRAGMENT_TYPE_STOCKTAKE_HISTORY = 0x22;//盘点记录
    public static final int FRAGMENT_TYPE_STOCK_TAKE = 0x23;//盘点
    public static final int FRAGMENT_TYPE_SHELVESBIND_HISTORY = 0x24;//商品绑定货架


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
    protected boolean finishScannerWhenDestroyEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

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
        } else if (serviceType == FT_INVIO_PICK_GOODS) {
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
        }
        else if (serviceType == FT_INVLOSS_INSPECTGOODS) {
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
        else if(serviceType == FRAGMENT_TYPE_STOCKTAKE_HISTORY){
            InvCheckHistoryFragment stockTakeHistoryFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                stockTakeHistoryFragment = InvCheckHistoryFragment.newInstance(intent.getExtras());
            }else{
                stockTakeHistoryFragment = InvCheckHistoryFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, stockTakeHistoryFragment)
//                    .add(R.id.fragment_container, stockTakeHistoryFragment).show(stockTakeHistoryFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_STOCK_TAKE){
            InvCheckHistoryFragment stockTakeFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                stockTakeFragment = InvCheckHistoryFragment.newInstance(intent.getExtras());
            }else{
                stockTakeFragment = InvCheckHistoryFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, stockTakeFragment)
//                    .add(R.id.fragment_container, stockTakeFragment).show(stockTakeFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_SHELVESBIND_HISTORY){
            GoodsShelvesHistoryFragment goodsShelvesHistoryFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                goodsShelvesHistoryFragment = GoodsShelvesHistoryFragment.newInstance(intent.getExtras());
            }else{
                goodsShelvesHistoryFragment = GoodsShelvesHistoryFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, goodsShelvesHistoryFragment)
//                    .add(R.id.fragment_container, goodsShelvesHistoryFragment).show(goodsShelvesHistoryFragment)
                    .commit();
        }
    }


}
