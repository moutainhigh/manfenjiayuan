package com.manfenjiayuan.cashierdisplay.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manfenjiayuan.cashierdisplay.AppContext;
import com.manfenjiayuan.cashierdisplay.R;
import com.manfenjiayuan.cashierdisplay.bean.CashierOrderInfo;
import com.manfenjiayuan.cashierdisplay.ui.adapter.OrderItemsAdapter;
import com.manfenjiayuan.cashierdisplay.ui.view.LineItemDecoration;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.bingshanguxue.vector_uikit.widget.AvatarView;

import butterknife.Bind;

/**
 * Created by bingshanguxue on 16/3/25.
 */
public class PayFragment extends BaseFragment {
    @Bind(R.id.iv_member_header)
    AvatarView ivMemberHeader;
    @Bind(R.id.tv_member_balance)
    TextView tvMemberBalance;
    @Bind(R.id.tv_member_score)
    TextView tvMemberScore;

    @Bind(R.id.tv_handle_amount)
    TextView tvHandleAmount;
    @Bind(R.id.tv_total_amount)
    TextView tvTotalAmount;
    @Bind(R.id.tv_discount_amount)
    TextView tvDiscountAmount;
    @Bind(R.id.tv_coupon_amount)
    TextView tvCouponAmount;
    @Bind(R.id.tv_paid_amount)
    TextView tvPaidAmount;
    @Bind(R.id.tv_coupon)
    TextView tvCouponInfo;

    @Bind(R.id.frame_coupon)
    RelativeLayout frameCoupon;
    @Bind(R.id.coupon_list)
    RecyclerView couponRecyclerView;
    private OrderItemsAdapter orderItemsAdapter;

    private CashierOrderInfo cashierOrderInfo;

    public static PayFragment newInstance(Bundle args) {
        PayFragment fragment = new PayFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            cashierOrderInfo = (CashierOrderInfo) args.getSerializable("cashierOrderInfo");
        }

        initCouponRecyclerView();

        refresh();
    }

    private void refresh() {
        ZLogger.d("显示订单信息");
        try {
            if (cashierOrderInfo != null) {
                tvHandleAmount.setText(String.format("%.2f", cashierOrderInfo.getHandleAmount()));
                tvTotalAmount.setText(String.format("%.2f", cashierOrderInfo.getRetailAmount()));
                tvDiscountAmount.setText(String.format("%.2f", cashierOrderInfo.getDiscountAmount()));
                tvCouponAmount.setText(String.format("%.2f", cashierOrderInfo.getCouponDiscountAmount()));
                tvPaidAmount.setText(String.format("%.2f", cashierOrderInfo.getPaidAmount()));
                String couponsIds = cashierOrderInfo.getCouponsIds();
                if (!StringUtils.isEmpty(couponsIds)){
                    tvCouponInfo.setText(String.format("使用了%d张优惠券，优惠金额%.2f元",
                            couponsIds.lastIndexOf(",") + 1, cashierOrderInfo.getCouponDiscountAmount()));
                }
                else{
                    tvCouponInfo.setText("");
                }
            }
        } catch (Exception e) {
            ZLogger.e("显示订单信息失败:" + e.toString());
        }
        //TODO,领取优惠券
    }

    /**
     * 初始化商品列表
     */
    private void initCouponRecyclerView() {
        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AppContext.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        couponRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        couponRecyclerView.setHasFixedSize(true);
        //设置Item增加、移除动画
//        itemsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //分割线
        couponRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));

        orderItemsAdapter = new OrderItemsAdapter(getActivity(), null);
        orderItemsAdapter.setOnAdapterListener(new OrderItemsAdapter.OnAdapterListener() {

            @Override
            public void onDataSetChanged(boolean needScroll) {
            }
        });

        // specify an adapter
        couponRecyclerView.setAdapter(orderItemsAdapter);
    }


}


