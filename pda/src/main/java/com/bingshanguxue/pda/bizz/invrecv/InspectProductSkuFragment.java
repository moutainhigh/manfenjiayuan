package com.bingshanguxue.pda.bizz.invrecv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSONArray;
import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.utils.ACacheHelper;
import com.mfh.framework.api.anon.ProductSku;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * 验收商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InspectProductSkuFragment extends BaseListFragment<ChainGoodsSku> {

    //    @Bind(R.id.toolbar)
    Toolbar mToolbar;
//    @Bind(R.id.office_list)
    RecyclerViewEmptySupport chainRecyclerView;
    private ProductSkuAdapter goodsAdapter;
    private LinearLayoutManager linearLayoutManager;
//    @Bind(R.id.animProgress)
    ProgressBar progressBar;
//    @Bind(R.id.empty_view)
    View emptyView;

    public static InspectProductSkuFragment newInstance(Bundle args) {
        InspectProductSkuFragment fragment = new InspectProductSkuFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inspect_productsku;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//            barcode = args.getString(EXTRA_KEY_BARCODE);
//        }
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        chainRecyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.office_list);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        emptyView = rootView.findViewById(R.id.empty_view);

        mToolbar.setTitle("平台商品档案");
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
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


    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chainRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        chainRecyclerView.setHasFixedSize(true);
        //添加分割线
//        chainRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        chainRecyclerView.setEmptyView(emptyView);
        chainRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        goodsAdapter = new ProductSkuAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new ProductSkuAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                ProductSku productSku = goodsAdapter.getEntity(position);
                if (productSku != null){
                    Intent data = new Intent();
                    data.putExtra("productSku", productSku);
                    getActivity().setResult(Activity.RESULT_OK, data);
                    getActivity().finish();
                }
                else{
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
            }

            @Override
            public void onItemLongClick(View view, final int position) {
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
                progressBar.setVisibility(View.GONE);
            }
        });

        chainRecyclerView.setAdapter(goodsAdapter);
    }


    /**
     * 重新加载数据
     */
//    @OnClick(R.id.empty_view)
    public void reload() {
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.INVRECV_INSPECT_GOODS_TEMPDATA);
        List<ProductSku> cacheData = JSONArray.parseArray(cacheStr, ProductSku.class);
        goodsAdapter.setEntityList(cacheData);
        ACacheHelper.remove(ACacheHelper.INVRECV_INSPECT_GOODS_TEMPDATA);
    }
}
