package com.mfh.litecashier.ui.fragment.cashier;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.GridItemDecoration2;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.litecashier.event.AddLaunchGoodsEvent;
import com.mfh.litecashier.event.LaundryGoodsEvent;
import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.mfh.litecashier.ui.adapter.LaundryGoodsAdapter;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.mfh.litecashier.utils.ACacheHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 洗衣类目商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public class LaundryGoodsFragment extends BaseListFragment<ChainGoodsSku>
implements IChainGoodsSkuView {

    @Bind(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.order_list)
    RecyclerViewEmptySupport mRecyclerView;
    @Bind(R.id.empty_view)
    TextView emptyView;

    GridLayoutManager mRLayoutManager;
    private LaundryGoodsAdapter adapter;

    private Long parentId;
    private Long categoryId;
    private String cacheKey;

    private ChainGoodsSkuPresenter chainGoodsSkuPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inv_sendorder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        chainGoodsSkuPresenter = new ChainGoodsSkuPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        //for Fragment.instantiate
        Bundle args = getArguments();
        if (args != null) {
            parentId = args.getLong("parentId");
            categoryId = args.getLong("categoryId");
        }
        //format:CACHE_KEY_FRONT_CATEGORY_GOODS_[parentId]_[categoryId]
        cacheKey = String.format("%s_%d_%d", ACacheHelper.CK_LAUNDRY_CATEGORY_GOODS, parentId, categoryId);
        ZLogger.d(String.format("parentId=%d,categoryId=%d,cacheKey=%s", parentId, categoryId, cacheKey));

        initRecyclerView();
        setupSwipeRefresh();

        readCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(LaundryGoodsEvent event) {
        ZLogger.d(String.format("LaundryGoodsFragment: LaundryGoodsEvent(%d/%d)", event.getAffairId(), event.getCategoryId()));
        if (event.getCategoryId().compareTo(categoryId) != 0){
            return;
        }
        if (event.getAffairId() == LaundryGoodsEvent.EVENT_ID_RELOAD_DATA) {
            //内容为空，自动重新加载
            if (adapter == null || adapter.getItemCount() <= 0){
                //优先加载缓存数据，加载缓存失败，重新加载
                if (!readCache()){
                    reload();
                }
            }
            //更新缓存数据
            else{
                readCache();
            }
        }
    }

    private void initRecyclerView(){
        mRLayoutManager = new GridLayoutManager(CashierApp.getAppContext(), 6);

        mRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //设置列表为空时显示的视图
        mRecyclerView.setEmptyView(emptyView);
//        mRecyclerView.setWrapperView(mSwipeRefreshLayout);
        //添加分割线
        mRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 1,
                getActivity().getResources().getColor(R.color.mf_dividerColorPrimary), 0,
                getActivity().getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f,
                getActivity().getResources().getColor(R.color.mf_dividerColorPrimary), 0f));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        adapter = new LaundryGoodsAdapter(CashierApp.getAppContext(), null);
        adapter.setOnAdapterLitener(new LaundryGoodsAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                EventBus.getDefault().post(new AddLaunchGoodsEvent(adapter.getEntity(position)));
            }

            @Override
            public void onItemLongClick(View view, final int position) {

                //长按操作不支持
//                final ChainGoodsSku goods = adapter.getEntity(position);
//                if (goods == null) {
//                    return;
//                }
//
//                if (actionDialog == null){
//                    actionDialog = new ActionDialog(getActivity());
//                    actionDialog.setCancelable(true);
//                    actionDialog.setCanceledOnTouchOutside(true);
//                }
//                actionDialog.setAction1("添加到我的", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        //TODO,
//                        actionDialog.dismiss();
//                        //TODO,
//                        CommonlyGoodsService.get().saveOrUpdate(parentId, goods);
//                    }
//                });
//                actionDialog.show();
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
            }
        });

        mRecyclerView.setAdapter(adapter);
    }

    public void reset(){
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        if (entityList == null){
            entityList = new ArrayList<>();
        }
        else{
            entityList.clear();
        }
    }

    /**
     * 重新加载数据
     * */
    @OnClick(R.id.empty_view)
    public synchronized void reload(){
        if (bSyncInProgress){
//            onLoadFinished();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())){
            ZLogger.d("网络未连接，暂停加载洗衣类目商品。");
            onLoadFinished();
            return;
        }


        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        chainGoodsSkuPresenter.loadLaundryGoods(mPageInfo, categoryId, MfhLoginService.get().getCurOfficeId());
        mPageInfo.setPageNo(1);
    }

    /**
     * 读取缓存
     * */
    public synchronized boolean readCache(){
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(cacheKey);
        List<ChainGoodsSku> cacheData = JSONArray.parseArray(cacheStr, ChainGoodsSku.class);
        if (cacheData != null && cacheData.size() > 0){
            ZLogger.d(String.format("加载缓存数据(%s): %d个洗衣类目商品", cacheKey, cacheData.size()));
            if (adapter != null) {
                adapter.setEntityList(cacheData);
            }
            return true;
        }

        return false;
    }

    /**
     * 翻页加载更多数据
     * */
    public void loadMore(){
        if (bSyncInProgress){
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())){
            ZLogger.d("网络未连接，暂停加载洗衣类目商品。");
            onLoadFinished();
            return;
        }


        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE){
            mPageInfo.moveToNext();

            chainGoodsSkuPresenter.loadLaundryGoods(mPageInfo, categoryId,
                    MfhLoginService.get().getCurOfficeId());
        }else{
            ZLogger.d("加载洗衣类目商品，已经是最后一页。");
            onLoadFinished();
        }
    }


    @Override
    public void onProcess() {
        onLoadStart();
    }

    @Override
    public void onError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
        mPageInfo = pageInfo;

        //第一页，缓存数据
        if (mPageInfo.getPageNo() == 1){
            ZLogger.d("缓存第一页洗衣类目商品");
            JSONArray cacheArrays = new JSONArray();
            if (dataList != null){
                cacheArrays.addAll(dataList);
            }
            if (adapter != null) {
                adapter.setEntityList(dataList);
            }
            ACacheHelper.put(cacheKey, cacheArrays.toJSONString());
        }
        else{
            if (adapter != null) {
                adapter.appendEntityList(dataList);
            }
        }

        onLoadFinished();
        ZLogger.d(String.format("保存洗衣商品商品,pageInfo':page=%d,rows=%d(%d/%d)",
                mPageInfo.getPageNo(), mPageInfo.getPageSize(),
                adapter.getItemCount(), mPageInfo.getTotalCount()));
    }

    @Override
    public void onQueryChainGoodsSku(ChainGoodsSku chainGoodsSku) {

    }

    /**
     * 设置刷新
     * */
    private void setupSwipeRefresh(){
//        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefreshlayout);
        if(mSwipeRefreshLayout != null){
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.swiperefresh_color1, R.color.swiperefresh_color2,
                    R.color.swiperefresh_color3, R.color.swiperefresh_color4);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (mState == STATE_REFRESH) {
                        ZLogger.d("正在刷新");
                        return;
                    }

                    reload();
                }
            });
        }
        mState = STATE_NONE;
    }

    /**
     * 设置刷新状态
     * */
    public void setRefreshing(boolean refreshing) {
        if (refreshing) {
            setSwipeRefreshLoadingState();
        } else {
            setSwipeRefreshLoadedState();
        }
    }

    /** 设置顶部正在加载的状态 */
    private void setSwipeRefreshLoadingState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            // 防止多次重复刷新
            mSwipeRefreshLayout.setEnabled(false);


            mState = STATE_REFRESH;
        }
    }

    /** 设置顶部加载完毕的状态 */
    private void setSwipeRefreshLoadedState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);

            mState = STATE_NONE;
        }
    }

}
