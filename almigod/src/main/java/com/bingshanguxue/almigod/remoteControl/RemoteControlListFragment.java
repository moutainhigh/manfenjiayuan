package com.bingshanguxue.almigod.remoteControl;

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

import com.bingshanguxue.almigod.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.anlaysis.remoteControl.RemoteControl;
import com.mfh.framework.anlaysis.remoteControl.RemoteControlClient;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 远程控制
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class RemoteControlListFragment extends BaseListFragment<CompanyInfo> {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private RemoteControlAdapter companyAdapter;

    @BindView(R.id.empty_view)
    View emptyView;
    @BindView(R.id.animProgress)
    ProgressBar progressBar;


    public static RemoteControlListFragment newInstance(Bundle args) {
        RemoteControlListFragment fragment = new RemoteControlListFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_poslist;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MAX_SYNC_PAGESIZE = 30;
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//            abilityItem = args.getInt(EXTRA_KEY_ABILITY_ITEM, AbilityItem.TENANT);
//        }
        ZLogger.d("进入POS设备列表页面 开始");
        mToolbar.setTitle("选择远程指令");
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
        ZLogger.d("进入POS设备列表页面 结束");
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

        companyAdapter = new RemoteControlAdapter(getActivity(), null);
        companyAdapter.setOnAdapterLitener(new RemoteControlAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                RemoteControl remoteControl = companyAdapter.getEntity(position);
                if (remoteControl != null){
                    Intent data = new Intent();
                    data.putExtra("remoteControl", companyAdapter.getEntity(position));

                    getActivity().setResult(Activity.RESULT_OK, data);
                    getActivity().finish();
                }
                else{
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }

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
    @OnClick(R.id.empty_view)
    public void reload() {
        companyAdapter.setEntityList(RemoteControlClient.getInstance().generateRemoteControls());
    }


}
