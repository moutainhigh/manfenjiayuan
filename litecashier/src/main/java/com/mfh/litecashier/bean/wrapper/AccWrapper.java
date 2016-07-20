package com.mfh.litecashier.bean.wrapper;


import com.mfh.framework.api.constant.WayType;
import com.mfh.litecashier.bean.DailysettleAccItem;
import com.mfh.litecashier.bean.HandoverAccItem;

import java.util.List;

/**
 * 交接班/日结流水分析数据
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class AccWrapper implements java.io.Serializable{

    private Double cashAmount = 0D;
    private Double cashQuantity = 0D;
    private Double alipayAmount = 0D;
    private Double alipayQuantity = 0D;
    private Double wxAmount = 0D;
    private Double wxQuantity = 0D;
    private Double memberAccountAmount = 0D;
    private Double memberAccountQuantity = 0D;
//    private Double scAmount = 0D;
//    private Double scQuantity = 0D;
    private Double bankcardAmount = 0D;
    private Double bankcardQuantity = 0D;

    public Double getCashAmount() {
        if (cashAmount == null){
            return 0D;
        }
        return cashAmount;
    }

    public void setCashAmount(Double cashAmount) {
        this.cashAmount = cashAmount;
    }

    public Double getCashQuantity() {
        if (cashQuantity == null){
            return 0D;
        }
        return cashQuantity;
    }

    public void setCashQuantity(Double cashQuantity) {
        this.cashQuantity = cashQuantity;
    }

    public Double getAlipayAmount() {
        if (alipayAmount == null){
            return 0D;
        }
        return alipayAmount;
    }

    public void setAlipayAmount(Double alipayAmount) {
        this.alipayAmount = alipayAmount;
    }

    public Double getAlipayQuantity() {
        if (alipayQuantity == null){
            return 0D;
        }
        return alipayQuantity;
    }

    public void setAlipayQuantity(Double alipayQuantity) {
        this.alipayQuantity = alipayQuantity;
    }

    public Double getWxAmount() {
        if (wxAmount == null){
            return 0D;
        }
        return wxAmount;
    }

    public void setWxAmount(Double wxAmount) {
        this.wxAmount = wxAmount;
    }

    public Double getWxQuantity() {
        if (wxQuantity == null){
            return 0D;
        }
        return wxQuantity;
    }

    public void setWxQuantity(Double wxQuantity) {
        this.wxQuantity = wxQuantity;
    }

    public Double getMemberAccountAmount() {
        if (memberAccountAmount == null){
            return 0D;
        }
        return memberAccountAmount;
    }

    public void setMemberAccountAmount(Double memberAccountAmount) {
        this.memberAccountAmount = memberAccountAmount;
    }

    public Double getMemberAccountQuantity() {
        if (memberAccountQuantity == null){
            return 0D;
        }
        return memberAccountQuantity;
    }

    public void setMemberAccountQuantity(Double memberAccountQuantity) {
        this.memberAccountQuantity = memberAccountQuantity;
    }

//    public Double getScAmount() {
//        if (scAmount == null){
//            return 0D;
//        }
//        return scAmount;
//    }
//
//    public void setScAmount(Double scAmount) {
//        this.scAmount = scAmount;
//    }
//
//    public Double getScQuantity() {
//        if (scQuantity == null){
//            return 0D;
//        }
//        return scQuantity;
//    }
//
//    public void setScQuantity(Double scQuantity) {
//        this.scQuantity = scQuantity;
//    }

    public Double getBankcardAmount() {
        if (bankcardAmount == null){
            return 0D;
        }
        return bankcardAmount;
    }

    public void setBankcardAmount(Double bankcardAmount) {
        this.bankcardAmount = bankcardAmount;
    }

    public Double getBankcardQuantity() {
        if (bankcardQuantity == null){
            return 0D;
        }
        return bankcardQuantity;
    }

    public void setBankcardQuantity(Double bankcardQuantity) {
        this.bankcardQuantity = bankcardQuantity;
    }

    private void reset(){
        cashAmount = 0D;
        cashQuantity = 0D;
        alipayAmount = 0D;
        alipayQuantity = 0D;
        wxAmount = 0D;
        wxQuantity = 0D;
        memberAccountAmount = 0D;
        memberAccountQuantity = 0D;
//        scAmount = 0D;
//        scQuantity = 0D;
        bankcardAmount = 0D;
        bankcardQuantity = 0D;
    }

    /**
     * 日结流水分析
     * */
    public void initWithDailysettleAccItems(List<DailysettleAccItem> entityList){
        reset();

        if (entityList == null || entityList.size() < 1){
            return;
        }

        for (DailysettleAccItem entity : entityList){
            //现金
            if (entity.getPayType().equals(WayType.CASH)){
                cashAmount = entity.getAmount();
                cashQuantity = entity.getOrderNum();
            }
            //支付宝扫码支付
            else if (entity.getPayType().equals(WayType.ALI_F2F)){
                alipayAmount = entity.getAmount();
                alipayQuantity = entity.getOrderNum();
            }
            //微信扫码付
            else if (entity.getPayType().equals(WayType.WX_F2F)){
                wxAmount = entity.getAmount();
                wxQuantity = entity.getOrderNum();
            }
            //满分账户
            else if (entity.getPayType().equals(WayType.MFACCOUNT)){
                memberAccountAmount = entity.getAmount();
                memberAccountQuantity = entity.getOrderNum();
            }
            //银联
            else if (entity.getPayType().equals(WayType.BANKCARD)){
                bankcardAmount = entity.getAmount();
                bankcardQuantity = entity.getOrderNum();
            }
        }
    }

    /**
     * 交接班流水分析
     * */
    public void initWithHandoverAccItems(List<HandoverAccItem> entityList){
        reset();

        if (entityList == null || entityList.size() < 1){
            return;
        }

        for (HandoverAccItem entity : entityList){
            if (entity.getPayType().equals(WayType.CASH)){
                cashAmount = entity.getAmount();
                cashQuantity = entity.getOrderNum();
            }
            else if (entity.getPayType().equals(WayType.ALI_F2F)){
                alipayAmount = entity.getAmount();
                alipayQuantity = entity.getOrderNum();
            }
            else if (entity.getPayType().equals(WayType.WX_F2F)){
                wxAmount = entity.getAmount();
                wxQuantity = entity.getOrderNum();
            }
            else if (entity.getPayType().equals(WayType.MFACCOUNT)){
                memberAccountAmount = entity.getAmount();
                memberAccountQuantity = entity.getOrderNum();
            }
//            else if (entity.getPayType().equals(Enumerate.WAY_TYPE_ALI)){
//                scAmount = entity.getAmount();
//                scQuantity = entity.getOrderNum();
//            }
            else if (entity.getPayType().equals(WayType.BANKCARD)){
                bankcardAmount = entity.getAmount();
                bankcardQuantity = entity.getOrderNum();
            }
        }
    }
}
