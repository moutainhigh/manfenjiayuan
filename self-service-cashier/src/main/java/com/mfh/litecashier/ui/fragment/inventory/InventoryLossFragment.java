package com.mfh.litecashier.ui.fragment.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.mfh.framework.api.invLossOrder.InvLossOrder;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invOrder.InvOrderApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.InvLossOrderItem;
import com.mfh.litecashier.event.StockLossEvent;
import com.mfh.litecashier.ui.adapter.StockLossGoodsAdapter;
import com.mfh.litecashier.ui.adapter.StockLossOrderAdapter;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.http.AjaxParams;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 库存－－库存报损
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InventoryLossFragment extends BaseFragment {

    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private StockLossGoodsAdapter goodsListAdapter;
    private LinearLayoutManager linearLayoutManager;
    @BindView(R.id.empty_view)
    TextView goodsEmptyView;
    @BindView(R.id.frame_bottom)
    LinearLayout frameBottom;
    @BindView(R.id.tv_quantity)
    TextView tvQuntity;
    @BindView(R.id.tv_amount)
    TextView tvAmount;

    private static final int STATE_NONE = 0;
    private static final int STATE_REFRESH = 1;
    private static final int STATE_LOADMORE = 2;
    private static final int STATE_NOMORE = 3;
    private static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    private static int mState = STATE_NONE;
    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    @BindView(R.id.order_empty_view)
    TextView orderEmptyView;
    private StockLossOrderAdapter orderListAdapter;
    @BindView(R.id.button_create)
    Button btnCreate;


    private List<InvLossOrder> orderList = new ArrayList<>();
    private InvLossOrder curOrder;

    private boolean isLoadingMore;

    private boolean bSyncInProgress = false;//是否正在同步
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 30;
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private List<InvLossOrderItem> goodsList = new ArrayList<>();



    /**
     * 读取报损订单缓存
     * 如果没有缓存则重新加载订单列表
     * 如果有缓存则根据同步状态{@link SharedPreferencesUltimate.PK_SYNC_STOCKLOSS_ORDER_ENABLED}决定是否需要重新加载订单列表。同步状态
     */
    public synchronized boolean readInvLossOrderCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_STOCK_LOSS_ORDER);
        List<InvLossOrder> cacheData = JSONArray.parseArray(cacheStr, InvLossOrder.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载报损订单缓存数据(%s): %d条报损订单",
                    ACacheHelper.CK_STOCK_LOSS_ORDER, cacheData.size()));
//            refreshCategoryGoodsTab(entity.getCategoryId(), cacheData);
            if (orderListAdapter != null) {
                orderListAdapter.setEntityList(cacheData);
            }

            return true;
        }
        return false;
    }


    /**
     * 加载订单明细
     */
    private void loadGoodsList(InvLossOrder order) {
        curOrder = order;
        if (order == null) {
            frameBottom.setVisibility(View.VISIBLE);
            tvQuntity.setText(String.format("数量：%.2f", 0D));
            tvAmount.setText(String.format("金额：%.2f", 0D));

            goodsListAdapter.setEntityList(null);
            return;
        }

        //正在盘点
        if (order.getStatus().equals(0)) {
            frameBottom.setVisibility(View.INVISIBLE);
        } else {
            frameBottom.setVisibility(View.VISIBLE);
            tvQuntity.setText(String.format("数量：%.2f", curOrder.getCommitGoodsNum()));
            tvAmount.setText(String.format("金额：%.2f", curOrder.getCommitPrice()));
        }

        onLoadStart();

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载库存报损列表。");
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
            ZLogger.d("正在加载库存报损。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载库存报损。");
            onLoadFinished();
            return;
        }


        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            onLoadStart();
            load(mPageInfo);
        } else {
            ZLogger.d("加载库存报损，已经是最后一页。");
            onLoadFinished();
        }
    }

    private void load(PageInfo pageInfo) {
        AjaxParams params = new AjaxParams();
        if (curOrder != null) {
            params.put("orderId", String.valueOf(curOrder.getId()));
        }
        params.put("wrapper", "true");
        if (pageInfo != null) {
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<InvLossOrderItem>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<InvLossOrderItem> rs) {
                        saveQueryResult(rs, pageInfo);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载报损订单明细失败:" + errMsg);
                        onLoadFinished();
                    }
                }, InvLossOrderItem.class, CashierApp.getAppContext());

        AfinalFactory.postDefault(InvOrderApiImpl.URL_INVLOSSORDERITEM_LIST, params, queryRsCallBack);
    }

    /**
     * 将后台返回的结果集保存到本地,同步执行
     *
     * @param rs       结果集
     * @param pageInfo 分页信息
     */
    private void saveQueryResult(final RspQueryResult<InvLossOrderItem> rs, final PageInfo pageInfo) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                mPageInfo = pageInfo;
                if (goodsList == null) {
                    goodsList = new ArrayList<>();
                }
                if (mPageInfo.getPageNo() == 1) {
                    goodsList.clear();
                }

                if (rs == null) {
                    return;
                }

                //保存下来
                int retSize = rs.getReturnNum();
                ZLogger.d(String.format("保存 %d 条报损订单明细", retSize));
                for (EntityWrapper<InvLossOrderItem> wrapper : rs.getRowDatas()) {
                    goodsList.add(wrapper.getBean());
                }

                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef("加载库存批次失败:" + e.toString());
                        onLoadFinished();
                    }

                    @Override
                    public void onNext(String s) {
                        if (goodsListAdapter != null) {
                            goodsListAdapter.setEntityList(goodsList);
                        }
                        onLoadFinished();
                    }

                });
    }


}
