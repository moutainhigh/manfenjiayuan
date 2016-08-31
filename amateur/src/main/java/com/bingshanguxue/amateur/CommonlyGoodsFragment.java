package com.mfh.litecashier.ui.fragment.cashier;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.entity.CommonlyGoodsEntity;
import com.mfh.litecashier.database.logic.CommonlyGoodsService;
import com.mfh.litecashier.event.AddCommonlyGoodsEvent;
import com.mfh.litecashier.event.CommonlyGoodsEvent;
import com.mfh.litecashier.ui.adapter.CommonlyGoodsAdapter;
import com.mfh.litecashier.ui.dialog.DoubleInputDialog;
import com.mfh.framework.uikit.recyclerview.GridItemDecoration2;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 常用商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public class CommonlyGoodsFragment extends BaseFragment {
    @Bind(R.id.option_list)
    RecyclerViewEmptySupport mRecyclerView;

    @Bind(R.id.animProgress)
    ProgressBar animProgress;
    @Bind(R.id.empty_view)
    TextView emptyView;

    GridLayoutManager mRLayoutManager;
    private CommonlyGoodsAdapter adapter;

    private Long categoryId;

    private DoubleInputDialog changeQuantityDialog = null;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_measure;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        //for Fragment.instantiate
        Bundle args = getArguments();
        if (args != null) {
            categoryId = args.getLong("categoryId", 0L);
        }

        initRecyclerView();

        load();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

        if (adapter != null) {
            adapter.setEntityList(null);
            adapter = null;
        }
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(CommonlyGoodsEvent event) {
        ZLogger.d(String.format("CommonlyGoodsFragment: CommonlyGoodsEvent(%d)", event.getAffairId()));
        if (event.getAffairId() == CommonlyGoodsEvent.EVENT_ID_RELOAD_DATA) {
            load();
        }
        else if (event.getAffairId() == CommonlyGoodsEvent.EVENT_ID_NORMAL_STATUS) {
            if (adapter != null) {
                adapter.setbRemoved(false);
            }
        }
        else if (event.getAffairId() == CommonlyGoodsEvent.EVENT_ID_REMOVE_STATUS) {
            if (adapter != null) {
                adapter.setbRemoved(true);
            }
        }
    }

    private void initRecyclerView() {
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
        //添加分割线
        mRecyclerView.addItemDecoration(new GridItemDecoration2(CashierApp.getAppContext(), 1,
                getActivity().getResources().getColor(R.color.mf_dividerColorPrimary), 0,
                getActivity().getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f,
                getActivity().getResources().getColor(R.color.mf_dividerColorPrimary), 0));

        adapter = new CommonlyGoodsAdapter(getActivity(), null);
        adapter.setOnAdapterLitener(new CommonlyGoodsAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                EventBus.getDefault().post(new AddCommonlyGoodsEvent(adapter.getEntity(position)));
            }

            @Override
            public void onItemLongClick(View view, final int position) {

                final CommonlyGoodsEntity goods = adapter.getEntity(position);
                if (goods == null) {
                    return;
                }

                if (changeQuantityDialog == null) {
                    changeQuantityDialog = new DoubleInputDialog(getActivity());
                    changeQuantityDialog.setCancelable(true);
                    changeQuantityDialog.setCanceledOnTouchOutside(true);
                }
                changeQuantityDialog.init("修改价格", 2, goods.getCostPrice(), new DoubleInputDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double quantity) {
                        goods.setCostPrice(quantity);
                        CommonlyGoodsService.get().saveOrUpdate(goods);
                        adapter.notifyItemChanged(position);
                    }
                });
                changeQuantityDialog.show();
            }

            @Override
            public void onDataSetChanged() {
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * 加载指定类目商品
     */
    private void load() {
        List<CommonlyGoodsEntity> entityList;
        ZLogger.d(String.format("load: categoryId=%d", categoryId));

        //查询全部
        if (categoryId == null || categoryId.compareTo(0L) == 0) {
            entityList = CommonlyGoodsService.get().queryAllByDesc(null);
        }
        //查询指定类目
        else {
            entityList = CommonlyGoodsService.get().queryAllByDesc(String.format("categoryId = '%d'", categoryId));
        }
        adapter.setEntityList(entityList);
    }

}
