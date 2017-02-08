package com.mfh.litecashier.ui.cashier;

import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.v1.CashierOrderInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * 收银台
 * Created by bingshanguxue on 05/02/2017.
 */

public class CashierDesktopObservable extends Observable{

    /**当前收银台收银商品,用列表的形式显示*/
    private List<CashierShopcartEntity> shopcartEntities = new ArrayList<>();
    /**商品总数量*/
    private Double bcount = 0D;
    /**商品总金额*/
    private Double amount = 0D;
    /**收银信息*/
    private CashierOrderInfo cashierOrderInfo = null;

//    /**优惠券金额*/
//    private Double couponDiscountAmount = 0D;
//    /**积分*/
//    private Double amount = 0D;
//    /**实付金额*/
//    private Double amount = 0D;
//    /**会员*/
//    private Human vipMember;

    private static CashierDesktopObservable instance = null;

    /**
     * 返回 DataDownloadManager 实例
     *
     * @return
     */
    public static CashierDesktopObservable getInstance() {
        if (instance == null) {
            synchronized (CashierDesktopObservable.class) {
                if (instance == null) {
                    instance = new CashierDesktopObservable();
                }
            }
        }
        return instance;
    }

    public List<CashierShopcartEntity> getShopcartEntities() {
        return shopcartEntities;
    }

    public void setShopcartEntities(List<CashierShopcartEntity> shopcartEntities) {
        this.shopcartEntities = shopcartEntities;

        onDatasetChanged();
    }

    public Double getBcount() {
        return bcount;
    }

    public Double getAmount() {
        return amount;
    }

    public CashierOrderInfo getCashierOrderInfo() {
        return cashierOrderInfo;
    }

    public void setCashierOrderInfo(CashierOrderInfo cashierOrderInfo) {
        this.cashierOrderInfo = cashierOrderInfo;
        notifyDatasetChanged();
    }

    /**清空数据*/
    public void clear(){
        this.shopcartEntities = new ArrayList<>();
        this.cashierOrderInfo = null;
        onDatasetChanged();
    }

    /**数据发生改变*/
    public void onDatasetChanged(){
        Double bcountTemp = 0D, amountTemp = 0D;
        if (shopcartEntities != null && shopcartEntities.size() > 0) {
            for (CashierShopcartEntity entity : shopcartEntities) {
                bcountTemp += entity.getBcount();
                amountTemp += entity.getFinalAmount();
            }
        }
        this.bcount = bcountTemp;
        this.amount = amountTemp;

        notifyDatasetChanged();
    }

    private void notifyDatasetChanged(){
        setChanged();    //标记此 Observable对象为已改变的对象
        notifyObservers();    //通知所有观察者
    }

}
