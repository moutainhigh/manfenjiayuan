package com.manfenjiayuan.mixicook_vip.ui.shopcart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.SettingsItem;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.ARCode;
import com.manfenjiayuan.mixicook_vip.ui.FragmentActivity;
import com.manfenjiayuan.mixicook_vip.ui.address.IReciaddrView;
import com.manfenjiayuan.mixicook_vip.ui.address.ReciaddrPresenter;
import com.manfenjiayuan.mixicook_vip.ui.order.CreateOrderBrief;
import com.manfenjiayuan.mixicook_vip.ui.order.OrderCreateFragment;
import com.manfenjiayuan.mixicook_vip.widget.LabelView2;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.companyInfo.CompanyInfoPresenter;
import com.mfh.framework.api.companyInfo.ICompanyInfoView;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.reciaddr.Reciaddr;
import com.mfh.framework.api.scOrder.ScOrderApi;
import com.mfh.framework.api.scOrder.TransFeeRule;
import com.mfh.framework.api.shoppingCart.Cart;
import com.mfh.framework.api.shoppingCart.CartPack;
import com.mfh.framework.api.shoppingCart.ShoppingCart;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.compound.ProgressView;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 购物车
 * Created by bingshanguxue on 6/28/16.
 */
public class ShopcartFragment extends BaseListFragment<ShoppingCart>
        implements IReciaddrView, ICompanyInfoView, IShopcartView {
    public static final String EXTRA_KEY_COMPANYINFO = "companyInfo";
    public static final String EXTRA_KEY_ADDRESSINFO = "reciaddr";


    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.address_view)
    LabelView2 mAddressView;
    @Bind(R.id.tv_shop_name)
    TextView tvShopName;
    @Bind(R.id.shop_checkbox)
    CheckBox shopCheckbox;
    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private ShopcartGoodsAdapter goodsListAdapter;
    private LinearLayoutManager mRLayoutManager;
    @Bind(R.id.empty_view)
    View emptyView;

    @Bind(R.id.item_transFeeRule)
    SettingsItem transFeeRuleView;
    @Bind(R.id.tv_brief)
    TextView tvBrief;
    @Bind(R.id.button_confirm)
    Button btnConfirm;

    private Reciaddr curAddress = null;
    private CompanyInfo curCompanyInfo = null;//当前店铺
    private ReciaddrPresenter mReciaddrPresenter;
    private CompanyInfoPresenter mCompanyInfoPresenter;
    private ShopcartPresenter mShopcartPresenter;


    private Double transFee = 0D;//配送费
    private TransFeeRule mTransFeeRule = null;//网点配送规则


    public static ShopcartFragment newInstance(Bundle args) {
        ShopcartFragment fragment = new ShopcartFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);
        mReciaddrPresenter = new ReciaddrPresenter(this);
        mCompanyInfoPresenter = new CompanyInfoPresenter(this);
        mShopcartPresenter = new ShopcartPresenter(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_shopcart;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_DEFAULT);
            curAddress = (Reciaddr) args.getSerializable(EXTRA_KEY_ADDRESSINFO);
            curCompanyInfo = (CompanyInfo) args.getSerializable(EXTRA_KEY_COMPANYINFO);
        }

        toolbar.setTitle("购物车");
        if (animType == ANIM_TYPE_NEW_FLOW) {
            toolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        }
        else{
            toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        }
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_selectAll) {
//                    goodsListAdapter.setChecked(true);
//                }
//                return true;
//            }
//        });
//        // Inflate a menu to be displayed in the toolbar
//        toolbar.inflateMenu(R.menu.menu_shopcart);

        shopCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goodsListAdapter.setChecked(shopCheckbox.isChecked());
            }
        });
        initGoodsRecyclerView();

        loadInitStep1();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARCode.ARC_ORDER_CREATE: {
                btnConfirm.setEnabled(true);

                if (resultCode == Activity.RESULT_OK) {
//                    showProgressDialog(ProgressDialog.STATUS_DONE, "下单成功", true);
                    loadInitStep3();

                    //加载配送规则
                    if (mTransFeeRule == null) {
                        getTransFeeRule();
                    }
                }
            }
            break;
            case ARCode.ARC_ADDRESS_LIST: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    curAddress = (Reciaddr) data.getSerializableExtra("reciaddr");
                    DialogUtil.showHint("切换新店铺，更新购物车");
                    loadInitStep2();

                    //加载配送规则
                    if (mTransFeeRule == null) {
                        getTransFeeRule();
                    }
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void reload() {
        super.reload();
        mShopcartPresenter.list(curCompanyInfo.getId(), MfhLoginService.get().getCurrentGuId(), null);

//        mAddressView.setPrimaryText();
//        holder.tvReceiveName.setText(entity.getReceiveName());
//        holder.tvReceivePhone.setText(entity.getReceivePhone());
//        if (entity.getIsDefault() != null && entity.getIsDefault().equals(1)){
//            holder.tvSubName.setText(StringUtils.toSpanned(String.format("<font color=#FE5000>[默认]</font><font color=#a6000000>%s</font>",
//                    entity.getSubName())));
//        }
//        else{
//            holder.tvSubName.setText(entity.getSubName());
//        }
    }

    @OnClick({R.id.button_go2shopping, R.id.item_transFeeRule})
    public void go2Shopping() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    /**
     * 跳转至收货地址
     */
    @OnClick(R.id.address_view)
    public void redirect2MyAddress() {
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_ADDRESS_LIST);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_ADDRESS_LIST);
    }

    /**
     * 下单
     */
    @OnClick(R.id.button_confirm)
    public void redirect2CreateOrder() {
        btnConfirm.setEnabled(false);
        if (curAddress == null) {
            DialogUtil.showHint("请选择收货地址");
//            showProgressDialog(ProgressDialog.STATUS_ERROR, "收货地址不能为空", true);
            btnConfirm.setEnabled(true);
            return;
        }

        CreateOrderBrief createOrderBrief = new CreateOrderBrief();
        createOrderBrief.setBizType(BizType.SC);
        createOrderBrief.setReciaddr(curAddress);
        createOrderBrief.setCompanyInfo(curCompanyInfo);
        createOrderBrief.setPacks(goodsListAdapter.retrieveSelectedPacks());
        createOrderBrief.setTransFee(transFee);
        createOrderBrief.setHumanId(MfhLoginService.get().getCurrentGuId());

        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_ORDER_CREATE);
        extras.putSerializable(OrderCreateFragment.EXTRA_KEY_ORDERBRIEF, createOrderBrief);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_ORDER_CREATE);
    }


    private void initGoodsRecyclerView() {
        mRLayoutManager = new LinearLayoutManager(AppContext.getAppContext());
        mRLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
//        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);
        goodsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                int lastVisibleItem = mRLayoutManager.findLastVisibleItemPosition();
//                int totalItemCount = mRLayoutManager.getItemCount();
//                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
//                // dy>0 表示向下滑动
////                ZLogger.d(String.format("%s %d(%d)", (dy > 0 ? "向上滚动" : "向下滚动"), lastVisibleItem, totalItemCount));
//                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
//                    if (!isLoadingMore) {
//                        loadMore();
//                    }
//                } else if (dy < 0) {
//                    isLoadingMore = false;
//                }
            }
        });

        goodsListAdapter = new ShopcartGoodsAdapter(AppContext.getAppContext(), null);
        goodsListAdapter.setOnAdapterListsner(new ShopcartGoodsAdapter.OnAdapterListener() {
                                                  @Override
                                                  public void onItemClick(View view, int position) {

                                                  }

                                                  @Override
                                                  public void onDataSetChanged() {
                                                      updateOrderWrapperInfo();
                                                  }
                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
        hideProgressDialog();
    }


    /**
     * 加载默认收货地址
     */
    private void loadInitStep1() {
        //加载配送规则
        if (mTransFeeRule == null) {
            getTransFeeRule();
        }

        if (curAddress == null) {
            showProgressDialog(ProgressView.STATUS_PROCESSING, "加载地址...", false);
            mReciaddrPresenter.getDefaultAddrsByHuman(MfhLoginService.get().getCurrentGuId());
        } else {
            if (curCompanyInfo == null) {
                loadInitStep2();
            } else {
                mAddressView.setTitle(String.format("%s/%s",
                        curAddress.getReceiveName(), curAddress.getReceivePhone()));
                mAddressView.setSubTitle(curAddress.getSubName());
                tvShopName.setText(curCompanyInfo.getName());
                loadInitStep3();
            }
        }

    }

    @Override
    public void onIReciaddrViewProcess() {

    }

    @Override
    public void onIReciaddrViewError(String errorMsg) {
        if (StringUtils.isEmpty(errorMsg)) {
            ZLogger.e(errorMsg);
            showProgressDialog(ProgressView.STATUS_ERROR, errorMsg, true);
        } else {
            hideProgressDialog();
        }

        loadInitStep2();
    }

    @Override
    public void onIReciaddrViewSuccess(PageInfo pageInfo, List<Reciaddr> dataList) {

    }

    @Override
    public void onIReciaddrViewSuccess(Reciaddr data) {
        hideProgressDialog();
        curAddress = data;
        loadInitStep2();
    }

    /**
     * 初始化：查询店铺
     */
    private void loadInitStep2() {
        if (curAddress != null) {
            mAddressView.setTitle(String.format("%s/%s",
                    curAddress.getReceiveName(), curAddress.getReceivePhone()));
            mAddressView.setSubTitle(curAddress.getSubName());
//            mNoAddressView.setVisibility(View.GONE);

            showProgressDialog(ProgressView.STATUS_PROCESSING, "加载店铺...", false);
            mCompanyInfoPresenter.findServicedNetsForUserPos(curAddress.getCityID(),
                    String.valueOf(curAddress.getLongitude()),
                    String.valueOf(curAddress.getLatitude()), null);
        } else {
            mAddressView.setTitle("");
            mAddressView.setSubTitle("");
//            mNoAddressView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onICompanyInfoViewProcess() {

    }

    @Override
    public void onICompanyInfoViewError(String errorMsg) {
        if (StringUtils.isEmpty(errorMsg)) {
            ZLogger.e(errorMsg);
            showProgressDialog(ProgressView.STATUS_ERROR, errorMsg, true);
        } else {
            hideProgressDialog();
        }
        loadInitStep3();
    }

    @Override
    public void onICompanyInfoViewSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
        if (dataList != null && dataList.size() > 0) {
            curCompanyInfo = dataList.get(0);
        } else {
            curCompanyInfo = null;
        }
        hideProgressDialog();
        loadInitStep3();
    }

    /**
     * 初始化：查询购物车
     */
    private void loadInitStep3() {
        showProgressDialog(ProgressView.STATUS_PROCESSING, "加载商品...", false);

        if (curCompanyInfo != null) {
            tvShopName.setText(curCompanyInfo.getName());
            mShopcartPresenter.list(curCompanyInfo.getId(), MfhLoginService.get().getCurrentGuId(), null);
        } else {
            tvShopName.setText("");
            mShopcartPresenter.list(null, MfhLoginService.get().getCurrentGuId(), null);
        }
    }

    @Override
    public void onIShopcartViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        onLoadStart();
    }

    @Override
    public void onIShopcartViewError(String errorMsg) {
        if (!StringUtils.isEmpty(errorMsg)) {
            ZLogger.df(errorMsg);
        }

        onLoadFinished();
    }

    @Override
    public void onIShopcartViewSuccess(PageInfo pageInfo, List<ShoppingCart> dataList) {
        try {
            mPageInfo = pageInfo;

            List<CartPack> cartPacks = new ArrayList<>();
            if (dataList != null && dataList.size() > 0) {
                for (ShoppingCart shoppingCart : dataList) {
                    List<CartPack> products = shoppingCart.getProducts();
                    if (products != null) {
                        cartPacks.addAll(products);
                    }
                }
            }
            //第一页，缓存数据
//            if (mPageInfo.getPageNo() == 1) {
//                ZLogger.d("缓存商品收货订单第一页数据");

            if (goodsListAdapter != null) {
                goodsListAdapter.setEntityList(cartPacks);
            }
//            } else {
//                if (goodsListAdapter != null) {
//                    goodsListAdapter.appendEntityList(cartPacks);
//                }
//            }

            onLoadFinished();
        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载商品收货订单失败: %s", ex.toString()));
            onLoadFinished();
        }
    }


    /**
     * 获取网点的配送规则
     */
    private void getTransFeeRule() {
        NetCallBack.NetTaskCallBack responseRC = new NetCallBack.NetTaskCallBack<TransFeeRule,
                NetProcessor.Processor<TransFeeRule>>(
                new NetProcessor.Processor<TransFeeRule>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                    {"code":"0","msg":"新增成功!","version":"1","data":{"val":"1090753;17.0"}}
                        TransFeeRule temp = null;
                        try {
                            if (rspData != null) {
                                RspBean<TransFeeRule> retValue = (RspBean<TransFeeRule>) rspData;
                                temp = retValue.getValue();
                            }
                        } catch (Exception e) {
                            ZLogger.ef(e.toString());
                        }
                        refreshTransFeeRule(temp);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("获取网点的配送规则失败：" + errMsg);
                        refreshTransFeeRule(null);
                    }
                }
                , TransFeeRule.class
                , MfhApplication.getAppContext()) {
        };
        ScOrderApi.getTransFeeRule(MfhLoginService.get().getCurOfficeId(), responseRC);
    }

    /**
     * 刷新配送规则
     */
    private void refreshTransFeeRule(TransFeeRule rule) {
        mTransFeeRule = rule;
        updateOrderWrapperInfo();
    }

    /**
     * 刷新订单信息
     */
    private void updateOrderWrapperInfo() {
        Double amount = 0D;
        List<CartPack> packs = goodsListAdapter.retrieveSelectedPacks();
        if (packs != null && packs.size() > 0) {
            if (packs.size() == goodsListAdapter.getItemCount()) {
                shopCheckbox.setChecked(true);
            } else {
                shopCheckbox.setChecked(false);
            }

            for (CartPack pack : packs) {
                Cart cart = pack.getCart();
                amount += cart.getAmount();
            }

            if (mTransFeeRule != null) {
//                transFeeRuleView.setTitle(String.format("配送费 "));
                Double limit = mTransFeeRule.getOrderMinLimit() - amount;
                if (limit > 0) {
                    Spanned spanned = StringUtils.toSpanned(String
                            .format("<font color=#FE5000>差￥%.2f起送，去凑单</font>",
                                    limit));
                    ZLogger.d(spanned.toString());
                    transFee = mTransFeeRule.getOrderTransFee();
                    transFeeRuleView.setSubTitle(spanned);
                    transFeeRuleView.setVisibility(View.VISIBLE);
                    btnConfirm.setEnabled(false);
                } else {
                    Double free = mTransFeeRule.getOrderNoTransFeeLimit() - amount;
                    if (free > 0) {
                        Spanned spanned = StringUtils.toSpanned(String
                                .format("<font color=#9E9E9E>差￥%.2f免配送费，去凑单</font>",
                                        free));
                        ZLogger.d(spanned.toString());
                        transFee = mTransFeeRule.getOrderTransFee();
                        transFeeRuleView.setSubTitle(spanned);
                        transFeeRuleView.setVisibility(View.VISIBLE);
                        btnConfirm.setEnabled(true);
                    } else {
                        ZLogger.d("免配送费");
                        transFee = 0D;
                        transFeeRuleView.setVisibility(View.GONE);
                        btnConfirm.setEnabled(true);
                    }
                }
            } else {
                ZLogger.d("未设置配送规则");
                transFee = 0D;
                transFeeRuleView.setVisibility(View.GONE);
                btnConfirm.setEnabled(true);
            }
        } else {
            ZLogger.d("未选中商品");
            transFee = 0D;
            shopCheckbox.setChecked(false);
            transFeeRuleView.setVisibility(View.GONE);
            btnConfirm.setEnabled(false);
        }
        Spanned spanned = StringUtils.toSpanned(String
                .format("<font color=#000000>合计</font>" +
                                "<font color=#9E9E9E>(不含运费)：</font>" +
                                "<font color=#FE5000>￥%.2f</font>",
                        amount));
        tvBrief.setText(spanned);
    }

}
