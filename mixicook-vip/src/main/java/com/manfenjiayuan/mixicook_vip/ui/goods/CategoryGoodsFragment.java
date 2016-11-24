package com.manfenjiayuan.mixicook_vip.ui.goods;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.manfenjiayuan.business.presenter.ScGoodsSkuPresenter;
import com.manfenjiayuan.business.view.IScGoodsSkuView;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.model.CartBrief;
import com.manfenjiayuan.mixicook_vip.ui.ARCode;
import com.manfenjiayuan.mixicook_vip.ui.FragmentActivity;
import com.manfenjiayuan.mixicook_vip.ui.home.ShopcartEvent;
import com.manfenjiayuan.mixicook_vip.utils.AddCartAnimation;
import com.manfenjiayuan.mixicook_vip.widget.FloatView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.api.shoppingCart.ShoppingCartApi;
import com.mfh.framework.api.shoppingCart.ShoppingCartApiImpl;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

import static com.tencent.bugly.crashreport.inner.InnerAPI.context;


/**
 * 商品类目
 * Created by bingshanguxue on 6/28/16.
 */
public class CategoryGoodsFragment extends BaseListFragment<ScGoodsSku> implements IScGoodsSkuView {

    public static final String EXTRA_KEY_NET_ID = "netId";
    public static final String EXTRA_KEY_FRONTCATEGORY_ID = "frontCategoryId";
    public static final String EXTRA_KEY_CATEGORYNAME = "categoryName";

    @BindView(R.id.rootview)
    CoordinatorLayout mRootView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private CategoryGoodsAdapter goodsListAdapter;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.float_cart)
    FloatView floatCartView;

    private Long netId;//店铺or网点编号
    private long frontCategoryId;//类目编号
    private String categoryName;//类目名称
    private ScGoodsSkuPresenter mScGoodsSkuPresenter;

    public static CategoryGoodsFragment newInstance(Bundle args) {
        CategoryGoodsFragment fragment = new CategoryGoodsFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        mScGoodsSkuPresenter = new ScGoodsSkuPresenter(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_category_goods;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            netId = args.getLong(EXTRA_KEY_NET_ID);
            frontCategoryId = args.getLong(EXTRA_KEY_FRONTCATEGORY_ID);
            categoryName = args.getString(EXTRA_KEY_CATEGORYNAME);
        }

        toolbar.setTitle(categoryName);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
//
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_manager) {
//                    managerAddress();
//                }
//                return true;
//            }
//        });
//        // Inflate a menu to be displayed in the toolbar
//        toolbar.inflateMenu(R.menu.menu_myaddress);

        initGoodsRecyclerView();

        reload();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARCode.ARC_SHOPCART: {
                if (data != null){
                    boolean isNeedReload = data.getBooleanExtra(ARCode.INTENT_KEY_ISRELOAD, false);
                    if (isNeedReload){
                        refreshShopcart();
                    }
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void reload() {
        super.reload();
        if (bSyncInProgress) {
            ZLogger.d("正在加载收货地址。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载订单流水。");
            onLoadFinished();
            return;
        }

        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        mScGoodsSkuPresenter.findOnlineGoodsList2(netId, frontCategoryId, mPageInfo);

        mPageInfo.setPageNo(1);
    }

    @Override
    public void loadMore() {
        super.loadMore();

        if (bSyncInProgress) {
            ZLogger.d("正在加载收货地址。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载收货地址。");
            onLoadFinished();
            return;
        }


        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            mScGoodsSkuPresenter.findOnlineGoodsList2(netId, frontCategoryId, mPageInfo);

        } else {
            ZLogger.d("加载收货地址，已经是最后一页。");
            onLoadFinished();
        }
    }

    /**
     * 跳转到购物车
     */
    @OnClick(R.id.float_cart)
    public void redirect2Cart() {
//        ActivityRoute.redirect2Cart(getActivity(), null, null);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_SHOPCART);

        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_SHOPCART);
    }

    public void onEventMainThread(ShopcartEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();

        ZLogger.d(String.format("ShopcartEvent(%d)", eventId));
        switch (eventId) {
//            case ShopcartEvent.EVENT_ID_ADD2CART: {
//                AddCartOptions options = event.getCartOptions();
//                if (options != null){
//                    AddCartAnimation.AddToCart2((ImageView) options.getSharedView(),
//                            floatCartView, getActivity(), rootView, 0.5F);
//                }
//            }
//            break;
            case ShopcartEvent.EVENT_ID_DATASETCHANGED: {
                refreshShopcart();
            }
            break;
        }
    }

    private void initGoodsRecyclerView() {
        mRLayoutManager = new GridLayoutManager(getActivity(), 3);
        goodsRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
//        //添加分割线
        goodsRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));

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
                int lastVisibleItem = mRLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = mRLayoutManager.getItemCount();
                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                // dy>0 表示向下滑动
