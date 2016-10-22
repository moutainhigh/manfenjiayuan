package com.mfh.framework.api.shoppingCart;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车
 * Created by bingshanguxue on 16/3/17.
 */
public class ShoppingCartMode {
    /**
     * 加载购物车
     */
    public void list(Long shopId, Long ownerId, PageInfo pageInfo, final OnPageModeListener<ShoppingCart> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (shopId == null) {
            if (listener != null) {
                listener.onError("店铺编号无效");
            }
            return;
        }


        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ShoppingCart>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ShoppingCart> rs) {
                        //此处在主线程中执行。
                        List<ShoppingCart> scGoodsSkus = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<ShoppingCart> wrapper : rs.getRowDatas()) {
                                scGoodsSkus.add(wrapper.getBean());
                            }
                        }
                        if (listener != null) {
                            listener.onSuccess(pageInfo, scGoodsSkus);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载类目商品失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, ShoppingCart.class, MfhApplication.getAppContext());

        ShoppingCartApiImpl.list(shopId, ownerId, pageInfo, queryRsCallBack);
    }


}
