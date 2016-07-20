package com.manfenjiayuan.cashierdisplay.bean;

/**
 * 收银订单
 * Created by Administrator on 2015/5/14.
 */
public class CashierOrderInfoWrapper implements java.io.Serializable {

    public final static int CMD_CLEAR_ORDER     = 0;
    public final static int CMD_PAY_ORDER       = 1;
    public final static int CMD_FINISH_ORDER    = 2;

    private int cmdType;
    private CashierOrderInfo cashierOrderInfo;

    public int getCmdType() {
        return cmdType;
    }

    public void setCmdType(int cmdType) {
        this.cmdType = cmdType;
    }

    public CashierOrderInfo getCashierOrderInfo() {
        return cashierOrderInfo;
    }

    public void setCashierOrderInfo(CashierOrderInfo cashierOrderInfo) {
        this.cashierOrderInfo = cashierOrderInfo;
    }
}
