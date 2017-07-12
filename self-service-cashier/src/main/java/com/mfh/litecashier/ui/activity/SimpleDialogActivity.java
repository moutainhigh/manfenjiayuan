package com.mfh.litecashier.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.R;
import com.mfh.litecashier.components.CashQuotaFragment;
import com.mfh.litecashier.components.OneKeyFeedbackFragment;
import com.mfh.litecashier.components.customer.CustomerTransactionFragment;
import com.mfh.litecashier.ui.fragment.dailysettle.DailySettleFragment;
import com.mfh.litecashier.ui.fragment.dailysettle.HandoverFragment;
import com.mfh.litecashier.ui.fragment.goods.GoodsSalesFragment;
import com.mfh.litecashier.ui.fragment.goods.ScSkuGoodsStoreInFragment;
import com.mfh.litecashier.ui.fragment.inventory.GreateScSkuGoodsFragment;
import com.mfh.litecashier.ui.fragment.orderflow.PayHistoryFragment;
import com.mfh.litecashier.components.pickup.PickupFragment;
import com.mfh.litecashier.ui.fragment.prepareorder.PrepareAbleOrdersFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseGoodsDetailFragment;
import com.mfh.litecashier.ui.fragment.purchase.SelectInvRecvOrderFragment;
import com.mfh.litecashier.ui.fragment.purchase.SelectWholesalerWithTenantFragment;
import com.mfh.litecashier.ui.fragment.tenant.TenantCategoryListFragment;
import com.mfh.litecashier.ui.goodsflow.BackendCategoryListFragment;
import com.mfh.litecashier.ui.goodsflow.GoodsFlowFragment;
import com.mfh.litecashier.components.reconcile.ProdLineListFragment;
import com.mfh.litecashier.components.reconcile.ReconcileFragment;

import butterknife.BindView;


