package com.mfh.litecashier.ui.prepare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.ExceptionHandle;
import com.mfh.framework.rxapi.httpmgr.ScOrderHttpManager;
import com.mfh.framework.rxapi.subscriber.MSubscriber;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * 组货确认
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PrepareStep2Fragment extends BaseFragment {
    public static final String EXTRA_KEY_SCORDER = "scOrder";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_vip_header)
    AvatarView ivMemberHeader;
    @BindView(R.id.tv_vip_brief)
    TextView tvVipBrief;
    @BindView(R.id.label_orderamount)
    MultiLayerLabel labelOrderAmount;
    @BindView(R.id.label_finalamount)
    MultiLayerLabel labelActualAmount;
    @BindView(R.id.label_diffamount)
    MultiLayerLabel labelDiffAmount;
    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    @BindView(R.id.empty_view)
    View emptyView;
    private PrepareOrderAdapter couponAdapter;
    @BindView(R.id.fab_submit)
    ImageButton btnSubmit;

    private ScOrder mScOrder;

    public static PrepareStep2Fragment newInstance(Bundle args) {
        PrepareStep2Fragment fragment = new PrepareStep2Fragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pick_step2;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        try {
            Bundle args = getArguments();
            ZLogger.d(String.format("打开平台组货页面，%s", StringUtils.decodeBundle(args)));
            if (args != null) {
                mScOrder = (ScOrder) args.getSerializable(EXTRA_KEY_SCORDER);
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

            // Inflate a menu to be displayed in the toolbar
            toolbar.inflateMenu(R.menu.menu_normal);
            ivMemberHeader.setBorderWidth(3);
            ivMemberHeader.setBorderColor(Color.parseColor("#e8e8e8"));

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

        couponAdapter = new PrepareOrderAdapter(getContext(), null);
        couponAdapter.setOnAdapterListener(new PrepareOrderAdapter.OnAdapterListener() {


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
        if (mScOrder == null) {
            DialogUtil.showHint("订单数据错误");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        } else {
            List<ScOrderItem> items = mScOrder.getItems();
            Double amount = 0D, actualAmount = 0D;
            if (items != null && items.size() > 0) {
                for (ScOrderItem item : items) {
                    amount += MathCompact.mult(item.getPrice(), item.getBcount());
                    actualAmount += MathCompact.mult(item.getPrice(), item.getQuantityCheck());
                }
            }
            couponAdapter.setEntityList(items);

            ivMemberHeader.setAvatarUrl(mScOrder.getServiceHumanImg());
            tvVipBrief.setText(String.format("%s/%s",
                    mScOrder.getReceiveName(), mScOrder.getReceivePhone()));

            labelOrderAmount.setTopText(String.format("%.2f", amount));
            labelActualAmount.setTopText(String.format("%.2f", actualAmount));
            Double diff = actualAmount - amount;
            if (diff > 0) {
                labelDiffAmount.setTopText(String.format("+%.2f", Math.abs(diff)),
                        ContextCompat.getColor(getContext(), R.color.material_red_500));
            } else {
                labelDiffAmount.setTopText(String.format("-%.2f", Math.abs(diff)),
                        ContextCompat.getColor(getContext(), R.color.material_green_500));
            }
        }
    }

    @OnClick(R.id.fab_submit)
    public void submitOrder() {
        btnSubmit.setEnabled(false);

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        updateCommitInfoWhenPrepaired();
    }

    /**
     * 确认组货
     */
    public void updateCommitInfoWhenPrepaired() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "组货中...", false);
        btnSubmit.setEnabled(false);

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        JSONArray jsonArray = new JSONArray();
        List<ScOrderItem> items = mScOrder.getItems();
        if (items != null && items.size() > 0) {
            for (ScOrderItem item : items) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("skuId", item.getSkuId());
                jsonObject.put("bcount", item.getQuantityCheck());
                jsonArray.add(jsonObject);
            }
        }

        Map<String, String> options = new HashMap<>();
        options.put("id", String.valueOf(mScOrder.getId()));
        options.put("jsonStr", jsonArray.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        ScOrderHttpManager.getInstance().updateCommitInfo(options,
                new MSubscriber<String>() {

//                    @Override
//                    public void onError(Throwable e) {
//                        DialogUtil.showHint(e.getMessage());
//                        btnSubmit.setEnabled(true);
//                        hideProgressDialog();
//                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {

                        DialogUtil.showHint(e.getMessage());
                        btnSubmit.setEnabled(true);
                        hideProgressDialog();
                    }

                    @Override
                    public void onNext(String s) {
                        ZLogger.d("发货并通知骑手:" + s);
                        prepareOrder();
                    }
                });
    }

    /**
     * 计算会员/优惠券优惠金额
     */
    private void prepareOrder() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "组货中...", true);
        btnSubmit.setEnabled(false);

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("orderId", String.valueOf(mScOrder.getId()));
        if (mScOrder.getGuideHumanId() != null) {
            options.put("buyerId", String.valueOf(mScOrder.getGuideHumanId()));
        }
//            if (transHumanId != null){
//                options.put("transHumanId", String.valueOf(transHumanId));
//            }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        ScOrderHttpManager.getInstance().prepareOrder(options,
                new MSubscriber<String>() {
//                    @Override
//                    public void onError(Throwable e) {
//                        ZLogger.ef(e.toString());
//
//                        btnSubmit.setEnabled(true);
//                        showProgressDialog(ProgressDialog.STATUS_ERROR, e.getMessage(), true);
//                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {

                        ZLogger.ef(e.toString());

                        btnSubmit.setEnabled(true);
                        showProgressDialog(ProgressDialog.STATUS_ERROR, e.getMessage(), true);
                    }

                    @Override
                    public void onNext(String s) {
                        btnSubmit.setEnabled(true);
                        hideProgressDialog();
                        DialogUtil.showHint("组货成功");
                        Intent data = new Intent();
                        data.putExtra(EXTRA_KEY_SCORDER, mScOrder);
                        getActivity().setResult(Activity.RESULT_OK, data);
                        getActivity().finish();
                    }
                });
    }

}
