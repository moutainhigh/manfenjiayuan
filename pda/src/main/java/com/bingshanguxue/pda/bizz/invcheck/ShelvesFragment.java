package com.bingshanguxue.pda.bizz.invcheck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bingshanguxue.pda.R;
import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.mfh.framework.api.account.Office;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;


/**
 * 选择货架
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ShelvesFragment extends BaseListFragment<Office> {

//    @Bind(R.id.toolbar)
    Toolbar mToolbar;
//    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport mRecyclerView;

    private ShelfnumberAdapter companyAdapter;

//    @Bind(R.id.empty_view)
    View emptyView;
//    @Bind(R.id.animProgress)
    ProgressBar progressBar;

    public static ShelvesFragment newInstance(Bundle args) {
        ShelvesFragment fragment = new ShelvesFragment();

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
//            abilityItem = args.getInt(EXTRA_KEY_ABILITY_ITEM, AbilityItem.TENANT);
//        }

        mToolbar.setTitle("选择区域");
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
        GridLayoutManager mRLayoutManager = new GridLayoutManager(getContext(), 4);
        mRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //添加分割线
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
        //设置列表为空时显示的视图
        mRecyclerView.setEmptyView(emptyView);

        companyAdapter = new ShelfnumberAdapter(getActivity(), null);
        companyAdapter.setOnAdapterLitener(new ShelfnumberAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent data = new Intent();
                data.putExtra("shelfNumber", companyAdapter.getEntity(position));

                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
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
        List<Shelfnumber> localList = new ArrayList<>();
        for (int i = 1; i < 50; i++) {
            localList.add(Shelfnumber.newInstance((long) i));
        }
        if (companyAdapter != null) {
            companyAdapter.setEntityList(localList);
        }
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        onLoadFinished();
    }
}
