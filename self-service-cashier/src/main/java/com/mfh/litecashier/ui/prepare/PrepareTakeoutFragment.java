package com.mfh.litecashier.ui.prepare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.bingshanguxue.cashier.CashierAgent;
import com.bingshanguxue.cashier.model.CashierOrderInfo;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 第三方系统货确认
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PrepareTakeoutFragment extends BaseFragment {
    public static final String EXTRA_KEY_POS_TRADENO = "posTradeNo";
    public static final String EXTRA_KEY_SUBTYPE = "subType";
    public static final String EXTRA_KEY_OUTTER_TRADENO = "outerTradeNo";


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_vip_header)
    ImageView ivMemberHeader;
    @BindView(R.id.tv_vip_brief)
    TextView tvVipBrief;
    @BindView(R.id.label_quantity)
    MultiLayerLabel labelQuantity;
    @BindView(R.id.label_amount)
    MultiLayerLabel labelAmount;
    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    @BindView(R.id.empty_view)
    View emptyView;
    private PrepareTakeoutAdapter couponAdapter;
    @BindView(R.id.fab_submit)
    ImageButton btnSubmit;

    private String posTradeNo = null;
    private BizSubTypeWrapper mBizSubTypeWrapper = null;
    private String outterTradeNo = null;
    private List<CashierShopcartEntity> mShopcartEntities;

    public static PrepareTakeoutFragment newInstance(Bundle args) {
        PrepareTakeoutFragment fragment = new PrepareTakeoutFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pick_takeout;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        try {
            Bundle args = getArguments();
            ZLogger.d(String.format("打开第三方组货页面，%s", StringUtils.decodeBundle(args)));
            if (args != null) {
                posTradeNo = args.getString(EXTRA_KEY_POS_TRADENO);
                mBizSubTypeWrapper = (BizSubTypeWrapper) args.getSerializable(EXTRA_KEY_SUBTYPE);
                outterTradeNo = args.getString(EXTRA_KEY_OUTTER_TRADENO);
            }

            toolbar.setTitle("组货");
//        setSupportActionBar(toolbar);
            // Set an OnMenuItemClickListener to handle menu item clicks
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // Handle the menu item
                    int id = item.getItemId();
                    if (id == R.id.action_close) {
                        getActivity().setResult(Activity.RESULT_CANCELED);
                        getActivity().finish();
                    }
                    return true;
                }
            });

            // Inflate a menu to be displayed in the
            toolbar.inflateMenu(R.menu.menu_normal);

            initgoodsRecyclerView();

            refresh();
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.ef(e.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        goodsRecyclerView.requestFocus();
    }

    private void initgoodsRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);
        //分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));

        couponAdapter = new PrepareTakeoutAdapter(getContext(), null);
        couponAdapter.setOnAdapterListener(new PrepareTakeoutAdapter.OnAdapterListener() {


            @Override
            public void onDataSetChanged(boolean needScroll) {

            }

        });
        goodsRecyclerView.setAdapter(couponAdapter);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_normal, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void refresh() {
        if (mBizSubTypeWrapper == null || StringUtils.isEmpty(posTradeNo)
                || StringUtils.isEmpty(outterTradeNo)) {
            DialogUtil.showHint("订单数据错误");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        } else {
            tvVipBrief.setText(outterTradeNo);
            ivMemberHeader.setImageResource(mBizSubTypeWrapper.getResId());
            mShopcartEntities = CashierShopcartService.getInstance()
                    .queryAllBy(String.format("posTradeNo = '%s'", posTradeNo));

            Double bcount = 0D, amount = 0D;
            if (mShopcartEntities != null && mShopcartEntities.size() > 0) {
                for (CashierShopcartEntity entity : mShopcartEntities) {
                    bcount += entity.getBcount();
                    amount += entity.getFinalAmount();
                }
            }
            labelQuantity.setTopText(MUtils.formatDouble(bcount, ""));
            labelAmount.setTopText(MUtils.formatDouble(amount, ""));

            couponAdapter.setEntityList(mShopcartEntities);
        }
    }

    @OnClick(R.id.fab_submit)
    public void submitOrder() {
        btnSubmit.setEnabled(false);
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "组货中...", true);

        CashierOrderInfo cashierOrderInfo = CashierAgent.settle(mBizSubTypeWrapper.getSubType(),
                posTradeNo, outterTradeNo,
                PosOrderEntity.ORDER_STATUS_FINISH, mShopcartEntities);

        hideProgressDialog();

        if (cashierOrderInfo != null) {
            Intent data = new Intent();
            data.putExtra("isTakeOutOrder", true);
            data.putExtra("posTradeNo", posTradeNo);
            getActivity().setResult(Activity.RESULT_OK, data);
            getActivity().finish();
        } else {
            DialogUtil.showHint("组货失败");
        }
    }

}
