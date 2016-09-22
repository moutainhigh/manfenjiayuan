package com.mfh.litecashier.ui.fragment.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.api.impl.InvOrderApiImpl;
import com.mfh.framework.api.invIoOrder.InvIoOrderApi;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.framework.api.invIoOrder.InvIoOrder;
import com.mfh.litecashier.bean.InvIoOrderItem;
import com.mfh.litecashier.event.InvIOOrderEvent;
import com.mfh.litecashier.event.StockBatchEvent;
import com.mfh.litecashier.ui.adapter.InvIOGoodsAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.mfh.litecashier.utils.ACacheHelper;

import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.http.AjaxParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 库存－－库存批次
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InventoryIOFragment extends BaseFragment {

    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private InvIOGoodsAdapter goodsListAdapter;
    private LinearLayoutManager linearLayoutManager;
    @Bind(R.id.frame_bottom)
    LinearLayout frameBottom;
    @Bind(R.id.tv_goods_quantity)
    TextView tvGoodsQunatity;
    @Bind(R.id.tv_total_amount)
    TextView tvTotalAmount;

    @Bind(R.id.tab_order)
    TopSlidingTabStrip paySlidingTabStrip;
    @Bind(R.id.viewpager_order)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;
    @Bind(R.id.button_create_bill)
    Button btnCreateBill;

    private boolean isLoadingMore;

    private boolean bSyncInProgress = false;//是否正在同步
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 30;
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private List<InvIoOrderItem> goodsList = new ArrayList<>();

    private InvIoOrder curOrder;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_stock_patch;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initTabs();
        initGoodsRecyclerView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.button_create_bill)
    public void createNewIOOrder() {
        DialogUtil.showHint(R.string.coming_soon);
//        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_CREATE_INVENTORY_IO_ORDER);
//
////        ServiceActivity.actionStart(getActivity(), extras);
//
//        Intent intent = new Intent(getActivity(), ServiceActivity.class);
//        intent.putExtras(extras);
//        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_CREATE_STOCK_BATCH: {
                //刷新订单列表
//                goodsListAdapter.notifyDataSetChanged();
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initTabs() {
        //setupViewPager
        mViewPager.setScrollEnabled(true);
        paySlidingTabStrip.setOnClickTabListener(null);
        paySlidingTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                notifyOrderRefresh(page);
            }
        });
        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(), paySlidingTabStrip, mViewPager, R.layout.tabitem_text);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();

        Bundle args2 = new Bundle();
        args2.putInt(InvIOOrderFragment.EXTRA_KEY_ORDER_TYPE, InvIoOrderApi.ORDER_TYPE_OUT);
        args2.putString(InvIOOrderFragment.EXTRA_KEY_CACHE_KEY,
                String.format("%s_%d", ACacheHelper.CK_INVENTORY_IO, InvIoOrderApi.ORDER_TYPE_OUT));
        mTabs.add(new ViewPageInfo("出库", "出库", InvIOOrderFragment.class,
                args2));

        Bundle args3 = new Bundle();
        args3.putInt(InvIOOrderFragment.EXTRA_KEY_ORDER_TYPE, InvIoOrderApi.ORDER_TYPE_IN);
        args2.putString(InvIOOrderFragment.EXTRA_KEY_CACHE_KEY,
                String.format("%s_%d", ACacheHelper.CK_INVENTORY_IO, InvIoOrderApi.ORDER_TYPE_IN));
        mTabs.add(new ViewPageInfo("入库", "入库", InvIOOrderFragment.class,
                args3));

        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    private void notifyOrderRefresh(int index) {
        Bundle args = new Bundle();
//        if (index == 0) {
//            args.putString(InvIOOrderFragment.EXTRA_KEY_ORDER_TYPE, String.valueOf(InvIoOrder.ORDER_TYPE_SET));
//        } else
        if (index == 0) {
            args.putInt(InvIOOrderFragment.EXTRA_KEY_ORDER_TYPE, InvIoOrderApi.ORDER_TYPE_OUT);
        } else if (index == 1) {
            args.putInt(InvIOOrderFragment.EXTRA_KEY_ORDER_TYPE, InvIoOrderApi.ORDER_TYPE_IN);
        }
        EventBus.getDefault().post(new InvIOOrderEvent(InvIOOrderEvent.EVENT_ID_RELOAD_DATA, args));
    }

    private void initGoodsRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
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
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = linearLayoutManager.getItemCount();
                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                // dy>0 表示向下滑动
