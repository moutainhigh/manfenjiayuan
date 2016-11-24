package com.manfenjiayuan.mixicook_vip.ui.order;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.model.CouponRuleWrapper;
import com.mfh.framework.api.shoppingCart.ShoppingCart;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * 订单优惠券
 * Created by bingshanguxue on 6/28/16.
 */
public class OrderCouponsFragment extends BaseListFragment<ShoppingCart> {
    public static final String EXTRA_KEY_MARKETRULEBRIEF = "marketRuleBrief";


    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private OrderCouponsAdapter goodsListAdapter;
    private LinearLayoutManager mRLayoutManager;
    @BindView(R.id.empty_view)
    View emptyView;

    private MarketRuleBrief mMarketRuleBrief;


    public static OrderCouponsFragment newInstance(Bundle args) {
        OrderCouponsFragment fragment = new OrderCouponsFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_order_copons;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mMarketRuleBrief = (MarketRuleBrief) args.getSerializable(EXTRA_KEY_MARKETRULEBRIEF);
        }

        toolbar.setTitle("优惠券");
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_submit) {
                    submit();
//                    goodsListAdapter.setChecked(true);
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_submit);

        initGoodsRecyclerView();

        reload();
    }

    @Override
    public void reload() {
        super.reload();
        goodsListAdapter.digest(mMarketRuleBrief);
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

        goodsListAdapter = new OrderCouponsAdapter(AppContext.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new OrderCouponsAdapter.OnAdapterListener() {
////                                                  @Override
////                                                  public void onItemClick(View view, int position) {
//
//                                                  }

                                                  @Override
                                                  public void onDataSetChanged() {
                                                      // TODO: 14/10/2016

//        MarketRuleBrief marketRuleBrief = new MarketRuleBrief();
                                                      List<Long> ruleIds = new ArrayList<>();
                                                      List<Long> couponIds = new ArrayList<>();
                                                      List<CouponRuleWrapper> couponRuleWrappers = goodsListAdapter.getEntityList();
                                                      if (couponRuleWrappers != null && couponRuleWrappers.size() > 0){
                                                          for (CouponRuleWrapper wrapper : couponRuleWrappers){
                                                              if (CouponRuleWrapper.TYPE_RULE.equals(wrapper.getType())){
                                                                  ruleIds.add(wrapper.getId());
                                                              }
                                                              else{
                                                                  if (wrapper.isSelected()){
                                                                      couponIds.add(wrapper.getCouponsId());
                                                                  }
                                                              }
                                                          }
                                                      }
                                                      mMarketRuleBrief.setCouponIds(couponIds);
                                                      mMarketRuleBrief.setRuleIds(ruleIds);

                                                  }
                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
        hideProgressDialog();
    }

    private void submit(){
        Intent data = new Intent();
        data.putExtra(EXTRA_KEY_MARKETRULEBRIEF, mMarketRuleBrief);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

}
