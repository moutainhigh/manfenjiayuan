package com.manfenjiayuan.pda_wholesaler.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.bingshanguxue.pda.IData95Activity;
import com.bingshanguxue.pda.bizz.invcheck.ShelvesFragment;
import com.bingshanguxue.pda.bizz.office.OfficeListFragment;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.CreateInvIoOrderFragment;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.CreateInvLossOrderFragment;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.CreateInvReturnOrderFragment;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.CreateInvSendIoOrderFragment;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.goods.InvSkuGoodsFragment;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.invcheck.InvCheckListFragment;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.shelves.BindGoods2ShelvesFragment;
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
    public static final int FRAGMENT_TYPE_PACKAGE = 0x1;
    public static final int FT_CREATE_INVIOORDER_BYFINDORDER = 0x02;//拣货发货
    public static final int FRAGMENT_TYPE_INVENTORY_CHECK = 0x06;//盘点订单列表
    public static final int FT_WHOLESALER_GOODS             = 0x07;//商品
    public static final int FT_WHOLESALER_GOODSSHELVES      = 0x08;//货架绑定商品
	public static final int FT_CREATE_INV_RETURNORDER      = 0x11;//退货
    public static final int FT_CREATE_INV_LOSSORDER      = 0x12;//报损
    public static final int FT_INVIO_IN         = 0x21;//入库
    public static final int FT_INVIO_OUT         = 0x22;//出库
    public static final int FT_OFFICE_LIST = 0x24;//网店租户列表
    public static final int FT_SHELVES_LIST = 0x25;//盘点区域


    /**
     * 0: 快递代收
     * */
    private int fragmentType = 0;

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
        handleIntent();

        super.onCreate(savedInstanceState);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
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

            fragmentType = intent.getIntExtra(EXTRA_KEY_SERVICE_TYPE, FRAGMENT_TYPE_NONE);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     * */
    private void initFragments(){
        if(fragmentType == FRAGMENT_TYPE_INVENTORY_CHECK){
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

        else if(fragmentType == FT_WHOLESALER_GOODS){
            InvSkuGoodsFragment fragment = new InvSkuGoodsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, commodityFragment).show(commodityFragment)
                    .commit();
        }
        else if(fragmentType == FT_WHOLESALER_GOODSSHELVES){
            BindGoods2ShelvesFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = BindGoods2ShelvesFragment.newInstance(intent.getExtras());
            }else{
                fragment = BindGoods2ShelvesFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, mBindGoods2ShelvesFragment).show(mBindGoods2ShelvesFragment)
                    .commit();
        }
        else if (fragmentType == FT_CREATE_INVIOORDER_BYFINDORDER) {
            CreateInvSendIoOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = CreateInvSendIoOrderFragment.newInstance(intent.getExtras());
            } else {
                fragment = CreateInvSendIoOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
		else if(fragmentType == FT_CREATE_INV_LOSSORDER){
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
        }else if(fragmentType == FT_INVIO_IN){
            CreateInvIoOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                intent.putExtra(CreateInvIoOrderFragment.EXTRA_KEY_ORDER_TYPE,
                        InvIoOrderApi.ORDER_TYPE_IN);
                intent.putExtra(CreateInvIoOrderFragment.EXTRA_KEY_STORE_TYPE,
                        InvIoOrderApi.STORE_TYPE_WHOLESALE);
                fragment = CreateInvIoOrderFragment.newInstance(intent.getExtras());
            }else{
                fragment = CreateInvIoOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, mBindGoods2TagFragment).show(mBindGoods2TagFragment)
                    .commit();
        }
        else if(fragmentType == FT_INVIO_OUT){
            CreateInvIoOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                intent.putExtra(CreateInvIoOrderFragment.EXTRA_KEY_ORDER_TYPE,
                        InvIoOrderApi.ORDER_TYPE_OUT);
                intent.putExtra(CreateInvIoOrderFragment.EXTRA_KEY_STORE_TYPE,
                        InvIoOrderApi.STORE_TYPE_WHOLESALE);
                fragment = CreateInvIoOrderFragment.newInstance(intent.getExtras());
            }else{
                fragment = CreateInvIoOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, mBindGoods2TagFragment).show(mBindGoods2TagFragment)
                    .commit();
        }

        else if(fragmentType == FT_CREATE_INV_RETURNORDER){
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
        }else if(fragmentType == FT_OFFICE_LIST){
            OfficeListFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = OfficeListFragment.newInstance(intent.getExtras());
            }else{
                fragment = OfficeListFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, goodsShelvesHistoryFragment).show(goodsShelvesHistoryFragment)
                    .commit();
        }
        else if(fragmentType == FT_SHELVES_LIST){
            ShelvesFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null){
                fragment = ShelvesFragment.newInstance(intent.getExtras());
            }else{
                fragment = ShelvesFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
//                    .add(R.id.fragment_container, goodsShelvesHistoryFragment).show(goodsShelvesHistoryFragment)
                    .commit();
        }
    }
}
