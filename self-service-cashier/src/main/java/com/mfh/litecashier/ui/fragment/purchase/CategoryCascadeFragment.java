package com.mfh.litecashier.ui.fragment.purchase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.manfenjiayuan.business.bean.CategoryOption;
import com.mfh.litecashier.event.OrderGoodsEvent;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.ui.adapter.CategoryCascadeAdapter;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 订货－－类目层叠
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CategoryCascadeFragment extends BaseFragment {

    @Bind(R.id.root_category_list)
    RecyclerView rootRecyclerView;
    @Bind(R.id.second_category_list)
    RecyclerView secondRecyclerView;
    @Bind(R.id.third_category_list)
    RecyclerView thirdRecyclerView;
    @Bind(R.id.fourth_category_list)
    RecyclerView fourthRecyclerView;

    private CategoryCascadeAdapter rootListAdapter;
    private CategoryCascadeAdapter secondListAdapter;
    private CategoryCascadeAdapter thirdListAdapter;
    private CategoryCascadeAdapter fourthListAdapter;

    private CategoryOption curOption = null;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_category_cascade;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initRootRecyclerView();
        initSecondRecyclerView();
        initThirdRecyclerView();
        initFourthRecyclerView();

        reload();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 返回上一层
     */
    @OnClick(R.id.button_submit)
    public void backupCategory() {
        Bundle args = new Bundle();
        args.putSerializable("categoryOption", curOption);
        EventBus.getDefault().post(new OrderGoodsEvent(OrderGoodsEvent.EVENT_ID_HIDE_CATEGORY, args));
    }

    /**
     * 初始化类目
     * */
    private void initRootRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rootRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        rootRecyclerView.setHasFixedSize(true);
        //添加分割线
        rootRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        rootListAdapter = new CategoryCascadeAdapter(CashierApp.getAppContext(), null);
        rootListAdapter.setOnAdapterListsner(new CategoryCascadeAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                curOption = rootListAdapter.getCurOption();
                refreshSecondList();
            }

            @Override
            public void onDataSetChanged() {
                refreshSecondList();
            }
        });
        rootRecyclerView.setAdapter(rootListAdapter);
    }
    private void initSecondRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        secondRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        secondRecyclerView.setHasFixedSize(true);
        //添加分割线
        secondRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        secondListAdapter = new CategoryCascadeAdapter(CashierApp.getAppContext(), null);
        secondListAdapter.setOnAdapterListsner(new CategoryCascadeAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                curOption = secondListAdapter.getCurOption();
                refreshThirdList();
            }

            @Override
            public void onDataSetChanged() {
                refreshThirdList();
            }
        });
        secondRecyclerView.setAdapter(secondListAdapter);
    }
    private void initThirdRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        thirdRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        thirdRecyclerView.setHasFixedSize(true);
        //添加分割线
        thirdRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        thirdListAdapter = new CategoryCascadeAdapter(CashierApp.getAppContext(), null);
        thirdListAdapter.setOnAdapterListsner(new CategoryCascadeAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                curOption = thirdListAdapter.getCurOption();
                refreshFourthList();
            }

            @Override
            public void onDataSetChanged() {
                refreshFourthList();
            }
        });
        thirdRecyclerView.setAdapter(thirdListAdapter);
    }
    private void initFourthRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        fourthRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        fourthRecyclerView.setHasFixedSize(true);
        //添加分割线
        fourthRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        fourthListAdapter = new CategoryCascadeAdapter(CashierApp.getAppContext(), null);
        fourthListAdapter.setOnAdapterListsner(new CategoryCascadeAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                curOption = fourthListAdapter.getCurOption();
            }

            @Override
            public void onDataSetChanged() {
            }
        });
        fourthRecyclerView.setAdapter(fourthListAdapter);
    }

    private void refreshSecondList(){
        CategoryOption option = rootListAdapter.getCurOption();

        if (option != null) {
            secondListAdapter.setEntityList(option.getItems());
            secondRecyclerView.setVisibility(View.VISIBLE);
        } else {
            secondListAdapter.setEntityList(null);
            secondRecyclerView.setVisibility(View.INVISIBLE);
        }
    }
    private void refreshThirdList(){
        CategoryOption option = secondListAdapter.getCurOption();

        if (option != null) {
            thirdListAdapter.setEntityList(option.getItems());
            thirdRecyclerView.setVisibility(View.VISIBLE);
        } else {
            thirdListAdapter.setEntityList(null);
            thirdRecyclerView.setVisibility(View.INVISIBLE);
        }
    }
    private void refreshFourthList(){
        CategoryOption option = thirdListAdapter.getCurOption();

        if (option != null) {
            fourthListAdapter.setEntityList(option.getItems());
            fourthRecyclerView.setVisibility(View.VISIBLE);
        } else {
            fourthListAdapter.setEntityList(null);
            fourthRecyclerView.setVisibility(View.INVISIBLE);
        }
    }

    public void onEventMainThread(DataSyncManager.DataSyncEvent event) {
        ZLogger.d(String.format("CategoryCascadeFragment: DataSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataSyncManager.DataSyncEvent.EVENT_ID_REFRESH_BACKEND_CATEGORYINFO) {
            //刷新供应商
            readCategoryInfoCache();
        }
    }

    /**
     * 加载商品类目
     */
    public void reload() {
        //加载后台类目树
        if (!readCategoryInfoCache()){
            DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_BACKEND_CATEGORYINFO);
        }
    }

    /**
     * 加载后台类目树
     * */
    private boolean readCategoryInfoCache(){
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_STOCKGOODS_CATEGORY);
        List<CategoryOption> cacheData = JSONArray.parseArray(cacheStr, CategoryOption.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d个后台商品类目", ACacheHelper.CK_STOCKGOODS_CATEGORY, cacheData.size()));

            if (rootListAdapter != null){
                rootListAdapter.setEntityList(cacheData);
            }

            return true;
        }
        if (rootListAdapter != null){
            rootListAdapter.setEntityList(null);
        }

        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_ENABLED, true);

        return false;
    }

}
