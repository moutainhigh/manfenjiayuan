package com.manfenjiayuan.pda_supermarket.ui.goods;

import android.app.Activity;
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
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;


/**
 * 产品SKU批发商列表
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ChainGoodsSkuFragment extends BaseFragment implements IChainGoodsSkuView {

    public final static String EXTRA_KEY_BARCODE = "barCode";

    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private ChainGoodsSkuAdapter goodsAdapter;
    @Bind(R.id.animProgress)
    ProgressBar animProgress;
    @Bind(R.id.empty_view)
    View emptyView;

    private String barcode;
    private ChainGoodsSkuPresenter mChainGoodsSkuPresenter;

    public static ChainGoodsSkuFragment newInstance(Bundle args) {
        ChainGoodsSkuFragment fragment = new ChainGoodsSkuFragment();

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
        return R.layout.fragment_chain_goodssku;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mChainGoodsSkuPresenter = new ChainGoodsSkuPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            barcode = args.getString(EXTRA_KEY_BARCODE);
        }

        initRecyclerView();

        if (StringUtils.isEmpty(barcode)) {
            DialogUtil.showHint("条码无效");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        } else {
            if (!NetWorkUtil.isConnect(getActivity())) {
                DialogUtil.showHint(R.string.toast_network_error);
                return;
            }

            mChainGoodsSkuPresenter.findSupplyChainGoodsSku(barcode, null, null);
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
