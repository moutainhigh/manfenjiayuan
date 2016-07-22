package com.bingshanguxue.cashier.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.constant.BizSubType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.MfhEntity;

/**
 * POS--订单销售流水
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name = "tb_pos_order_v3")
public class PosOrderEntity extends MfhEntity<Long> implements ILongId {
    /**POS唯一订单号(12位字符串),流水号，可拆分成多个订单,拆分后的订单共用一个posTradeNo*/
    private String barCode;

    public static final Integer DEACTIVE = 0;
    public static final Integer ACTIVE = 1;
    /**是否激活状态，0 已关闭；1 已激活（默认值）
     * 订单关闭后，对应的订单明细*/
    private Integer isActive = ACTIVE;

    private Integer bizType;//业务类型
    private Integer subType;//业务子类型，根据商品而定

    private String posId; //POS编号

    /**
     * 订单状态
     * 0(0)-初始
     * 1(01)-挂起
     * 2(10)-等待支付
     * 4(100)-结束（由于历史原因，值为4，不能改变）
     * 8(1000)-支付处理中（已经支付过一部分金额）
     * 16(10000)- 订单异常
     */
    public static final int ORDER_STATUS_INIT = 0;//初始
    public static final int ORDER_STATUS_HANGUP = 1;//挂起
    public static final int ORDER_STATUS_EXCEPTION = 16;//订单异常
    public static final int ORDER_STATUS_STAY_PAY = 2;//等待支付
    public static final int ORDER_STATUS_PROCESS = 8;//支付处理中（支付中断或者正在进行支付）
    public static final int ORDER_STATUS_FINISH = 4;//结束（由于历史原因，值为4，不能改变）
    private int status = ORDER_STATUS_INIT;

    private Long humanId; //订单请求人，业主ID



    /**
     * 同步状态
     * 0-初始
     * 1-已同步
     */
    public static final int SYNC_STATUS_NONE = 0;//初始状态
    public static final int SYNC_STATUS_SYNCED = 1;//结束状态
    private int syncStatus = SYNC_STATUS_NONE;//同步参数：0，未同步；1已同步

    /**
     * 支付状态
     * 0-未付款
     * 1-已付款(全部或部分)
     */
    public static final int PAY_STATUS_NO = 0;          //未付款
    public static final int PAY_STATUS_YES = 1;         //已付款
    private int paystatus = PAY_STATUS_NO;//：1，未付款

    private String remark = ""; //备注

    //商品零售金额(按商品零售价计算的总金额)，
    private Double retailAmount = 0D;
    //商品成交金额(按商品成交计算的总金额)
    private Double finalAmount = 0D;
    //折扣价1(价格调整)
    private Double discountAmount = 0D;

    private Double bcount = 0D; //总件数

    //deprecated废弃
    private Long companyId; //超市商家
    private Long sellOffice; //销售网点
    private Long sellerId;//订单所属租户编号(销售网点的公司);//spid，ternatid




    /**
     * 支付方式<br>
     * {@link WayType#CASH 1 现金}<br>
     */
    private Integer payType;
    private String couponsIds = ""; //优惠券号列表
    private String ruleIds = "";//促销规则
    //折扣价2(促销规则&卡券优惠)
    private Double ruleDiscountAmount = 0D;
    //已支付金额＝实际收取金额＋折扣价2 >＝ 商品成交金额＝折扣价1＋商品零售金额
    private Double paidAmount = 0D;
    //找零金额，与paidAmount一样，订单支付记录表中对应AMOUNT_TYPE_OUT的金额总和
    //注意：只记录当前订单找零金额，不纳入统计
    private Double change = 0D;
    private Double score = 0D; //订单总积分

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
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

    public Double getRetailAmount() {
        if (retailAmount == null) {
            return 0D;
        }
        return retailAmount;
    }

    public void setRetailAmount(Double retailAmount) {
        this.retailAmount = retailAmount;
    }

    public Double getFinalAmount() {
        if (finalAmount == null) {
            return 0D;
        }
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public Double getDiscountAmount() {
        if (discountAmount == null) {
            return 0D;
        }
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
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
    public Double getRuleDiscountAmount() {
        if (ruleDiscountAmount == null) {
            return 0D;
        }
        return ruleDiscountAmount;
    }

    public void setRuleDiscountAmount(Double ruleDiscountAmount) {
        this.ruleDiscountAmount = ruleDiscountAmount;
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

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Double getChange() {
        if (change == null) {
            return 0D;
        }
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
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

    public Integer getSubType() {
        if (subType == null){
            return BizSubType.POS_STANDARD;
        }
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

}
