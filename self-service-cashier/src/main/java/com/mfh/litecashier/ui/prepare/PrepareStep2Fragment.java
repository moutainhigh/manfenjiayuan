package com.mfh.litecashier.ui.prepare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderApiImpl;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.OnClick;

/**
 * 组货确认
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PrepareStep2Fragment extends BaseFragment {
    public static final String EXTRA_KEY_SCORDER = "scOrder";

//    @Bind(R.id.toolbar)
    Toolbar toolbar;
//    @Bind(R.id.iv_vip_header)
    AvatarView ivMemberHeader;
//    @Bind(R.id.tv_vip_brief)
    TextView tvVipBrief;
//    @Bind(R.id.label_orderamount)
    MultiLayerLabel labelOrderAmount;
//    @Bind(R.id.label_finalamount)
    MultiLayerLabel labelActualAmount;
//    @Bind(R.id.label_diffamount)
    MultiLayerLabel labelDiffAmount;
//    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
//    @Bind(R.id.empty_view)
    View emptyView;
    private PrepareOrderAdapter couponAdapter;
//    @Bind(R.id.fab_submit)
    FloatingActionButton btnSubmit;

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
        try{
            Bundle args = getArguments();
            ZLogger.df(String.format("打开组货页面，%s", StringUtils.decodeBundle(args)));
            if (args != null) {
                mScOrder = (ScOrder) args.getSerializable(EXTRA_KEY_SCORDER);
            }

            toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            ivMemberHeader = (AvatarView) rootView.findViewById(R.id.iv_vip_header);
            tvVipBrief = (TextView) rootView.findViewById(R.id.tv_vip_brief);
            labelOrderAmount = (MultiLayerLabel) rootView.findViewById(R.id.label_orderamount);
            labelActualAmount = (MultiLayerLabel) rootView.findViewById(R.id.label_finalamount);
            labelDiffAmount = (MultiLayerLabel) rootView.findViewById(R.id.label_diffamount);
            goodsRecyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.goods_list);
            emptyView = rootView.findViewById(R.id.empty_view);
            btnSubmit = (FloatingActionButton) rootView.findViewById(R.id.fab_submit);

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
        }
        catch (Exception e){
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
            if (diff > 0){
                labelDiffAmount.setTopText(String.format("+%.2f", Math.abs(diff)),
                        ContextCompat.getColor(getContext(), R.color.material_red_500));
            }
            else{
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
     * 加载优惠券列表
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

        ScOrderApiImpl.updateCommitInfo(mScOrder.getId(), jsonArray.toJSONString(), updateCommitInfoRC);
    }

    NetCallBack.NetTaskCallBack updateCommitInfoRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"操作成功!","version":"1","data":null}
                    if (rspData != null){
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String retStr = retValue.getValue();
                    }

                    prepareOrder();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    btnSubmit.setEnabled(true);
                    hideProgressDialog();
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };

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

        ScOrderApiImpl.prepareOrder(mScOrder.getId(), mScOrder.getGuideHumanId(), null, prepareOrderRC);
    }

    //保存
    NetCallBack.NetTaskCallBack prepareOrderRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"14.0"}}
//                        {"code":"0","msg":"查询成功!","version":"1","data":[6.0,6.0]}
                    btnSubmit.setEnabled(true);
                    hideProgressDialog();
                    DialogUtil.showHint("组货成功");
                    Intent data = new Intent();
                    data.putExtra(EXTRA_KEY_SCORDER, mScOrder);
                    getActivity().setResult(Activity.RESULT_OK, data);
                    getActivity().finish();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.ef(errMsg);

                    btnSubmit.setEnabled(true);
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };


}
