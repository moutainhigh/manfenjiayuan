package com.mfh.litecashier.ui.goodsflow;

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

import com.alibaba.fastjson.JSONArray;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.category.CategoryOption;
import com.mfh.framework.api.category.CategoryQueryInfo;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.rxapi.http.ExceptionHandle;
import com.mfh.framework.rxapi.httpmgr.ScCategoryInfoHttpManager;
import com.mfh.framework.rxapi.subscriber.MSubscriber;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.utils.ACacheHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;


/**
 * 后台类目列表
 * Created by bingshanguxue on 15/8/30.
 */
public class BackendCategoryListFragment extends BaseListFragment<CategoryOption> {

    //    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    //    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private BackendCategoryListAdapter companyAdapter;

    //    @Bind(R.id.empty_view)
    View emptyView;
    //    @Bind(R.id.animProgress)
    ProgressBar progressBar;


    public static BackendCategoryListFragment newInstance(Bundle args) {
        BackendCategoryListFragment fragment = new BackendCategoryListFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_template_goods_list;
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
//            tenantId = args.getLong(EXTRA_KEY_TENANTID);
//        }

        mToolbar.setTitle("选择类目");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
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

        companyAdapter = new BackendCategoryListAdapter(getActivity(), null);
        companyAdapter.setOnAdapterListener(new BackendCategoryListAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                //TODO,跳转至详情页
                Intent data = new Intent();
                data.putExtra("CategoryOption", companyAdapter.getEntity(position));

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
     * 加载后台类目树
     */
    @Override
    public void reload() {
        super.reload();
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_BACKEND_CATEGORY_TREE);
        List<CategoryOption> cacheData = JSONArray.parseArray(cacheStr, CategoryOption.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d个后台商品类目",
                    ACacheHelper.CK_BACKEND_CATEGORY_TREE, cacheData.size()));

            companyAdapter.setEntityList(cacheData);
        } else {
            companyAdapter.setEntityList(null);
            listBackendCategoryStep1();
        }
    }

    /**
     * 下载后台类目树
     */
    private void listBackendCategoryStep1() {
        onLoadStart();

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadFinished();
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("kind", "code");
        options.put("domain", String.valueOf(CateApi.DOMAIN_TYPE_PROD));
        options.put("cateType", "");
        options.put("catePosition", String.valueOf(CateApi.CATE_POSITION_BACKEND));
        options.put("deep", "2");//层级
//        params.put("tenantId", MfhLoginService.get().getSpid() == null ? "0" : String.valueOf(MfhLoginService.get().getSpid()));
//        options.put("tenantId", CATEGORY_TENANT_ID);//使用类目专属ID
        ScCategoryInfoHttpManager.getInstance().comnQuery(options, new MSubscriber<CategoryQueryInfo>() {


//            @Override
//            public void onError(Throwable e) {
//                ZLogger.ef("加载后台类目树失败, " + e.toString());
//                onLoadFinished();
//            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {

                ZLogger.ef("加载后台类目树失败, " + e.toString());
                onLoadFinished();
            }

            @Override
            public void onNext(CategoryQueryInfo categoryQueryInfo) {
                if (categoryQueryInfo != null) {
                    //缓存数据
                    listBackendCategoryStep2(categoryQueryInfo.getOptions());
                } else {
                    listBackendCategoryStep2(null);
                }

                onLoadFinished();
            }
        });
    }

    /**
     * 保存后台类目树
     */
    private void listBackendCategoryStep2(List<CategoryOption> options) {
//        ZLogger.d(String.format("保存POS %d个后台类目",
//                (options != null ? options.size() : 0)));
        //缓存数据
        JSONArray cacheArrays = new JSONArray();
        if (options != null && options.size() > 0) {
            for (CategoryOption option : options) {
                cacheArrays.add(option);
            }
        }
        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .put(ACacheHelper.CK_BACKEND_CATEGORY_TREE, cacheArrays.toJSONString());

        if (companyAdapter != null) {
            companyAdapter.setEntityList(options);
        }
    }

}
