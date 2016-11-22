package com.mfh.petitestock.ui.fragment.receipt;

import android.app.Activity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.dialog.AccountQuickPayDialog;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.invOrder.InvOrderApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.petitestock.AppContext;
import com.mfh.petitestock.R;
import com.mfh.petitestock.database.entity.DistributionSignEntity;
import com.mfh.petitestock.ui.fragment.GpioFragment;

import java.util.List;

/**
 * Created by bingshanguxue on 5/11/16.
 */
public abstract class BaseReceiveOrderFragment extends GpioFragment {
    private AccountQuickPayDialog payDialog = null;
    protected Double totalAmount = 0D;

    /**
     * 支付成功
     */
    public void onReceiveOrderSucceed(String orderId) {
        ZLogger.d("新建收货单成功: " + orderId);
    }

    /**
     * 支付中断，取消&失败
     */
    public void onReceiveOrderInterrupted() {
    }

    /**
     * 签收订单
     */
    public void doSignWork(List<DistributionSignEntity> goodsList,
                           Long sendNetId, Long sendTenantId, Integer isPrivate) {
        if (goodsList == null || goodsList.size() < 1) {
            DialogUtil.showHint("商品不能为空");
            onReceiveOrderInterrupted();
            return;
        }

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            onReceiveOrderInterrupted();
            return;
        }

        final JSONObject jsonStrObject = new JSONObject();
        if (sendNetId != null) {
            jsonStrObject.put("sendNetId", sendNetId);
        }
        if (sendTenantId != null) {
            jsonStrObject.put("sendTenantId", sendTenantId);
        }
        jsonStrObject.put("isPrivate", isPrivate);
        jsonStrObject.put("receiveNetId", MfhLoginService.get().getCurOfficeId());
        jsonStrObject.put("tenantId", MfhLoginService.get().getSpid());
        jsonStrObject.put("remark", "");

        JSONArray itemsArray = new JSONArray();
        Double amount = 0D;
        for (DistributionSignEntity goods : goodsList) {
            JSONObject item = new JSONObject();
            item.put("chainSkuId", goods.getChainSkuId());//查询供应链
            item.put("proSkuId", goods.getProSkuId());
            item.put("productName", goods.getProductName());
            item.put("quantityCheck", goods.getQuantityCheck());
            item.put("price", goods.getPrice());
            item.put("amount", goods.getAmount());
            item.put("barcode", goods.getBarcode());
            item.put("providerId", goods.getProviderId());
            item.put("isPrivate", goods.getIsPrivate());//（0：不是 1：是）

            itemsArray.add(item);
        }
        jsonStrObject.put("items", itemsArray);

        InvOrderApiImpl.createInvSendIoRecOrder(null, true,
                jsonStrObject.toJSONString(), signResponseCallback);
    }

    private NetCallBack.NetTaskCallBack signResponseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
                    DialogUtil.showHint("新建收货单失败" + errMsg);
                    onReceiveOrderInterrupted();
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
