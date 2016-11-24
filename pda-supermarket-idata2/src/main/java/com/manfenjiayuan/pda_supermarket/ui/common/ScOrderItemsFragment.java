package com.manfenjiayuan.pda_supermarket.ui.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import butterknife.BindView;
import de.greenrobot.event.EventBus;


/**
 * 商城订单——订单明细
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ScOrderItemsFragment extends BaseFragment {

    @BindView(R.id.googs_list)
    RecyclerViewEmptySupport chainRecyclerView;
    private ScOrderItemAdapter goodsAdapter;
    private LinearLayoutManager linearLayoutManager;
    @BindView(R.id.animProgress)
    ProgressBar progressBar;
    @BindView(R.id.empty_view)
    View emptyView;

    private ScOrder mScOrder;

    public static ScOrderItemsFragment newInstance(Bundle args) {
        ScOrderItemsFragment fragment = new ScOrderItemsFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }


//    @Override
//    protected boolean isResponseBackPressed() {
//        return false;
//    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_scorder_items;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//            barcode = args.getString(EXTRA_KEY_BARCODE);
//        }

        initRecyclerView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 验证
     */
    public void onEventMainThread(ScOrderEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();

        ZLogger.d(String.format("ScOrderEvent(%d)", eventId));
        switch (eventId) {
            case ScOrderEvent.EVENT_ID_UPDATE: {
                ScOrder scOrder = (ScOrder) args.getSerializable(ScOrderEvent.EXTRA_KEY_SCORDER);
                refresh(scOrder);
            }
            break;
        }
    }

    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chainRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        chainRecyclerView.setHasFixedSize(true);
        //添加分割线
        chainRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
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
//                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
//                int totalItemCount = linearLayoutManager.getItemCount();
//                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
//                // dy>0 表示向下滑动
////                ZLogger.d(String.format("%s %d(%d)", (dy > 0 ? "向上滚动" : "向下滚动"), lastVisibleItem, totalItemCount));
//                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
//                    if (!isLoadingMore) {
//                        loadMore();
//                    }
//                } else if (dy < 0) {
//                    isLoadingMore = false;
//                }
            }
        });

        goodsAdapter = new ScOrderItemAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new ScOrderItemAdapter.OnAdapterListener() {
            @Override
            public void onDataSetChanged(boolean needScroll) {
                progressBar.setVisibility(View.GONE);
            }
        });

        chainRecyclerView.setAdapter(goodsAdapter);
    }

    private void refresh(ScOrder scOrder) {
        mScOrder = scOrder;
        goodsAdapter.setEntityList(scOrder != null ? scOrder.getItems() : null);
    }
}
