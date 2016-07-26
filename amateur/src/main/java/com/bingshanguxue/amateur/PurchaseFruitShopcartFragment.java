package com.mfh.litecashier.ui.fragment.purchase;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.impl.CashierApiImpl;
import com.mfh.framework.api.impl.InvSendOrderApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.SubdisCode;
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartSplitOrder;
import com.mfh.litecashier.database.entity.PurchaseShopcartEntity;
import com.mfh.litecashier.database.logic.PurchaseShopcartService;
import com.mfh.litecashier.event.PurchaseShopcartSyncEvent;
import com.mfh.litecashier.ui.adapter.PurchaseFruitOrderAdapter;
import com.mfh.litecashier.ui.adapter.PurchaseFruitShopcartGoodsAdapter;
import com.mfh.litecashier.utils.FruitShopcartHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 生鲜订购购物车
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PurchaseFruitShopcartFragment extends BaseFragment {

    @Bind(R.id.tv_order_quantity)
    TextView tvOrderQuantity;
    @Bind(R.id.tv_goods_quantity)
    TextView tvGoodsQunatity;
    @Bind(R.id.tv_total_amount)
    TextView tvTotalAmount;
    @Bind(R.id.button_submit)
    TextView btnSubmit;

    @Bind(R.id.order_list)
    RecyclerView orderRecyclerView;
    private PurchaseFruitOrderAdapter orderAdapter;

    @Bind(R.id.goods_list)
    RecyclerView goodsRecyclerView;
    private PurchaseFruitShopcartGoodsAdapter goodsAdapter;
    private ItemTouchHelper itemTouchHelper;


    public static PurchaseFruitShopcartFragment newInstance(Bundle args) {
        PurchaseFruitShopcartFragment fragment = new PurchaseFruitShopcartFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_purchase_fresh_shopcart;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        initProgressDialog("正在发送请求", "下单成功", "下单失败");

        initOrderRecyclerView();
        initGoodsRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadData(true);
    }

    @OnClick(R.id.button_submit)
    public void sendOrder() {
        btnSubmit.setEnabled(false);
        showConfirmDialog("确定要提交订单吗？",
                "下单", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        doSubmitStuff();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        btnSubmit.setEnabled(true);
                    }
                });


    }

    /**
     * 提交采购订单
     */
    private void doSubmitStuff() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING);

        JSONObject jsonObject = new JSONObject();
        JSONArray items = new JSONArray();

        //status的值不要传了，后台自动判断（因为生鲜类要求是0，其他类是1）
//        jsonObject.put("status", OrderStatus.STATUS_CONFIRM);
        jsonObject.put("bizType", InvOrderApi.BIZTYPE_PURCHASE);
        jsonObject.put("receiveNetId", MfhLoginService.get().getCurOfficeId());
        jsonObject.put("contact", MfhLoginService.get().getHumanName());
        jsonObject.put("receiveMobile", MfhLoginService.get().getTelephone());
        //TODO,这里需要填写地址，暂时使用网点名称
        jsonObject.put("receiveAddr", MfhLoginService.get().getCurOfficeName());

        List<PurchaseShopcartSplitOrder> orderList = FruitShopcartHelper.getInstance().getSplitOrders();
        if (orderList != null && orderList.size() > 0) {
            for (PurchaseShopcartSplitOrder order : orderList) {

                List<PurchaseShopcartEntity> goodsList = order.getGoodsList();
                if (goodsList != null && goodsList.size() > 0) {
                    for (PurchaseShopcartEntity goods : goodsList) {

                        JSONObject item = new JSONObject();
                        item.put("chainSkuId", goods.getChainSkuId());
                        item.put("proSkuId", goods.getProSkuId());
                        item.put("providerId", goods.getProviderId());
                        item.put("isPrivate", goods.getIsPrivate());
                        item.put("productName", goods.getName());
                        item.put("askTotalCount", goods.getQuantity());
//                        item.put("totalCount", goods.getQuantityCheck());
                        item.put("price", goods.getPrice());
                        item.put("amount", goods.getQuantity() * goods.getPrice());
                        item.put("barcode", goods.getBarcode());
                        if (goods.getIsPrivate().equals(IsPrivate.PRIVATE)) {
                            item.put("controlType", "1");
                        } else if (goods.getIsPrivate().equals(IsPrivate.UNIFORM)) {
                            item.put("controlType", "0");
                        }

                        items.add(item);
                    }
                }
            }
        }
        jsonObject.put("items", items);

        InvSendOrderApiImpl.askSendOrder(jsonObject.toJSONString(), sendOrderRespCallback);
    }

    NetCallBack.NetTaskCallBack sendOrderRespCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("采购订单创建失败: " + errMsg);
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    btnSubmit.setEnabled(true);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            hideProgressDialog();
//                        }
//                    }, 1000);
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":{"val":"158"}}
                    /**
                     * 新建订单成功,清空购物车
                     * */
                    PurchaseShopcartService.getInstance().clearFreshGoodsList();
                    showProgressDialog(ProgressDialog.STATUS_DONE);
                    btnSubmit.setEnabled(true);

                    //刷新购物车
                    EventBus.getDefault().post(new PurchaseShopcartSyncEvent(PurchaseShopcartSyncEvent.EVENT_ID_ORDER_SUCCESS));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        }
                    }, 1000);
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };

    private void initOrderRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        orderRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        orderRecyclerView.setHasFixedSize(true);