//                ZLogger.d(String.format("%s %d(%d)", (dy > 0 ? "向上滚动" : "向下滚动"), lastVisibleItem, totalItemCount));
                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        goodsListAdapter = new CategoryGoodsAdapter(AppContext.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new CategoryGoodsAdapter.OnAdapterListener() {

                                                  @Override
                                                  public void onAdd2Cart(View view, ScGoodsSku product) {
                                                      add2Cart(view, product);
                                                  }

                                                  @Override
                                                  public void onItemClick(View view, int position) {

                                                  }

                                                  @Override
                                                  public void onItemLongClick(View view, int position) {

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

    @Override
    public void onIScGoodsSkuViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        onLoadStart();
    }

    @Override
    public void onIScGoodsSkuViewError(String errorMsg) {
        if (!StringUtils.isEmpty(errorMsg)) {
            ZLogger.df(errorMsg);
        }

        onLoadFinished();
    }

    @Override
    public void onIScGoodsSkuViewSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
        try {
            mPageInfo = pageInfo;
            if (mPageInfo.getPageNo() == 1) {
                if (goodsListAdapter != null) {
                    goodsListAdapter.setEntityList(dataList);
                }
            } else {
                if (goodsListAdapter != null) {
                    goodsListAdapter.appendEntityList(dataList);
                }
            }

            onLoadFinished();
        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载收货地址失败: %s", ex.toString()));
            onLoadFinished();
        }
    }

    @Override
    public void onIScGoodsSkuViewSuccess(ScGoodsSku data) {

    }

    /**
     * 加入购物车
     */
    private void add2Cart(View view, ScGoodsSku product) {
        if (product == null) {
            return;
        }

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            ZLogger.d("网络未连接");
            return;
        }

        AddCartAnimation.AddToCart2((ImageView) view,
                floatCartView, getActivity(), mRootView, 0.5F);

        NetCallBack.NetTaskCallBack responseC = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        ZLogger.df("加入购物车成功");
                        // TODO: 10/10/2016 刷新购物车按钮
//                        DialogUtil.showHint("加入购物车成功");
                        refreshShopcart();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("加入购物车失败: " + errMsg);
                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {
        };

        JSONObject cart = new JSONObject();
        cart.put("goodsId", product.getProductId());
        cart.put("bcount", 1);
        cart.put("productId", product.getProductId());
        cart.put("shopId", netId);
        cart.put("price", product.getCostPrice());
        cart.put("proSkuId", product.getProSkuId());
        cart.put("bizType", ShoppingCartApi.BIZTYPE_BUY);
        cart.put("subType", ShoppingCartApi.SUBTYPE);

        JSONArray specItems = new JSONArray();
        ShoppingCartApiImpl.add2Cart(cart, specItems, responseC);
    }

    /**
     * 刷新购物车商品数量
     * */
    private void refreshShopcart(){
        NetCallBack.NetTaskCallBack responseC = new NetCallBack.NetTaskCallBack<CartBrief,
                NetProcessor.Processor<CartBrief>>(
                new NetProcessor.Processor<CartBrief>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        ZLogger.df("调整购物车商品数量: 操作成功");
//
                        CartBrief cartBrief = null;
                        try {
                            if (rspData != null) {
                                RspBean<CartBrief> retValue = (RspBean<CartBrief>) rspData;
                                cartBrief = retValue.getValue();
                            }
                        } catch (Exception e) {
                            ZLogger.ef(e.toString());
                        }
//
                        if (cartBrief != null){
                            floatCartView.setBadgeNumber(cartBrief.getSkuNum());
//                            DialogUtil.showHint(String.format("商品SKu数目 %.2f", cartBrief.getSkuNum()));
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("调整购物车商品数量: " + errMsg);
                    }
                }
                , CartBrief.class
                , MfhApplication.getAppContext()) {
        };

        ShoppingCartApiImpl.staticShopCart(String.valueOf(netId), responseC);
    }
}
