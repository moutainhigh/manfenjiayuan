package com.bingshanguxue.pda.bizz.invrecv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bingshanguxue.pda.R;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.productConvert.ProductConvert;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.entity.MEntityWrapper;
import com.mfh.framework.rxapi.http.ScProductConvertHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 准换规则列表
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ScProductConvertListFragment extends BaseListFragment<ProductConvert> {

    //    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    //    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ScProductConvertAdapter companyAdapter;

    //    @Bind(R.id.empty_view)
    View emptyView;
    //    @Bind(R.id.animProgress)
    ProgressBar progressBar;


    public static ScProductConvertListFragment newInstance(Bundle args) {
        ScProductConvertListFragment fragment = new ScProductConvertListFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_scproduct_convertlist;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MAX_SYNC_PAGESIZE = 30;
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.goods_list);
        emptyView = rootView.findViewById(R.id.empty_view);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);

        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reload();
            }
        });
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//            status = args.getString(EXTRA_KEY_STATUS);
//        }

        mToolbar.setTitle("选择转换模型");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        initRecyclerView();

        reload();
    }


    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        mRecyclerView.setEmptyView(emptyView);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        companyAdapter = new ScProductConvertAdapter(getActivity(), null);
        companyAdapter.setOnAdapterListener(new ScProductConvertAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                //TODO,跳转至详情页
                Intent data = new Intent();
                data.putExtra("productConvert", companyAdapter.getEntity(position));

                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
//                animProgress.setVisibility(View.GONE);
            }
        });

        mRecyclerView.setAdapter(companyAdapter);
    }

    @Override
    public void onLoadStart() {
        super.onLoadStart();

        progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public void onLoadFinished() {
        super.onLoadFinished();

        progressBar.setVisibility(View.GONE);
    }

    /**
     * 重新加载数据
     */
//    @OnClick(R.id.empty_view)
    public void reload() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载转换规则。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载转换规则。");
            onLoadFinished();
            return;
        }


        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        load(mPageInfo);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载转换规则。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载转换规则。");
            onLoadFinished();
            return;
        }


        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            load(mPageInfo);
        } else {
            ZLogger.d("加载转换规则，已经是最后一页。");
            onLoadFinished();
        }
    }

    private void load(PageInfo pageInfo) {
        onLoadStart();

        Map<String, String> options = new HashMap<>();
        options.put("wrapper", "true");
//        options.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
//        options.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        if (pageInfo != null){
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        ScProductConvertHttpManager.getInstance().list(options,
                new MQuerySubscriber<MEntityWrapper<ProductConvert>>(pageInfo) {

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<MEntityWrapper<ProductConvert>> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        List<ProductConvert> entityList = new ArrayList<>();
                        if (dataList != null) {
                            for (MEntityWrapper<ProductConvert> wrapper : dataList) {
                                ProductConvert productConvert = wrapper.getBean();
                                Map<String, String> caption = wrapper.getCaption();
                                if (caption != null) {
                                    productConvert.setCaptionOriginProSku(caption.get("originProSku"));
                                }
                                entityList.add(productConvert);
                            }
                        }

                        mPageInfo = pageInfo;

                        //第一页，缓存数据
                        if (mPageInfo.getPageNo() == 1) {
                            if (companyAdapter != null) {
                                companyAdapter.setEntityList(entityList);
                            }
                        } else {
                            if (dataList != null) {
                                if (companyAdapter != null) {
                                    companyAdapter.appendEntityList(entityList);
                                }
                            }
                        }

                        ZLogger.d(String.format("加载商品采购订单结束,pageInfo':page=%d/%d(%d/%d)",
                                mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                                companyAdapter.getItemCount(), mPageInfo.getTotalCount()));

                        onLoadFinished();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        ZLogger.df("加载转换规则失败:" + e.toString());
                        onLoadFinished();
                    }
                });
    }

}
