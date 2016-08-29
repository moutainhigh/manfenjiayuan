package com.manfenjiayuan.pda_supermarket.ui.fragment.receipt;

import android.app.Activity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.dialog.AccountQuickPayDialog;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.DataSyncManager;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.entity.DistributionSignEntity;
import com.manfenjiayuan.pda_supermarket.scanner.PDAScanFragment;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.api.impl.InvOrderApiImpl;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.List;

/**
 * Created by bingshanguxue on 5/11/16.
 */
public abstract class BaseReceiveOrderFragment extends PDAScanFragment {
    private AccountQuickPayDialog payDialog = null;
    protected Double totalAmount = 0D;

    public void onReceiveOrderProcess(){
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在处理订单，请稍后...", false);
    }

    /**
     * 支付成功
     */
    public void onReceiveOrderSucceed(String orderId) {
        hideProgressDialog();
        ZLogger.d("新建收货单成功: " + orderId);

        DataSyncManager.getInstance().notifyUpdateSku();

//        支付收货订单
        doPayWork(orderId, totalAmount);
    }

    /**
     * 支付中断，取消&失败
     */
    public void onReceiveOrderInterrupted(String message) {
        DialogUtil.showHint(message);
        hideProgressDialog();
    }

    /**
     * 签收订单
     * @param sendTenantId {"code":"1","msg":"收货时发送方租户不能为空!","data":null,"version":1}
     */
    public void doSignWork(List<DistributionSignEntity> goodsList, Long otherOrderId,
                           Long sendTenantId, Integer isPrivate) {
        onReceiveOrderProcess();

        if (goodsList == null || goodsList.size() < 1) {
            onReceiveOrderInterrupted("商品不能为空");
            return;
        }

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            onReceiveOrderInterrupted(getString(R.string.toast_network_error));
            return;
        }

        final JSONObject jsonStrObject = new JSONObject();
        if (sendTenantId != null) {
            jsonStrObject.put("sendTenantId", sendTenantId);
        }
        jsonStrObject.put("sendStoreType", StoreType.WHOLESALER);
        jsonStrObject.put("isPrivate", isPrivate);
        jsonStrObject.put("receiveNetId", MfhLoginService.get().getCurOfficeId());
        jsonStrObject.put("tenantId", MfhLoginService.get().getSpid());
        jsonStrObject.put("remark", "");

        JSONArray itemsArray = new JSONArray();
        Double amount = 0D;
        for (DistributionSignEntity goods : goodsList) {
            if (goods.getReceivePrice() == null){
                ZLogger.d("未设置价格不允许收货");
                continue;
            }
            JSONObject item = new JSONObject();
            item.put("chainSkuId", goods.getChainSkuId());//查询供应链
            item.put("proSkuId", goods.getProSkuId());
            String productName = goods.getProductName();
            // TODO: 6/10/16  商品名字太长，后台不允许提交,这里增加一层过滤
            if (!StringUtils.isEmpty(productName) && productName.length() > 10){
                item.put("productName", productName.substring(0, 10));
            }
            else{
                item.put("productName", productName);
            }
            item.put("quantityCheck", goods.getReceiveQuantity());
            item.put("price", goods.getReceivePrice());
            item.put("amount", goods.getReceiveAmount());
            item.put("barcode", goods.getBarcode());
            item.put("providerId", goods.getProviderId());
            item.put("isPrivate", goods.getIsPrivate());//（0：不是 1：是）

            itemsArray.add(item);
            amount += goods.getReceiveAmount();
        }
        jsonStrObject.put("items", itemsArray);
        totalAmount = amount;

//        ZLogger.d("jsonStr:\n " + JSON.toJSONString(jsonStrObject));
        InvSendIoOrderApiImpl.createInvSendIoRecOrder(otherOrderId, true,
                jsonStrObject.toJSONString(), signResponseCallback);
    }

    public void doSignWork(JSONArray itemsArray, Double amount, Long otherOrderId,
                           Long sendTenantId, Integer isPrivate) {
        onReceiveOrderProcess();

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            onReceiveOrderInterrupted(getString(R.string.toast_network_error));
            return;
        }
        final JSONObject jsonStrObject = new JSONObject();
        if (sendTenantId != null) {
            jsonStrObject.put("sendTenantId", sendTenantId);
        }
        jsonStrObject.put("sendStoreType", StoreType.WHOLESALER);
        jsonStrObject.put("isPrivate", isPrivate);
        jsonStrObject.put("receiveNetId", MfhLoginService.get().getCurOfficeId());
        jsonStrObject.put("tenantId", MfhLoginService.get().getSpid());
        jsonStrObject.put("remark", "");
        jsonStrObject.put("items", itemsArray);
        totalAmount = amount;

//        ZLogger.d("jsonStr:\n " + JSON.toJSONString(jsonStrObject));
        InvSendIoOrderApiImpl.createInvSendIoRecOrder(otherOrderId, true,
                jsonStrObject.toJSONString(), signResponseCallback);
    }



    private NetCallBack.NetTaskCallBack signResponseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //parser:{"code":"1","msg":"收货时发送方租户不能为空!","data":null,"version":1}
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
                    onReceiveOrderInterrupted("新建收货单失败" + errMsg);
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}
                    /**
                     * 新增采购单成功，更新采购单列表
                     * */
                    RspValue<String> retValue = (RspValue<String>) rspData;
                    onReceiveOrderSucceed(retValue.getValue());
                }
            }
            , String.class
            , AppContext.getAppContext()) {
    };


    /**
     * 支付成功
     */
    public void onOrderPaySucceed() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();

    }

    /**
     * 支付中断，取消&失败
     */
    public void onOrderPayInterrupted() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    /**
     * 支付订单
     */
    public void doPayWork(String orderId, Double amount) {
        if (amount <= 0){
            onOrderPaySucceed();
            return;
        }

        if (StringUtils.isEmpty(orderId)) {
            ZLogger.d("订单无效");
            onOrderPayInterrupted();
            return;
        }

        //支付
        if (payDialog == null) {
            payDialog = new AccountQuickPayDialog(getActivity());
            payDialog.setCancelable(false);
            payDialog.setCanceledOnTouchOutside(false);
        }
        payDialog.init(orderId, amount, new AccountQuickPayDialog.DialogClickListener() {
            @Override
            public void onPaySucceed() {
                //支付成功
                onOrderPaySucceed();
            }

            @Override
            public void onPayFailed() {

            }

            @Override
            public void onPayCanceled() {
                onOrderPayInterrupted();
            }
        });
        payDialog.show();
    }

}
