package com.mfh.litecashier.ui.fragment.purchase.manual;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.companyInfo.CompanyInfoApiImpl;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.invOrder.InvOrderApi;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.rxapi.http.InvSendOrderHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.SubdisCode;
import com.mfh.litecashier.database.entity.PurchaseGoodsEntity;
import com.mfh.litecashier.database.entity.PurchaseOrderEntity;
import com.mfh.litecashier.database.logic.PurchaseGoodsService;
import com.mfh.litecashier.database.logic.PurchaseOrderService;
import com.mfh.litecashier.event.PurchaseShopcartSyncEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 手动订货购物车
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ManualPurchaseShopcartFragment extends BaseFragment {
    @BindView(R.id.tv_order_quantity)
    TextView tvOrderQuantity;
    @BindView(R.id.tv_goods_quantity)
    TextView tvGoodsQunatity;
    @BindView(R.id.tv_total_amount)
    TextView tvTotalAmount;
    @BindView(R.id.button_submit)
    TextView btnSubmit;

    @BindView(R.id.order_list)
    RecyclerView orderRecyclerView;
    private ManualPurchaseShopcartOrderAdapter orderAdapter;

    @BindView(R.id.goods_list)
    RecyclerView goodsRecyclerView;
    private ManualPurchaseShopcartGoodsAdapter goodsAdapter;
    private ItemTouchHelper itemTouchHelper;


    public static ManualPurchaseShopcartFragment newInstance(Bundle args) {
        ManualPurchaseShopcartFragment fragment = new ManualPurchaseShopcartFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_apply_shopcart;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initProgressDialog("正在发送请求", "下单成功", "下单失败");

        initOrderRecyclerView();
        initGoodsRecyclerView();

        loadData();
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
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
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

        List<PurchaseOrderEntity> orderList = orderAdapter.getEntityList();
        if (orderList != null && orderList.size() > 0) {
            for (PurchaseOrderEntity order : orderList) {
                List<PurchaseGoodsEntity> goodsList = PurchaseGoodsService.getInstance()
                        .fetchGoodsEntities(order.getPurchaseType(), order.getProviderId());
                if (goodsList != null && goodsList.size() > 0) {
                    for (PurchaseGoodsEntity goods : goodsList) {
                        JSONObject item = new JSONObject();
                        item.put("chainSkuId", goods.getChainSkuId());
                        item.put("proSkuId", goods.getProSkuId());
                        item.put("providerId", goods.getProviderId());
                        item.put("isPrivate", goods.getIsPrivate());
                        item.put("productName", goods.getProductName());
                        item.put("askTotalCount", goods.getQuantityCheck());
//                        item.put("totalCount", goods.getQuantityCheck());
                        item.put("price", goods.getBuyPrice());
                        item.put("amount", goods.getQuantityCheck() * goods.getBuyPrice());
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

        Map<String, String> options = new HashMap<>();
        options.put("jsonStr", jsonObject.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSendOrderHttpManager.getInstance().askSendOrder(options,
                new MValueSubscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.e("采购订单创建失败: " + e.toString());
                        showProgressDialog(ProgressDialog.STATUS_ERROR, e.getMessage(), true);
                        btnSubmit.setEnabled(true);

                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        ZLogger.d("新建采购订单成功,清空购物车..." + data);

                        PurchaseHelper.getInstance().clear(PurchaseOrderEntity.PURCHASE_TYPE_MANUAL);
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

                });
    }

    /**
     * 初始化订单列表
     */
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
        orderAdapter = new ManualPurchaseShopcartOrderAdapter(CashierApp.getAppContext(), null);
        orderAdapter.setOnAdapterListener(new ManualPurchaseShopcartOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                loadGoodsList(orderAdapter.getCurOrder());
            }

            @Override
            public void onDataSetChanged() {
                //默认选中第一条订单
                loadGoodsList(orderAdapter.getCurOrder());
                refreshBottomBarInfo();
            }
        });
        orderRecyclerView.setAdapter(orderAdapter);
    }

    /**
     * 初始化订单明细列表
     */
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
        goodsAdapter = new ManualPurchaseShopcartGoodsAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new ManualPurchaseShopcartGoodsAdapter.OnAdapterListener() {

            @Override
            public void onDataSetChanged(boolean isNeedReloadOrder) {
                if (isNeedReloadOrder) {
                    loadData();
                } else {
                    //刷新订单列表
                    orderAdapter.notifyDataSetChanged();
                }
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
    private void loadData() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        //TODO,拆分订单,加载商品
        List<PurchaseOrderEntity> orderEntities = PurchaseOrderService.getInstance()
                .fetchOrders(PurchaseOrderEntity.PURCHASE_TYPE_MANUAL);
        if (orderAdapter != null){
            orderAdapter.setEntityList(orderEntities);
        }
        hideProgressDialog();
    }

    /**
     * 加载订单明细
     */
    private void loadGoodsList(PurchaseOrderEntity orderWrapper) {
        //TODO,根据订单状态，显示隐藏支付按钮
        if (orderWrapper == null) {
            goodsAdapter.setEntityList(null);
            return;
        }
        List<PurchaseGoodsEntity> goodsEntities = PurchaseGoodsService.getInstance()
                .fetchGoodsEntities(orderWrapper.getPurchaseType(),
                orderWrapper.getProviderId());
        goodsAdapter.setEntityList(goodsEntities);
    }

    /**
     * 刷新底部Bar信息
     */
    private void refreshBottomBarInfo() {
        try{
            int orderCount = orderAdapter.getItemCount();
            int itemCount = 0;
            Double amount = 0D;
            List<PurchaseOrderEntity> entities = orderAdapter.getEntityList();
            if (entities != null && entities.size() > 0){
                for (PurchaseOrderEntity entity : entities){
                    amount += entity.getAmount();

                    List<PurchaseGoodsEntity> goodsEntities = PurchaseGoodsService.getInstance()
                            .fetchGoodsEntities(entity.getPurchaseType(), entity.getProviderId());
                    if (goodsEntities != null && goodsEntities.size() > 0){
                        itemCount += goodsEntities.size();
                    }
                }
            }
            tvOrderQuantity.setText(String.format("订单数: %d", orderCount));
            tvGoodsQunatity.setText(String.format("商品数: %d", itemCount));
            tvTotalAmount.setText(StringUtils.toSpanned(String.format("<font color=#000000>商品金额:</font>" +
                    "<font color=#FF009B4E>%.2f</font>",amount)));

            if (orderCount> 0) {
                btnSubmit.setVisibility(View.VISIBLE);
            } else {
                btnSubmit.setVisibility(View.INVISIBLE);
            }
        }
        catch (Exception e){
            ZLogger.ef(e.toString());
        }

    }

    /***
     * 加载用户网点信息
     */
    private void loadNetInfo() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
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

        CompanyInfoApiImpl.getNetInfoById(String.valueOf(MfhLoginService.get().getCurOfficeId()),
                netTaskCallBack);
    }
}
