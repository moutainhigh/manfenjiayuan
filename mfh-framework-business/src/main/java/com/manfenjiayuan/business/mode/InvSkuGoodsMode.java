package com.manfenjiayuan.business.mode;

import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.impl.StockApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

/**
 * {@link com.manfenjiayuan.business.bean.InvSkuGoods}
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSkuGoodsMode {

    public void getByBarcodeMust(String barcode, final OnModeListener<InvSkuGoods> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        //检查参数：
        if (StringUtils.isEmpty(barcode)) {
            if (listener != null) {
                listener.onError("条码不能为空");
            }
            return;
        }

        NetCallBack.NetTaskCallBack queryRespCallback = new NetCallBack.NetTaskCallBack<InvSkuGoods,
                NetProcessor.Processor<InvSkuGoods>>(
                new NetProcessor.Processor<InvSkuGoods>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                        InvSkuGoods chainGoodsSku = null;
                        if (rspData != null) {
                            RspBean<InvSkuGoods> retValue = (RspBean<InvSkuGoods>) rspData;
                            chainGoodsSku = retValue.getValue();
                        }
                        if (listener != null) {
                            listener.onSuccess(chainGoodsSku);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("查询商品失败：" + errMsg);

                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , InvSkuGoods.class
                , MfhApplication.getAppContext()) {
        };

        StockApiImpl.getByBarcodeMust(barcode, queryRespCallback);
    }


}
