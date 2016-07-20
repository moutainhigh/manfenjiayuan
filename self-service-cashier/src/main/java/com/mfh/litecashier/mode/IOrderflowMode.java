package com.mfh.litecashier.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

/**
 * 订单流水:门店收银/线上销售/衣服洗护/快递代发
 * Created by bingshanguxue on 16/3/17.
 */
public interface IOrderflowMode<D> {

    /**
     * 获取门店数据
     * */
    void loadOrders(Integer btype, String orderStatus, Long sellOffices, PageInfo pageInfo,
                    OnPageModeListener<D> listener);
}
