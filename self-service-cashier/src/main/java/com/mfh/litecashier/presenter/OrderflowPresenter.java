package com.mfh.litecashier.presenter;

import com.mfh.framework.api.pmcstock.PosOrder;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.litecashier.mode.OrderflowMode;
import com.mfh.litecashier.ui.view.IOrderflowView;

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
     * 查询订单
     * @param btype 类目编号
     * */
    /**
     * 获取门店数据
     * */
    public void findGoodsOrderList(Integer btype, String subTypes, String orderStatus,
                                   String sellOffices, PageInfo pageInfo){
        orderflowMode.findGoodsOrderList(btype, subTypes, orderStatus, sellOffices, pageInfo,
                new OnPageModeListener<PosOrder>() {
                    @Override
                    public void onProcess() {
                        if (iOrderflowView != null) {
                            iOrderflowView.onProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<PosOrder> dataList) {
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
