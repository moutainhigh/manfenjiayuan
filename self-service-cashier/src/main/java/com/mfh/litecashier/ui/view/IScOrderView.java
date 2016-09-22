package com.mfh.litecashier.ui.view;

import com.mfh.framework.api.scOrder.ScOrder;

/**
 * Created by bingshanguxue on 9/22/16.
 */

public interface IScOrderView {
    void onIScOrderViewProcess();
    void onIScOrderViewError(String errorMsg);
    void onIScOrderViewNext(ScOrder data);
}
