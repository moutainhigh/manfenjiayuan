package com.manfenjiayuan.pda_supermarket.ui.fragment.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;


/**
 * 产品SKU批发商列表
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GoodsChainFragment extends BaseFragment implements IChainGoodsSkuView {

    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private ChainGoodsSkuAdapter goodsAdapter;
    @Bind(R.id.animProgress)
    ProgressBar animProgress;
    @Bind(R.id.empty_view)
    View emptyView;

    private ChainGoodsSkuPresenter mChainGoodsSkuPresenter;

    public static GoodsChainFragment newInstance(Bundle args) {
        GoodsChainFragment fragment = new GoodsChainFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected boolean isResponseBackPressed() {
        return false;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_goods_chain;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        mChainGoodsSkuPresenter = new ChainGoodsSkuPresenter(this);
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
    public void onEventMainThread(ScGoodsSkuEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();

        ZLogger.d(String.format("ScGoodsSkuEvent(%d)", eventId));
        switch (eventId) {
            case ScGoodsSkuEvent.EVENT_ID_SKU_UPDATE: {
                ScGoodsSku curGoods = (ScGoodsSku) args.getSerializable("scGoodsSku");
                reload(curGoods);
            }
            break;

        }
    }

    private void reload(ScGoodsSku goods){
        if (goods != null){
            if (!NetWorkUtil.isConnect(getActivity())) {
                DialogUtil.showHint(R.string.toast_network_error);
                return;
            }

            mChainGoodsSkuPresenter.findSupplyChainGoodsSku(goods.getBarcode(), null, null);
        }
        else{
            goodsAdapter.setEntityList(null);
        }
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        addressRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        addressRecyclerView.setHasFixedSize(true);
        //添加分割线
//        addressRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        addressRecyclerView.setEmptyView(emptyView);

        goodsAdapter = new ChainGoodsSkuAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new ChainGoodsSkuAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onItemLongClick(View view, final int position) {
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
                animProgress.setVisibility(View.GONE);
            }
        });

        addressRecyclerView.setAdapter(goodsAdapter);
    }


    @Override
    public void onChainGoodsSkuViewProcess() {
        animProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onChainGoodsSkuViewError(String errorMsg) {
        animProgress.setVisibility(View.GONE);
        goodsAdapter.setEntityList(null);
    }

    @Override
    public void onChainGoodsSkuViewSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
        animProgress.setVisibility(View.GONE);
        goodsAdapter.setEntityList(dataList);
    }

    @Override
    public void onChainGoodsSkuViewSuccess(ChainGoodsSku data) {
        animProgress.setVisibility(View.GONE);
    }
}
