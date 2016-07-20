package com.mfh.litecashier.bean.wrapper;


import com.mfh.framework.api.constant.BizType;
import com.mfh.litecashier.bean.DailysettleAggItem;
import com.mfh.litecashier.bean.HandoverAggItem;

import java.util.List;

/**
 * 交接班/日结经营分析数据
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class AggWrapper implements java.io.Serializable{

    private Double grossProfit = 0D;//毛利
    private Double turnOver = 0D;//营业额合计

    private Double posAmount = 0D;
    private Double posQuantity = 0D;
    private Double scAmount = 0D;
    private Double scQuantity = 0D;
    private Double laundryAmount = 0D;
    private Double laundryQuantity = 0D;
    private Double pijuAmount = 0D;
    private Double pijuQuantity = 0D;
    private Double courierAmount = 0D;
    private Double courierQuantity = 0D;
    private Double expressAmount = 0D;
    private Double expressQuantity = 0D;
    private Double rechargeAmount = 0D;
    private Double rechargeQuantity = 0D;


    public Double getPosAmount() {
        if (posAmount == null){
            return 0D;
        }
        return posAmount;
    }

    public void setPosAmount(Double posAmount) {
        this.posAmount = posAmount;
    }

    public Double getPosQuantity() {
        if (posQuantity == null){
            return 0D;
        }
        return posQuantity;
    }

    public void setPosQuantity(Double posQuantity) {
        this.posQuantity = posQuantity;
    }

    public Double getScAmount() {
        if (scAmount == null){
            return 0D;
        }
        return scAmount;
    }

    public void setScAmount(Double scAmount) {
        this.scAmount = scAmount;
    }

    public Double getScQuantity() {
        if (scQuantity == null){
            return 0D;
        }
        return scQuantity;
    }

    public void setScQuantity(Double scQuantity) {
        this.scQuantity = scQuantity;
    }

    public Double getLaundryAmount() {
        if (laundryAmount == null){
            return 0D;
        }
        return laundryAmount;
    }

    public void setLaundryAmount(Double laundryAmount) {
        this.laundryAmount = laundryAmount;
    }

    public Double getLaundryQuantity() {
        if (laundryQuantity == null){
            return 0D;
        }
        return laundryQuantity;
    }

    public void setLaundryQuantity(Double laundryQuantity) {
        this.laundryQuantity = laundryQuantity;
    }

    public Double getPijuAmount() {
        if (pijuAmount == null){
            return 0D;
        }
        return pijuAmount;
    }

    public void setPijuAmount(Double pijuAmount) {
        this.pijuAmount = pijuAmount;
    }

    public Double getPijuQuantity() {
        if (pijuQuantity == null){
            return 0D;
        }
        return pijuQuantity;
    }

    public void setPijuQuantity(Double pijuQuantity) {
        this.pijuQuantity = pijuQuantity;
    }

    public Double getCourierAmount() {
        if (courierAmount == null){
            return 0D;
        }
        return courierAmount;
    }

    public void setCourierAmount(Double courierAmount) {
        this.courierAmount = courierAmount;
    }

    public Double getCourierQuantity() {
        if (courierQuantity == null){
            return 0D;
        }
        return courierQuantity;
    }

    public void setCourierQuantity(Double courierQuantity) {
        this.courierQuantity = courierQuantity;
    }

    public Double getExpressAmount() {
        if (expressAmount == null){
            return 0D;
        }
        return expressAmount;
    }

    public void setExpressAmount(Double expressAmount) {
        this.expressAmount = expressAmount;
    }

    public Double getExpressQuantity() {
        if (expressQuantity == null){
            return 0D;
        }
        return expressQuantity;
    }

    public void setExpressQuantity(Double expressQuantity) {
        this.expressQuantity = expressQuantity;
    }

    public Double getRechargeAmount() {
        if (rechargeAmount == null){
            return 0D;
        }
        return rechargeAmount;
    }

    public void setRechargeAmount(Double rechargeAmount) {
        this.rechargeAmount = rechargeAmount;
    }

    public Double getRechargeQuantity() {
        if (rechargeQuantity == null){
            return 0D;
        }
        return rechargeQuantity;
    }

    public void setRechargeQuantity(Double rechargeQuantity) {
        this.rechargeQuantity = rechargeQuantity;
    }

    public Double getGrossProfit() {
        if (grossProfit == null){
            return 0D;
        }
        return grossProfit;
    }

    public void setGrossProfit(Double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public Double getTurnOver() {
        if (turnOver == null){
            return 0D;
        }
        return turnOver;
    }

    public void setTurnOver(Double turnOver) {
        this.turnOver = turnOver;
    }

    private void reset(){
        this.turnOver = 0D;
        this.grossProfit = 0D;
        this.posAmount = 0D;
        this.posQuantity = 0D;
        this.scAmount = 0D;
        this.scQuantity = 0D;
        this.laundryAmount = 0D;
        this.laundryQuantity = 0D;
        this.pijuAmount = 0D;
        this.pijuQuantity = 0D;
        this.courierAmount = 0D;
        this.courierQuantity = 0D;
        this.expressAmount = 0D;
        this.expressQuantity = 0D;
        this.rechargeAmount = 0D;
        this.rechargeQuantity = 0D;
    }

    /**
     * 日结经营分析
     * */
    public void initWithDailysettleAggItems(List<DailysettleAggItem> entityList){
        reset();

        if (entityList == null || entityList.size() < 1){
            return;
        }

        for (DailysettleAggItem entity : entityList){
            //线下商超
            if (entity.getBizType().equals(BizType.POS)){
                this.turnOver += entity.getTurnover();
                this.grossProfit += entity.getGrossProfit();
                posAmount = entity.getTurnover();
                posQuantity = entity.getOrderNum();
            }
            //线上商城
            else if (entity.getBizType().equals(BizType.SC)){
                this.turnOver += entity.getTurnover();
                this.grossProfit += entity.getGrossProfit();
                scAmount = entity.getTurnover();
                scQuantity = entity.getOrderNum();
            }
            //衣服洗护
            else if (entity.getBizType().equals(BizType.LAUNDRY)){
                this.turnOver += entity.getTurnover();
                this.grossProfit += entity.getGrossProfit();
                laundryAmount = entity.getTurnover();
                laundryQuantity = entity.getOrderNum();
            }
            //皮具护理
            else if (entity.getBizType().equals(BizType.PIJU)){
                this.turnOver += entity.getTurnover();
                this.grossProfit += entity.getGrossProfit();
                pijuAmount = entity.getTurnover();
                pijuQuantity = entity.getOrderNum();
            }
            else if (entity.getBizType().equals(BizType.STOCK)){
                this.turnOver += entity.getTurnover();
                this.grossProfit += entity.getGrossProfit();
                courierAmount = entity.getTurnover();
                courierQuantity = entity.getOrderNum();
            }
            else if (entity.getBizType().equals(BizType.TRANSPORT)){
                this.turnOver += entity.getTurnover();
                this.grossProfit += entity.getGrossProfit();
                expressAmount = entity.getTurnover();
                expressQuantity = entity.getOrderNum();
            }
            //代充值业务
            else if (entity.getBizType().equals(BizType.RECHARGE)){
                this.turnOver += entity.getTurnover();
                this.grossProfit += entity.getGrossProfit();
                rechargeAmount = entity.getTurnover();
                rechargeQuantity = entity.getOrderNum();
            }
        }
    }

    /**
     * 交接班经营分析
     * */
    public void initWithHandoverAggShift(List<HandoverAggItem> entityList){
        reset();

        if (entityList == null || entityList.size() < 1){
            return;
        }

        for (HandoverAggItem entity : entityList){
            if (entity.getBizType().equals(BizType.POS)){
                this.turnOver += entity.getTurnover();
                posAmount = entity.getTurnover();
                posQuantity = entity.getOrderNum();
            }
            else if (entity.getBizType().equals(BizType.SC)){
                this.turnOver += entity.getTurnover();
                scAmount = entity.getTurnover();
                scQuantity = entity.getOrderNum();
            }
            else if (entity.getBizType().equals(BizType.LAUNDRY)){
                this.turnOver += entity.getTurnover();
                laundryAmount = entity.getTurnover();
                laundryQuantity = entity.getOrderNum();
            }
            else if (entity.getBizType().equals(BizType.PIJU)){
                this.turnOver += entity.getTurnover();
                pijuAmount = entity.getTurnover();
                pijuQuantity = entity.getOrderNum();
            }
            else if (entity.getBizType().equals(BizType.TRANSPORT)){
                this.turnOver += entity.getTurnover();
                courierAmount = entity.getTurnover();
                courierQuantity = entity.getOrderNum();
            }
            else if (entity.getBizType().equals(BizType.INVENTORY)){
                this.turnOver += entity.getTurnover();
                expressAmount = entity.getTurnover();
                expressQuantity = entity.getOrderNum();
            }
            else if (entity.getBizType().equals(BizType.RECHARGE)){
                this.turnOver += entity.getTurnover();
                rechargeAmount = entity.getTurnover();
                rechargeQuantity = entity.getOrderNum();
            }
        }
    }
}