//        添加分割线
        orderRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST, 8));
        orderAdapter = new PurchaseFruitOrderAdapter(CashierApp.getAppContext(), null);
        orderAdapter.setOnAdapterListener(new PurchaseFruitOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                loadGoodsList(orderAdapter.getEntity(position));
            }

            @Override
            public void onDataSetChanged() {
                //默认选中第一条订单
                loadGoodsList(orderAdapter.getCurOrder());
            }
        });
        orderRecyclerView.setAdapter(orderAdapter);
    }

    private void initGoodsRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
//        添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        goodsAdapter = new PurchaseFruitShopcartGoodsAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new PurchaseFruitShopcartGoodsAdapter.OnAdapterListener() {

            @Override
            public void onDataSetChanged(boolean isNeedReloadOrder) {
                if (isNeedReloadOrder) {
                    loadData(false);
                }
// else {
//                    //刷新订单列表
//                    orderAdapter.notifyDataSetChanged();
//                    refreshBottomBarInfo();
//                }
                //刷新购物车
                EventBus.getDefault().post(new PurchaseShopcartSyncEvent(PurchaseShopcartSyncEvent.EVENT_ID_DATASET_CHANGED));
            }
        });
        goodsRecyclerView.setAdapter(goodsAdapter);

        ItemTouchHelper.Callback callback = new MyItemTouchHelper(goodsAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(goodsRecyclerView);
    }

    /***/
    private void loadData(boolean isResetCurrent) {
        //TODO,拆分订单,加载商品
        FruitShopcartHelper.getInstance().reloadAndInit();
        orderAdapter.setEntityList(FruitShopcartHelper.getInstance().getSplitOrders(), isResetCurrent);
    }

    /**
     * 加载订单明细
     */
    private void loadGoodsList(PurchaseShopcartSplitOrder order) {
        //TODO,根据订单状态，显示隐藏支付按钮
        if (order == null) {
            goodsAdapter.setEntityList(null);
            return;
        }
        goodsAdapter.setEntityList(PurchaseShopcartService.getInstance()
                .getFreshGoodsList(order.getProviderId()));

        refreshBottomBarInfo();
    }

    /**
     * 刷新底部Bar信息
     */
    private void refreshBottomBarInfo() {
        tvOrderQuantity.setText(String.format("订单数: %d",
                FruitShopcartHelper.getInstance().getOrderTotalCount()));
        tvGoodsQunatity.setText(String.format("商品数: %.2f",
                FruitShopcartHelper.getInstance().getTotalGoodsCount()));
        tvTotalAmount.setText(Html.fromHtml(String
                .format("<font color=#000000>商品金额:</font><font color=#FF009B4E>%.2f</font>",
                        FruitShopcartHelper.getInstance().getTotalOrderAmount())));

        if (FruitShopcartHelper.getInstance().getTotalGoodsCount() > 0) {
            btnSubmit.setVisibility(View.VISIBLE);
        } else {
            btnSubmit.setVisibility(View.INVISIBLE);
        }

        //显示 总计－－金额
//                SerialDisplayHelper.show(2, productAdapter.getProductAmount());
    }

    /***
     * 加载用户网点信息
     */
    private void loadNetInfo() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
//            DialogUtil.showHint("网络未连接");
            return;
        }

        NetCallBack.NetTaskCallBack netTaskCallBack = new NetCallBack.NetTaskCallBack<SubdisCode,
                NetProcessor.Processor<SubdisCode>>(
                new NetProcessor.Processor<SubdisCode>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        if (rspData == null) {
//                            initTab(null, isNeedRefresh);
//                            return;
                        }

                        //保存网点信息
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                    }
                }
                , SubdisCode.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.getNetInfoById(String.valueOf(MfhLoginService.get().getCurOfficeId()), netTaskCallBack);
    }
}