//                ZLogger.d(String.format("%s %d(%d)", (dy > 0 ? "向上滚动" : "向下滚动"), lastVisibleItem, totalItemCount));
                if (dy > 0) {
//                    fabShopcart.setVisibility(View.VISIBLE);
                    if ((lastVisibleItem >= totalItemCount - 4) && !isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
//                    fabShopcart.setVisibility(View.GONE);
                }
            }
        });

        goodsListAdapter = new InvIOGoodsAdapter(CashierApp.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new InvIOGoodsAdapter.OnAdapterListener() {
                                                  @Override
                                                  public void onDataSetChanged() {
                                                      isLoadingMore = false;
                                                      refreshBottomBar();
                                                  }

                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }


    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(StockBatchEvent event) {
        ZLogger.d(String.format("InventoryCostFragment: StockBatchEvent(%d)", event.getEventId()));
        if (event.getEventId() == StockBatchEvent.EVENT_ID_RELOAD_DATA) {
            //优先加载缓存显示，同时在后台加载数据
            notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
        } else if (event.getEventId() == StockBatchEvent.EVENT_ID_RELAOD_ITEM_DATA) {
            Bundle args = event.getArgs();
            if (args != null) {
                loadGoodsList((InvIoOrder) args.getSerializable("order"));
            }
        }
    }


    /**
     * 开始加载
     */
    private void onLoadStart() {
        isLoadingMore = true;
        bSyncInProgress = true;
//        showSyncDataDialog();
    }

    /**
     * 加载完成
     */
    private void onLoadFinished() {
        bSyncInProgress = false;
        isLoadingMore = false;
//        if (syncDataDialog != null) {
//            syncDataDialog.dismiss();
//        }
    }

    /**
     * 刷新底部信息
     */
    private void refreshBottomBar() {
        Double count = 0D;
        Double goodsFee = 0D;
        List<InvIoOrderItem> orderItems = goodsListAdapter.getEntityList();
        if (orderItems != null && orderItems.size() > 0) {
            for (InvIoOrderItem orderItem : orderItems) {
                count += orderItem.getQuantityCheck();
                goodsFee += orderItem.getPrice() * orderItem.getQuantityCheck();
            }
        }

        tvGoodsQunatity.setText(String.format("商品数：%.2f", count));
        tvTotalAmount.setText(String.format("商品金额：%.2f", goodsFee));
//        tvTransFee.setText(String.format("配送费：%.2f", curOrder == null ? 0D : curOrder.getTransFee()));
    }


    /**
     * 加载商品列表
     */
    private void loadGoodsList(InvIoOrder invIoOrder) {
        curOrder = invIoOrder;

        if (curOrder == null) {
            if (goodsList == null) {
                goodsList = new ArrayList<>();
            } else {
                goodsList.clear();
            }
            if (goodsListAdapter != null) {
                goodsListAdapter.setEntityList(goodsList);
            }
            onLoadFinished();

            tvGoodsQunatity.setText(String.format("商品数：%.2f", 0D));
            tvTotalAmount.setText("商品金额：");
            return;
        }

        tvGoodsQunatity.setText(String.format("商品数：%.2f", curOrder.getCommitGoodsNum()));
        tvTotalAmount.setText("商品金额：");

        onLoadStart();

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载库存批次。");
            onLoadFinished();
            return;
        }

        //初始化
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        //从第一页开始请求，每页最多50条记录
        load(mPageInfo);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载库存批次。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载库存批次。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            onLoadStart();
            load(mPageInfo);
        } else {
            ZLogger.d("加载库存批次，已经是最后一页。");
            onLoadFinished();
        }
    }


    private void load(PageInfo pageInfo) {
        AjaxParams params = new AjaxParams();

        if (curOrder != null && curOrder.getId() != null) {
            params.put("orderId", String.valueOf(curOrder.getId()));
        }
        params.put("wrapper", "true");
//        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvIoOrderItem>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<InvIoOrderItem> rs) {
                //此处在主线程中执行。
                new ProductQueryAsyncTask(pageInfo).execute(rs);
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载库存批次失败:" + errMsg);
                onLoadFinished();
            }
        }, InvIoOrderItem.class, CashierApp.getAppContext());

        AfinalFactory.postDefault(InvOrderApiImpl.URL_INVIOORDERITEM_LIST, params, queryRsCallBack);
    }

    public class ProductQueryAsyncTask extends AsyncTask<RspQueryResult<InvIoOrderItem>, Integer, Long> {
        private PageInfo pageInfo;

        public ProductQueryAsyncTask(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
        }

        @Override
        protected Long doInBackground(RspQueryResult<InvIoOrderItem>... params) {
            saveQueryResult(params[0], pageInfo);
            return -1L;
//        return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
//            ZLogger.d(String.format("pageInfo':page=%d,rows=%d(%d)", pageInfo.getPageNo(), pageInfo.getPageSize(), (goodsList == null ? 0 : goodsList.size())));

            if (goodsListAdapter != null) {
                goodsListAdapter.setEntityList(goodsList);
            }
            onLoadFinished();
        }

        /**
         * 将后台返回的结果集保存到本地,同步执行
         *
         * @param rs       结果集
         * @param pageInfo 分页信息
         */
        private void saveQueryResult(RspQueryResult<InvIoOrderItem> rs, PageInfo pageInfo) {//此处在主线程中执行。
            try {
                mPageInfo = pageInfo;
                if (mPageInfo.getPageNo() == 1) {
                    if (goodsList == null) {
                        goodsList = new ArrayList<>();
                    } else {
                        goodsList.clear();
                    }
                }
                else{
                    if (goodsList == null) {
                        goodsList = new ArrayList<>();
                    }
                }

                if (rs == null) {
                    return;
                }

                //保存下来
                int retSize = rs.getReturnNum();
                ZLogger.d(String.format("加载 %d 商品", retSize));
                for (EntityWrapper<InvIoOrderItem> wrapper : rs.getRowDatas()) {
                    InvIoOrderItem ioOrderItem = wrapper.getBean();
                    Map<String, String> caption = wrapper.getCaption();
                    if (caption != null) {
                        ioOrderItem.setOrderTypeCaption(wrapper.getCaption().get("orderType"));
                    }

                    goodsList.add(ioOrderItem);
                }

            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.e(String.format("加载库存批次失败: %s", ex.toString()));
            }
        }
    }
}
