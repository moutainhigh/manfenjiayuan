package com.manfenjiayuan.cashierdisplay;


import com.manfenjiayuan.cashierdisplay.bean.CashierOrderInfo;
import com.manfenjiayuan.cashierdisplay.bean.CashierOrderInfoWrapper;

/**
 * 事务
 * Created by bingshanguxue on 15/9/23.
 */
public class CashierOrderEvent {

    private CashierOrderInfoWrapper cashierOrderInfoWrapper;

    public CashierOrderEvent(CashierOrderInfoWrapper cashierOrderInfoWrapper) {
        this.cashierOrderInfoWrapper = cashierOrderInfoWrapper;
    }

    public CashierOrderInfoWrapper getCashierOrderInfoWrapper() {
        return cashierOrderInfoWrapper;
    }
}