/**
 * 对话框
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SimpleDialogActivity extends BaseActivity {
    public static final String EXTRA_KEY_SERVICE_TYPE = "serviceType";

    public static final int FRAGMENT_TYPE_CREATE_PURCHASE_GOODS = 0x01;//采购商品－新增商品
    public static final int FRAGMENT_TYPE_GENERATE_PURCHASE_GOODS = 0x02;//采购商品－新增商品
    public static final int FRAGMENT_TYPE_SELECT_INV_RECVORDER = 0x03;//选择收货单
    public static final int FRAGMENT_TYPE_PURCHASE_GOODSDETAIL = 0x04;//采购商品详情
    public static final int FRAGMENT_TYPE_SELECT_WHOLESALER_TENANT = 0x05;//选择批发商&门店
    public static final int FRAGMENT_TYPE_DAILY_SETTLE = 0x06;//日结
    public static final int FRAGMENT_TYPE_HANDOVER = 0x07;//交接班
    public static final int FT_CANARY_CASH_QUOTA = 0x08;//现金授权
    public static final int FT_PAY_HISTORY = 0x09;//支付记录
    public static final int FT_GOODS_SALESHISTORY = 0x10;//商品销量记录

    public static final int FT_TENANT_POSCATEGORYLIST = 0x13;//租户pos前台类目
    public static final int FT_GOODS_FLOW = 0x14;//商品流水
    public static final int FT_BACKEND_CATEGORY = 0x15;//后台类目
    public static final int FT_PREPAREABLE_ORDER = 0x21;//买手接单
    public static final int FT_RECONCILE = 0x22;//对账
    public static final int FT_PROD_LINE_LIST = 0x23;//产品线列表


    public static final int FT_CUSTOMER_TRANSACTION = 0x33;//会员交易查询
    public static final int FT_CUSTOMER_PICKUP = 0x34;//团购订单自提



    public static final int FT_ONEKEY_FEEDBACK = 0x41;//一键反馈


    private int serviceType = 0;


    public static final String EXTRA_KEY_DIALOG_TYPE = "dialogType";
    public static final int DT_NORMAL = 0x01;//正常
    public static final int DT_MIDDLE = 0x02;//中等
    public static final int DT_VERTICIAL_FULLSCREEN = 0x03;//全屏

    private int dialogType = DT_NORMAL;

    public static final String EXTRA_KEY_TITLE = "title";

    @BindView(R.id.fragment_container)
    FrameLayout frameLayout;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, SimpleDialogActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_simple_dialog;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected boolean isFullscreenEnabled() {
        return true;
    }

    @Override
    protected void initViews() {
        super.initViews();

        ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
        if (dialogType == DT_NORMAL) {
            layoutParams.width = DensityUtil.dip2px(this, 600);
        } else if (dialogType == DT_MIDDLE) {
            layoutParams.width = DensityUtil.dip2px(this,
                    getResources().getDimension(R.dimen.mf_simple_dialog_width));
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else if (dialogType == DT_VERTICIAL_FULLSCREEN) {
            layoutParams.width = DensityUtil.dip2px(this, 600);
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        frameLayout.setLayoutParams(layoutParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

//        startService(new Intent(this, Utf7ImeService.class));
        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
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
//                this.setTheme(R.style.activity_new_task);
            }

            serviceType = intent.getIntExtra(EXTRA_KEY_SERVICE_TYPE, -1);
            dialogType = intent.getIntExtra(EXTRA_KEY_DIALOG_TYPE, DT_MIDDLE);
//            ZLogger.d("serviceType=" + serviceType);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     */
    private void initFragments() {
        switch (serviceType) {
            case FRAGMENT_TYPE_CREATE_PURCHASE_GOODS: {
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
            }
            break;
            case FRAGMENT_TYPE_GENERATE_PURCHASE_GOODS: {
                GreateScSkuGoodsFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = GreateScSkuGoodsFragment.newInstance(intent.getExtras());
                } else {
                    fragment = GreateScSkuGoodsFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FRAGMENT_TYPE_HANDOVER: {
                HandoverFragment handoverFragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    handoverFragment = HandoverFragment.newInstance(intent.getExtras());
                } else {
                    handoverFragment = HandoverFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, handoverFragment)
                        .commit();
            }
            break;
            case FRAGMENT_TYPE_DAILY_SETTLE: {
                DailySettleFragment dailySettleFragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    dailySettleFragment = DailySettleFragment.newInstance(intent.getExtras());
                } else {
                    dailySettleFragment = DailySettleFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, dailySettleFragment)
                        .commit();
            }
            break;
            case FRAGMENT_TYPE_SELECT_INV_RECVORDER: {
                SelectInvRecvOrderFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = SelectInvRecvOrderFragment.newInstance(intent.getExtras());
                } else {
                    fragment = SelectInvRecvOrderFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FRAGMENT_TYPE_PURCHASE_GOODSDETAIL: {
                PurchaseGoodsDetailFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = PurchaseGoodsDetailFragment.newInstance(intent.getExtras());
                } else {
                    fragment = PurchaseGoodsDetailFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FRAGMENT_TYPE_SELECT_WHOLESALER_TENANT: {
                SelectWholesalerWithTenantFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = SelectWholesalerWithTenantFragment.newInstance(intent.getExtras());
                } else {
                    fragment = SelectWholesalerWithTenantFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_CANARY_CASH_QUOTA: {
                CashQuotaFragment fragment = new CashQuotaFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_PAY_HISTORY: {
                PayHistoryFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = PayHistoryFragment.newInstance(intent.getExtras());
                } else {
                    fragment = PayHistoryFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_GOODS_SALESHISTORY: {
                ZLogger.d("准备跳转到销量页面");
                try {
                    GoodsSalesFragment fragment;
                    Intent intent = this.getIntent();
                    if (intent != null) {
                        fragment = GoodsSalesFragment.newInstance(intent.getExtras());
                    } else {
                        fragment = GoodsSalesFragment.newInstance(null);
                    }
                    getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                } catch (Exception e) {
                    ZLogger.e(e.toString());
                    e.printStackTrace();
                }
            }
            break;
            case FT_CUSTOMER_TRANSACTION: {
                CustomerTransactionFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = CustomerTransactionFragment.newInstance(intent.getExtras());
                } else {
                    fragment = CustomerTransactionFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_CUSTOMER_PICKUP: {
                PickupFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = PickupFragment.newInstance(intent.getExtras());
                } else {
                    fragment = PickupFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_TENANT_POSCATEGORYLIST: {
                TenantCategoryListFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = TenantCategoryListFragment.newInstance(intent.getExtras());
                } else {
                    fragment = TenantCategoryListFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;

            case FT_PREPAREABLE_ORDER: {
                PrepareAbleOrdersFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = PrepareAbleOrdersFragment.newInstance(intent.getExtras());
                } else {
                    fragment = PrepareAbleOrdersFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;

            case FT_BACKEND_CATEGORY: {
                BackendCategoryListFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = BackendCategoryListFragment.newInstance(intent.getExtras());
                } else {
                    fragment = BackendCategoryListFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_GOODS_FLOW: {
                GoodsFlowFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = GoodsFlowFragment.newInstance(intent.getExtras());
                } else {
                    fragment = GoodsFlowFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_ONEKEY_FEEDBACK: {
                OneKeyFeedbackFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = OneKeyFeedbackFragment.newInstance(intent.getExtras());
                } else {
                    fragment = OneKeyFeedbackFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_RECONCILE: {
                ReconcileFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = ReconcileFragment.newInstance(intent.getExtras());
                } else {
                    fragment = ReconcileFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_PROD_LINE_LIST: {
                ProdLineListFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = ProdLineListFragment.newInstance(intent.getExtras());
                } else {
                    fragment = ProdLineListFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            default:
                break;
        }
    }
}
