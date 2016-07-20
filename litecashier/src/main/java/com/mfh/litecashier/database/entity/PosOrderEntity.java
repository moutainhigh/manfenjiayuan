package com.mfh.litecashier.database.entity;

import com.mfh.framework.core.MfhEntity;
import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;

/**
 * POS--订单销售流水
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name = "tb_pos_order_v2")
public class PosOrderEntity extends MfhEntity<Long> implements ILongId {
    private String barCode; //订单条码号(POS机本地生成的条码)
    private Long humanId; //订单请求人，业主ID

    /**
     * 订单状态
     * 0(0)-初始
     * 1(01)-挂起
     * 2(10)-等待支付
     * 4(100)-结束（由于历史原因，值为4，不能改变）
     * 8(1000)-支付处理中（已经支付过一部分金额）
     * 16(10000)- 订单异常
     */
    public static final int ORDER_STATUS_INIT       = 0;//初始
    public static final int ORDER_STATUS_HANGUP     = 1;//挂起
    public static final int ORDER_STATUS_STAY_PAY   = 2;//等待支付
    public static final int ORDER_STATUS_PROCESS    = 8;//支付处理中（支付中断或者正在进行支付）
    public static final int ORDER_STATUS_EXCEPTION  = 16;//订单异常
    public static final int ORDER_STATUS_FINISH     = 4;//结束（由于历史原因，值为4，不能改变）
    private int status = ORDER_STATUS_INIT;

    /**
     * 同步状态
     * 0-初始
     * 1-已同步
     */
    public static final int SYNC_STATUS_NONE    = 0;//初始状态
    public static final int SYNC_STATUS_SYNCED  = 1;//结束状态
    private int syncStatus = SYNC_STATUS_NONE;//同步参数：0，未同步；1已同步

    /**
     * 支付状态
     * 0-未付款
     * 1-已付款(全部或部分)
     */
    public static final int PAY_STATUS_NO = 0;          //未付款
    public static final int PAY_STATUS_YES = 1;         //已付款
    private int paystatus = PAY_STATUS_NO;//：1，未付款

    private String remark; //备注

    //商品实际金额(按商品零售价计算的总金额)，
    private Double retailAmount = 0D;
    //商品实际交易金额(按商品实际成交价计算的总金额)
//    private Double actualAmount = 0D;
    //折扣价(会员优惠)
    private Double discountAmount = 0D;
    //折扣价(卡券优惠)
    private Double couponDiscountAmount = 0D;
    //实际支付金额（包含找零金额）
    private Double paidAmount = 0D;

    private String adjPrice; //价格调整
    private String couponsIds; //优惠券号列表
    private String ruleIds;
    private Double score = 0D; //订单总积分
    private Double bcount = 0D; //总件数

    /**
     * 支付方式<br>
     * {@link Enumerate#WAY_TYPE_CASH 1 现金}<br>
     * */
    private Integer payType;
    //deprecated废弃
    private Long companyId; //超市商家
    private String posId; //POS编号
    private Long sellOffice; //销售网点
    private Long sellerId;//订单所属租户编号(销售网点的公司);//spid，ternatid

    private Integer bizType;//业务类型
    private Double paidMoney = 0D;//实收金额－－打印小票需要
    private Double charge = 0D;//找零－－POS收银显示需要,（应付金额，负数表示需要找零，正数表示需要支付）


    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public Long getHumanId() {
        return humanId;
    }

    public void setHumanId(Long humanId) {
        this.humanId = humanId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    public String getAdjPrice() {
        return adjPrice;
    }

    public void setAdjPrice(String adjPrice) {
        this.adjPrice = adjPrice;
    }

    public String getCouponsIds() {
        return couponsIds;
    }

    public void setCouponsIds(String couponsIds) {
        this.couponsIds = couponsIds;
    }

    public String getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(String ruleIds) {
        this.ruleIds = ruleIds;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }


    public Double getRetailAmount() {
        if (retailAmount == null) {
            return 0D;
        }
        return retailAmount;
    }

    public void setRetailAmount(Double retailAmount) {
        this.retailAmount = retailAmount;
    }

//    public Double getActualAmount() {
//        if (actualAmount == null) {
//            return 0D;
//        }
//        return actualAmount;
//    }
//
//    public void setActualAmount(Double actualAmount) {
//        this.actualAmount = actualAmount;
//    }

    public Double getDiscountAmount() {
        if (discountAmount == null) {
            return 0D;
        }
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getCouponDiscountAmount() {
        if (couponDiscountAmount == null) {
            return 0D;
        }
        return couponDiscountAmount;
    }

    public void setCouponDiscountAmount(Double couponDiscountAmount) {
        this.couponDiscountAmount = couponDiscountAmount;
    }

    public Double getPaidAmount() {
        if (paidAmount == null) {
            return 0D;
        }
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Double getBcount() {
        return bcount;
    }

    public void setBcount(Double bcount) {
        this.bcount = bcount;
    }

    public int getPaystatus() {
        return paystatus;
    }

    public void setPaystatus(int paystatus) {
        this.paystatus = paystatus;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public Long getSellOffice() {
        return sellOffice;
    }

    public void setSellOffice(Long sellOffice) {
        this.sellOffice = sellOffice;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }


    public Double getPaidMoney() {
        if (paidMoney == null) {
            return 0D;
        }
        return paidMoney;
    }

    public void setPaidMoney(Double paidMoney) {
        this.paidMoney = paidMoney;
    }

    public Double getCharge() {
        if (charge == null) {
            return 0D;
        }
        return charge;
    }

    public void setCharge(Double charge) {
        this.charge = charge;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }
}
