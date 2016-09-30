package com.manfenjiayuan.mixicook_vip.ui.shopcart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.FragmentActivity;
import com.manfenjiayuan.mixicook_vip.ui.SimpleActivity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.shoppingCart.CartPack;
import com.mfh.framework.api.shoppingCart.ShoppingCart;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by bingshanguxue on 6/28/16.
 */
public class ShopcartFragment extends BaseListFragment<ShoppingCart> implements IShopcartView {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private ShopcartGoodsAdapter goodsListAdapter;
    private LinearLayoutManager mRLayoutManager;
    @Bind(R.id.empty_view)
    TextView emptyView;

    @Bind(R.id.tv_brief)
    TextView tvBrief;
    @Bind(R.id.button_confirm)
    Button btnConfirm;

    private ShopcartPresenter mShopcartPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

        mShopcartPresenter = new ShopcartPresenter(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_shopcart;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        toolbar.setTitle("购物车");
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        initGoodsRecyclerView();

        reload();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0: {
                if (resultCode == Activity.RESULT_OK) {
                    showProgressDialog(ProgressDialog.STATUS_DONE, "预定成功", true);

                    reload();
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void reload() {
        super.reload();
        mShopcartPresenter.list(null, null);
//        List<PurchaseShopcartEntity> entities = PurchaseShopcartService.getInstance().fetchFreshEntites();
//        goodsListAdapter.setEntityList(entities);
//
//        DialogUtil.showHint("购物车" + entities.size());
    }

    @OnClick(R.id.button_confirm)
    public void redirect2OrderFragment() {
        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "确认订单");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_CONFIRM_ORDER);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, 0);
    }


    private void initGoodsRecyclerView() {
        mRLayoutManager = new LinearLayoutManager(AppContext.getAppContext());
        mRLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
//        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);
        goodsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                int lastVisibleItem = mRLayoutManager.findLastVisibleItemPosition();
//                int totalItemCount = mRLayoutManager.getItemCount();
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

        goodsListAdapter = new ShopcartGoodsAdapter(AppContext.getAppContext(), null);
        goodsListAdapter.setOnAdapterListsner(new ShopcartGoodsAdapter.OnAdapterListener() {
                                                  @Override
                                                  public void onItemClick(View view, int position) {

                                                  }

                                                  @Override
                                                  public void onDataSetChanged() {
                                                      int count = goodsListAdapter.getItemCount();
                                                      if (count > 0) {
                                                          tvBrief.setText(String.format("商品数：%d", count));
                                                          btnConfirm.setEnabled(true);
                                                      } else {
                                                          tvBrief.setText("暂无商品");
                                                          btnConfirm.setEnabled(false);
                                                      }
                                                  }
                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

    @Override
    public void onIShopcartViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        onLoadStart();
    }

    @Override
    public void onIShopcartViewError(String errorMsg) {
        if (!StringUtils.isEmpty(errorMsg)){
            ZLogger.df(errorMsg);
        }

        hideProgressDialog();
        onLoadFinished();
    }

    @Override
    public void onIShopcartViewSuccess(PageInfo pageInfo, List<ShoppingCart> dataList) {
        try {
            mPageInfo = pageInfo;

            List<CartPack> cartPacks = new ArrayList<>();
            if (dataList != null && dataList.size() > 0) {
                for (ShoppingCart shoppingCart : dataList) {
                    List<CartPack> products = shoppingCart.getProducts();
                    if (products != null) {
                        cartPacks.addAll(products);
                    }
                }
            }
            //第一页，缓存数据
            if (mPageInfo.getPageNo() == 1) {
                ZLogger.d("缓存商品收货订单第一页数据");

                if (goodsListAdapter != null) {
                    goodsListAdapter.setEntityList(cartPacks);
                }
            } else {
                if (goodsListAdapter != null) {
                    goodsListAdapter.appendEntityList(cartPacks);
                }
            }

            onLoadFinished();
        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载商品收货订单失败: %s", ex.toString()));
            onLoadFinished();
        }
    }
}
