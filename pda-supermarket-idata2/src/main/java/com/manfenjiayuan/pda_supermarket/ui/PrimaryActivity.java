package com.manfenjiayuan.pda_supermarket.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.bingshanguxue.pda.IData95Activity;
import com.bingshanguxue.pda.bizz.goods.ScGoodsSkuFragment;
import com.bingshanguxue.pda.bizz.goods.ScSkuGoodsStoreInFragment;
import com.bingshanguxue.pda.bizz.invcheck.ShelvesFragment;
import com.bingshanguxue.pda.bizz.office.OfficeListFragment;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.buyer.AcceptPrepareOrdersFragment;
import com.manfenjiayuan.pda_supermarket.ui.buyer.BuyPrepareOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.buyer.BuyScOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.common.MyFragment;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.pay.instock.InstockPayFragment;
import com.manfenjiayuan.pda_supermarket.ui.rider.InstockOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.rider.InstockScOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.rider.SendOrderFragmnt;
import com.manfenjiayuan.pda_supermarket.ui.store.BindGoods2TagFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.CreateInvReturnOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.InvLabelFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.InvSendOrderNewFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.PackageFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.cashier.CashierFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.groupBuy.GroupBuyActivityFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.invIo.CreateInvIoOrderFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.invIo.InvIoConvertFromFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.invRecv.InvRecvOrderConvertFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.invRecv.InvRecvOrderNewFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.invcheck.InvCheckListFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.invloss.InvLossFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.invloss.InvLossOrderListFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.invloss.InvLossStockFragment;
import com.manfenjiayuan.pda_supermarket.ui.store.groupBuy.GroupBuyOrdersFragment;
import com.mfh.framework.api.invIoOrder.InvIoOrderApi;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.uikit.BackHandledInterface;
import com.mfh.framework.uikit.base.BaseFragment;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PrimaryActivity extends IData95Activity implements BackHandledInterface {

    public static final String EXTRA_KEY_SERVICE_TYPE = "EXTRA_KEY_SERVICE_TYPE";

    //通用
    public static final int FRAGMENT_TYPE_NONE = 0x00;
    public static final int FT_MY = 0x01;//我的
    //门店
    public static final int FRAGMENT_TYPE_PACKAGE = 0x03;
    public static final int FT_INV_CHECKORDER_STOCKTAKE = 0x04;//盘点订单列表
    public static final int FT_INV_RECVORDER_NEW = 0x05;//新建采购收货单
    public static final int FT_INV_RECVORDER_CONVERT = 0x06;//收货转换
    public static final int FT_CREATE_INV_RETURNORDER = 0x07;//退货
    public static final int FT_INV_LOSSORDER_NEW = 0x08;//报损
    public static final int FT_INV_LOSSORDER_LIST = 0x09;//报损盘点
    public static final int FT_INV_LOSSORDER_STOCKTAKE = 0x0A;//报损盘点
    public static final int FT_INV_CONVERT = 0x0B;//库存转换
    public static final int FT_INVIO_IN = 0x0C;//入库
    public static final int FT_INVIO_OUT = 0x0D;//出库
    public static final int FT_OFFICE_LIST = 0x0E;//网店租户列表
    public static final int FT_SHELVES_LIST = 0x0F;//盘点区域
    public static final int FT_STORE_IN = 0x10;//商品建档
    public static final int FT_CASHIER = 0x11;//收银
    public static final int FRAGMENT_TYPE_GOODS = 0x12;
    public static final int FT_SCORDER_DETAIL = 0x13;//商城订单详情
    public static final int FT_BIND_GOODS_2_TAGS = 0x14;//货架绑定商品
    public static final int FT_PRINT_PRICETAGS = 0x15;//价签
    public static final int FT_INV_SENDORDER_NEW = 0x16;//订货
    public static final int FT_INV_PICKUP_ORDER = 0x17;//自提订单
    public static final int FT_INV_PICKUP_ORDER_CUSTOMERS = 0x18;//自提订单-顾客列表
    //买手
    public static final int FT_BUY_SCORDER = 0x20;//买手——线上订单
    public static final int FT_BUY_PREAPARE = 0x21;//买手-组货
    public static final int FT_INVFIND_CREATE = 0x22;//新建拣货单
    //骑手
    public static final int FT_INSTOCK_SCORDER = 0x30;//骑手——线上订单
    public static final int FT_INV_RIDER_SEND = 0x31;//骑手-揽件
    public static final int FT_INV_RIDER_INSTOCK = 0x32;//骑手-妥投
    public static final int FT_INV_RIDER_INSTOCK_PAY = 0x33;//骑手-妥投—补差价


    /**
     * 0: 快递代收
     */
    private int fragmentType = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, PrimaryActivity.class);
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
    public void onBackPressed() {
//        super.onBackPressed();

        DeviceUtils.hideSoftInput(this);

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

            fragmentType = intent.getIntExtra(EXTRA_KEY_SERVICE_TYPE, FRAGMENT_TYPE_NONE);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     */
    private void initFragments() {
////        fragments = new Fragment[]{serviceFragment, surroundFragment};
//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.fragment_container, stockInFragment)
//                .add(R.id.fragment_container, stockOutFragment).hide(stockInFragment).hide(stockOutFragment)
//                .commit();

        if (fragmentType == FRAGMENT_TYPE_PACKAGE) {
            PackageFragment fragment = new PackageFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FRAGMENT_TYPE_GOODS) {
            ScGoodsSkuFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = ScGoodsSkuFragment.newInstance(intent.getExtras());
            } else {
                fragment = ScGoodsSkuFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_RECVORDER_NEW) {
            InvRecvOrderNewFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvRecvOrderNewFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvRecvOrderNewFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_RECVORDER_CONVERT) {
            InvRecvOrderConvertFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvRecvOrderConvertFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvRecvOrderConvertFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_CHECKORDER_STOCKTAKE) {
            InvCheckListFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvCheckListFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvCheckListFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_BIND_GOODS_2_TAGS) {
            BindGoods2TagFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = BindGoods2TagFragment.newInstance(intent.getExtras());
            } else {
                fragment = BindGoods2TagFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_CREATE_INV_RETURNORDER) {
            CreateInvReturnOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = CreateInvReturnOrderFragment.newInstance(intent.getExtras());
            } else {
                fragment = CreateInvReturnOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_LOSSORDER_NEW) {
            InvLossFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvLossFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvLossFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_LOSSORDER_LIST) {
            InvLossOrderListFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvLossOrderListFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvLossOrderListFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_LOSSORDER_STOCKTAKE) {
            InvLossStockFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvLossStockFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvLossStockFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_CONVERT) {
            InvIoConvertFromFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvIoConvertFromFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvIoConvertFromFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INVIO_IN) {
            CreateInvIoOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                intent.putExtra(CreateInvIoOrderFragment.EXTRA_KEY_ORDER_TYPE,
                        InvIoOrderApi.ORDER_TYPE_IN);
                intent.putExtra(CreateInvIoOrderFragment.EXTRA_KEY_STORE_TYPE,
                        InvIoOrderApi.STORE_TYPE_RETAIL);
                fragment = CreateInvIoOrderFragment.newInstance(intent.getExtras());
            } else {
                fragment = CreateInvIoOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INVIO_OUT) {
            CreateInvIoOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                intent.putExtra(CreateInvIoOrderFragment.EXTRA_KEY_ORDER_TYPE,
                        InvIoOrderApi.ORDER_TYPE_OUT);
                intent.putExtra(CreateInvIoOrderFragment.EXTRA_KEY_STORE_TYPE,
                        InvIoOrderApi.STORE_TYPE_RETAIL);
                fragment = CreateInvIoOrderFragment.newInstance(intent.getExtras());
            } else {
                fragment = CreateInvIoOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_PRINT_PRICETAGS) {
            InvLabelFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvLabelFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvLabelFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_OFFICE_LIST) {
            OfficeListFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = OfficeListFragment.newInstance(intent.getExtras());
            } else {
                fragment = OfficeListFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_SHELVES_LIST) {
            ShelvesFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = ShelvesFragment.newInstance(intent.getExtras());
            } else {
                fragment = ShelvesFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_STORE_IN) {
            ScSkuGoodsStoreInFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = ScSkuGoodsStoreInFragment.newInstance(intent.getExtras());
            } else {
                fragment = ScSkuGoodsStoreInFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_CASHIER) {
            CashierFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = CashierFragment.newInstance(intent.getExtras());
            } else {
                fragment = CashierFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_MY) {
            MyFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = MyFragment.newInstance(intent.getExtras());
            } else {
                fragment = MyFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_BUY_SCORDER) {
            BuyScOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = BuyScOrderFragment.newInstance(intent.getExtras());
            } else {
                fragment = BuyScOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_BUY_PREAPARE) {
            BuyPrepareOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = BuyPrepareOrderFragment.newInstance(intent.getExtras());
            } else {
                fragment = BuyPrepareOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INVFIND_CREATE) {
            AcceptPrepareOrdersFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = AcceptPrepareOrdersFragment.newInstance(intent.getExtras());
            } else {
                fragment = AcceptPrepareOrdersFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_RIDER_SEND) {
            SendOrderFragmnt fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = SendOrderFragmnt.newInstance(intent.getExtras());
            } else {
                fragment = SendOrderFragmnt.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_RIDER_INSTOCK) {
            InstockOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InstockOrderFragment.newInstance(intent.getExtras());
            } else {
                fragment = InstockOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_RIDER_INSTOCK_PAY) {
            InstockPayFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InstockPayFragment.newInstance(intent.getExtras());
            } else {
                fragment = InstockPayFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INSTOCK_SCORDER) {
            InstockScOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InstockScOrderFragment.newInstance(intent.getExtras());
            } else {
                fragment = InstockScOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_SCORDER_DETAIL) {
            ScOrderFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = ScOrderFragment.newInstance(intent.getExtras());
            } else {
                fragment = ScOrderFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_SENDORDER_NEW) {
            InvSendOrderNewFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvSendOrderNewFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvSendOrderNewFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_PICKUP_ORDER) {
            GroupBuyActivityFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = GroupBuyActivityFragment.newInstance(intent.getExtras());
            } else {
                fragment = GroupBuyActivityFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (fragmentType == FT_INV_PICKUP_ORDER_CUSTOMERS) {
            GroupBuyOrdersFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = GroupBuyOrdersFragment.newInstance(intent.getExtras());
            } else {
                fragment = GroupBuyOrdersFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
