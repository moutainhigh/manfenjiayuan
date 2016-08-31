package com.bingshanguxue.cashier.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.anon.PubSkus;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/21.
 */
public interface IScProductPriceView extends MvpView {
    void onIScProductPriceViewProcess();
    void onIScProductPriceViewError(String errorMsg);
    void onIScProductPriceViewSuccess(PageInfo pageInfo, List<PubSkus> dataList);
    void onIScProductPriceViewSuccess(PubSkus data);
}
