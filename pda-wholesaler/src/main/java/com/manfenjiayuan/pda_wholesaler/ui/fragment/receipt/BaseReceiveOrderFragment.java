package com.manfenjiayuan.pda_wholesaler.ui.fragment.receipt;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.PDAScanFragment;
import com.manfenjiayuan.pda_wholesaler.AppContext;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.database.entity.DistributionSignEntity;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;

import java.util.List;

/**
 * Created by bingshanguxue on 5/11/16.
 */
public abstract class BaseReceiveOrderFragment extends PDAScanFragment {
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
     * @param sendTenantId {"code":"1","msg":"收货时发送方租户不能为空!","data":null,"version":1}
     */
    public void doSignWork(List<DistributionSignEntity> goodsList, Long otherOrderId,
                           Long sendTenantId, Integer isPrivate) {
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
            //价格不能为空
            if (goods.getPrice() == null){
                continue;
            }

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
            amount += goods.getAmount();
        }
        jsonStrObject.put("items", itemsArray);
        totalAmount = amount;

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

}
