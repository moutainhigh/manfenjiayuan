package com.manfenjiayuan.business.mvp.presenter;

import com.mfh.framework.rxapi.bean.GoodsOrder;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;
import com.manfenjiayuan.business.mvp.mode.OrderflowMode;
import com.manfenjiayuan.business.mvp.view.IOrderflowView;

import java.util.List;

/**
 * 订单流水
 * Created by bingshanguxue on 16/3/17.
 */
public class OrderflowPresenter {
    private IOrderflowView iOrderflowView;
    private OrderflowMode orderflowMode;

    public OrderflowPresenter(IOrderflowView iOrderflowView) {
        this.iOrderflowView = iOrderflowView;
        this.orderflowMode = new OrderflowMode();
    }

    /**
     * 获取门店数据
     * */
    public void findGoodsOrderList(Integer btype, String subTypes, String orderStatus,
                                   String sellOffices, PageInfo pageInfo){
        orderflowMode.findGoodsOrderList(btype, subTypes, orderStatus, sellOffices, pageInfo,
                new OnPageModeListener<GoodsOrder>() {
                    @Override
                    public void onProcess() {
                        if (iOrderflowView != null) {
                            iOrderflowView.onProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<GoodsOrder> dataList) {
                        if (iOrderflowView != null) {
                            iOrderflowView.onSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (iOrderflowView != null) {
                            iOrderflowView.onError(errorMsg);
                        }
                    }
                });
    }

    /**
     * 获取门店数据
     * */
    public void findGoodsOrderList(Long humanId, PageInfo pageInfo){
        orderflowMode.findGoodsOrderList(humanId, pageInfo,
                new OnPageModeListener<GoodsOrder>() {
                    @Override
                    public void onProcess() {
                        if (iOrderflowView != null) {
                            iOrderflowView.onProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<GoodsOrder> dataList) {
                        if (iOrderflowView != null) {
                            iOrderflowView.onSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (iOrderflowView != null) {
                            iOrderflowView.onError(errorMsg);
                        }
                    }
                });
    }


}
