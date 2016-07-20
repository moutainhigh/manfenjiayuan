package com.mfh.litecashier.bean.wrapper;


import com.mfh.framework.api.constant.BizType;
import com.mfh.litecashier.bean.AggItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 交接班/日结经营分析数据
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 */
public class AggWrapper implements java.io.Serializable {
    private Double grossProfit = 0D;//毛利
    private Double turnOver = 0D;//营业额合计

    /**
     * {@link BizType#POS}
     */
    private List<AggItem> posItems;
    /**
     * {@link BizType#SC}
     */
    private List<AggItem> scItems;
    /**
     * {@link BizType#LAUNDRY}
     */
    private List<AggItem> laundryItems;
    /**
     * {@link BizType#PIJU}
     */
    private List<AggItem> pijuItems;
    /**
     * {@link BizType#STOCK}
     */
    private List<AggItem> stockItems;
    /**
     * {@link BizType#SEND}
     */
    private List<AggItem> sendItems;
    /**
     * {@link BizType#RECHARGE}
     */
    private List<AggItem> rechargeItems;

    public Double getGrossProfit() {
        if (grossProfit == null) {
            return 0D;
        }
        return grossProfit;
    }

    public void setGrossProfit(Double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public Double getTurnOver() {
        if (turnOver == null) {
            return 0D;
        }
        return turnOver;
    }

    public void setTurnOver(Double turnOver) {
        this.turnOver = turnOver;
    }

    public List<AggItem> getPosItems() {
        return posItems;
    }

    public void setPosItems(List<AggItem> posItems) {
        this.posItems = posItems;
    }

    public List<AggItem> getScItems() {
        return scItems;
    }

    public void setScItems(List<AggItem> scItems) {
        this.scItems = scItems;
    }

    public List<AggItem> getLaundryItems() {
        return laundryItems;
    }

    public void setLaundryItems(List<AggItem> laundryItems) {
        this.laundryItems = laundryItems;
    }

    public List<AggItem> getPijuItems() {
        return pijuItems;
    }

    public void setPijuItems(List<AggItem> pijuItems) {
        this.pijuItems = pijuItems;
    }


    public List<AggItem> getStockItems() {
        return stockItems;
    }

    public void setStockItems(List<AggItem> stockItems) {
        this.stockItems = stockItems;
    }

    public List<AggItem> getSendItems() {
        return sendItems;
    }

    public void setSendItems(List<AggItem> sendItems) {
        this.sendItems = sendItems;
    }

    public List<AggItem> getRechargeItems() {
        return rechargeItems;
    }

    public void setRechargeItems(List<AggItem> rechargeItems) {
        this.rechargeItems = rechargeItems;
    }

    public AggWrapper() {
        this(null);
    }

    public AggWrapper(List<AggItem> aggItems) {
        this.turnOver = 0D;
        this.grossProfit = 0D;
        this.posItems = new ArrayList<>();
        this.scItems = new ArrayList<>();
        this.laundryItems = new ArrayList<>();
        this.pijuItems = new ArrayList<>();
        this.stockItems = new ArrayList<>();
        this.sendItems = new ArrayList<>();
        this.rechargeItems = new ArrayList<>();

        if (aggItems != null && aggItems.size() > 0) {
            for (AggItem entity : aggItems) {
                //线下商超
                if (entity.getBizType().equals(BizType.POS)) {
                    this.posItems.add(entity);
                    this.turnOver += entity.getTurnover();
                    this.grossProfit += entity.getGrossProfit();
                }
                //线上商城
                else if (entity.getBizType().equals(BizType.SC)) {
                    this.scItems.add(entity);
                    this.turnOver += entity.getTurnover();
                    this.grossProfit += entity.getGrossProfit();
                }
                //衣服洗护
                else if (entity.getBizType().equals(BizType.LAUNDRY)) {
                    this.laundryItems.add(entity);
                    this.turnOver += entity.getTurnover();
                    this.grossProfit += entity.getGrossProfit();
                }
                //皮具护理
                else if (entity.getBizType().equals(BizType.PIJU)) {
                    this.pijuItems.add(entity);
                    this.turnOver += entity.getTurnover();
                    this.grossProfit += entity.getGrossProfit();
                }
                //快递代收
                else if (entity.getBizType().equals(BizType.STOCK)) {
                    this.stockItems.add(entity);
                    this.turnOver += entity.getTurnover();
                    this.grossProfit += entity.getGrossProfit();
                }
                //快递代揽
                else if (entity.getBizType().equals(BizType.SEND)) {
                    this.sendItems.add(entity);
                    this.turnOver += entity.getTurnover();
                    this.grossProfit += entity.getGrossProfit();
                }
                //代充值业务
                else if (entity.getBizType().equals(BizType.RECHARGE)) {
                    this.rechargeItems.add(entity);
                    this.turnOver += entity.getTurnover();
                    this.grossProfit += entity.getGrossProfit();
                }
            }
        }
    }

}
