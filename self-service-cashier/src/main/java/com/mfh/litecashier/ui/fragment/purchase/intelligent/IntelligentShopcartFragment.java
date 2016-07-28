package com.mfh.litecashier.ui.fragment.purchase.intelligent;


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
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.manfenjiayuan.business.bean.InvSendOrderItemBrief;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.impl.InvSendOrderApiImpl;
import com.mfh.framework.api.impl.StockApiImpl;
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
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartGoodsWrapper;
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartOrder;
import com.mfh.litecashier.event.PurchaseShopcartSyncEvent;
import com.mfh.litecashier.ui.adapter.PurchaseShopcartOrderAdapter;
import com.mfh.litecashier.ui.dialog.SelectInvCompanyInfoDialog;
import com.mfh.litecashier.utils.IntelligentShopcartHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 智能订货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class IntelligentShopcartFragment extends BaseFragment {

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
    private PurchaseShopcartOrderAdapter orderAdapter;

    @Bind(R.id.goods_list)
    RecyclerView goodsRecyclerView;
    private PurchaseIntelligentShopcartGoodsAdapter goodsAdapter;
    private ItemTouchHelper itemTouchHelper;


    private SelectInvCompanyInfoDialog selectPlatformProviderDialog = null;
    private CompanyInfo mCompanyInfo = null;

    public static IntelligentShopcartFragment newInstance(Bundle args) {
        IntelligentShopcartFragment fragment = new IntelligentShopcartFragment();

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

        //购物车不为空
        if (IntelligentShopcartHelper.getInstance().getItemCount() > 0) {
            refresh();
            showConfirmDialog("购物车不为空，是否继续订货？",
                    "重新开始", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            IntelligentShopcartHelper.getInstance().clear();
                            selectPlatformProvider();
                        }
                    }, "继续订货", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        } else {
            IntelligentShopcartHelper.getInstance().clear();
            selectPlatformProvider();
        }
    }

    public void selectPlatformProvider() {
        //TODO,判断商品是否存在多个供应链，若存在多个，则提示选择供应链
        if (selectPlatformProviderDialog == null) {
            selectPlatformProviderDialog = new SelectInvCompanyInfoDialog(getActivity());
            selectPlatformProviderDialog.setCancelable(false);
            selectPlatformProviderDialog.setCanceledOnTouchOutside(false);
        }
        selectPlatformProviderDialog.init(new SelectInvCompanyInfoDialog.OnDialogListener() {
            @Override
            public void onItemSelected(CompanyInfo companyInfo) {
                changeCompany(companyInfo);
            }

            @Override
            public void onCancel() {
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }

        });
        if (!selectPlatformProviderDialog.isShowing()) {
            selectPlatformProviderDialog.show();
        }
    }

    private void changeCompany(CompanyInfo companyInfo) {
        mCompanyInfo = companyInfo;
        IntelligentShopcartHelper.getInstance().clear();
        if (mCompanyInfo != null) {
            loadIntelligentOrder();
        }
    }

    /**
     * 智能订货
     */
    public void loadIntelligentOrder() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在为您智能订货...", false);
        StockApiImpl.autoAskSendOrder(mCompanyInfo.getId(), intelligentRespCallback);
    }

    NetCallBack.NetTaskCallBack intelligentRespCallback = new NetCallBack.NetTaskCallBack<InvSendOrderItemBrief,
            NetProcessor.Processor<InvSendOrderItemBrief>>(
            new NetProcessor.Processor<InvSendOrderItemBrief>() {
                @Override
                public void processResult(IResponseData rspData) {

                    hideProgressDialog();
                    List<InvSendOrderItem> orderItems = new ArrayList<>();

                    if (rspData != null) {
                        //com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                        RspBean<InvSendOrderItemBrief> retValue = (RspBean<InvSendOrderItemBrief>) rspData;
                        InvSendOrderItemBrief orderDetail = retValue.getValue();
                        orderItems = orderDetail.getItems();
                    }

                    if (orderItems != null && orderItems.size() > 0) {
                        for (InvSendOrderItem invSendOrderItem : orderItems) {
                            PurchaseShopcartGoodsWrapper shopcartGoodsWrapper = PurchaseShopcartGoodsWrapper
                                    .fromIntelligentOrderItem(invSendOrderItem, mCompanyInfo,
                                            IsPrivate.PLATFORM);
                            IntelligentShopcartHelper.getInstance().addToShopcart(shopcartGoodsWrapper);
                        }
                    }

                    refresh();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("智能订货失败：" + errMsg);

                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                }
            }
            , InvSendOrderItemBrief.class
            , CashierApp.getAppContext()) {
    };


    @OnClick(R.id.button_submit)
    public void sendOrder() {
        btnSubmit.setEnabled(false);

        doSubmitStuff();
//        showConfirmDialog("确定要提交订单吗？",
//                "下单", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//
//                        doSubmitStuff();
//                    }
//                }, "点错了", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        btnSubmit.setEnabled(true);
//                    }
//                });

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

        boolean isHasEmptyQuantity = false;
        final JSONObject jsonObject = new JSONObject();
        JSONArray items = new JSONArray();

        //status的值不要传了，后台自动判断（因为生鲜类要求是0，其他类是1）
//        jsonObject.put("status", OrderStatus.STATUS_CONFIRM);
        jsonObject.put("bizType", InvOrderApi.BIZTYPE_PURCHASE);
        jsonObject.put("receiveNetId", MfhLoginService.get().getCurOfficeId());
        jsonObject.put("contact", MfhLoginService.get().getHumanName());
        jsonObject.put("receiveMobile", MfhLoginService.get().getTelephone());
        //TODO,这里需要填写地址，暂时使用网点名称
        jsonObject.put("receiveAddr", MfhLoginService.get().getCurOfficeName());

        List<PurchaseShopcartOrder> orderList = IntelligentShopcartHelper.getInstance().getOrderList();
        if (orderList != null && orderList.size() > 0) {
            for (PurchaseShopcartOrder order : orderList) {

                List<PurchaseShopcartGoodsWrapper> goodsList = order.getGoodsList();
                if (goodsList != null && goodsList.size() > 0) {
                    for (PurchaseShopcartGoodsWrapper goods : goodsList) {

                        Double quantityCheck = goods.getQuantityCheck();
                        if (quantityCheck == null || quantityCheck.compareTo(0D) <= 0) {
                            isHasEmptyQuantity = true;
                        }
                        JSONObject item = new JSONObject();
                        item.put("chainSkuId", goods.getChainSkuId());
                        item.put("proSkuId", goods.getProSkuId());
                        item.put("providerId", goods.getSupplyId());
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

//        if (isHasEmptyQuantity){
//
//        }
        String title = isHasEmptyQuantity ? "检测到有部分商品数量为空，您确定要提交订单吗？" : "您确定要提交订单吗？";
        showConfirmDialog(title,
                "下单", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        InvSendOrderApiImpl.askSendOrder(jsonObject.toJSONString(), sendOrderRespCallback);
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        hideProgressDialog();
                        btnSubmit.setEnabled(true);
                    }
                });


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
                     10           * 新建订单成功,清空购物车
                     * */
                    IntelligentShopcartHelper.getInstance().clear();
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
        orderAdapter = new PurchaseShopcartOrderAdapter(CashierApp.getAppContext(), null);
        orderAdapter.setOnAdapterListener(new PurchaseShopcartOrderAdapter.OnAdapterListener() {
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

    /***/
    private void refresh() {
        //TODO,拆分订单,加载商品
        orderAdapter.setEntityList(IntelligentShopcartHelper.getInstance().getOrderList());
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
        goodsAdapter = new PurchaseIntelligentShopcartGoodsAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new PurchaseIntelligentShopcartGoodsAdapter.OnAdapterListener() {

            @Override
            public void onDataSetChanged(boolean isNeedReloadOrder) {
                if (isNeedReloadOrder) {
                    orderAdapter.setEntityList(IntelligentShopcartHelper.getInstance().getOrderList());
                } else {
                    //刷新订单列表
                    orderAdapter.notifyDataSetChanged();
                    refreshBottomBarInfo();
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

    /**
     * 加载订单明细
     */
    private void loadGoodsList(PurchaseShopcartOrder order) {
        //TODO,根据订单状态，显示隐藏支付按钮
        if (order == null) {
            goodsAdapter.setEntityList(null);
            return;
        }
        goodsAdapter.setEntityList(order.getGoodsList());
    }

    /**
     * 刷新底部Bar信息
     */
    private void refreshBottomBarInfo() {
        tvOrderQuantity.setText(String.format("订单数: %d",
                IntelligentShopcartHelper.getInstance().getOrderCount()));
        tvGoodsQunatity.setText(String.format("商品数: %.2f",
                IntelligentShopcartHelper.getInstance().getTotalCount()));
        tvTotalAmount.setText(Html.fromHtml(String.format("<font color=#000000>商品金额:</font><font color=#FF009B4E>%.2f</font>",
                IntelligentShopcartHelper.getInstance().getAmount())));

        if (IntelligentShopcartHelper.getInstance().getOrderCount() > 0) {
            btnSubmit.setVisibility(View.VISIBLE);
        } else {
            btnSubmit.setVisibility(View.INVISIBLE);
        }

        //显示 总计－－金额
//                SerialDisplayHelper.show(2, productAdapter.getProductAmount());
    }

}
