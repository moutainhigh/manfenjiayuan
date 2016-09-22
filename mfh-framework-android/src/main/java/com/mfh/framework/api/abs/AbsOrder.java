package com.mfh.framework.api.abs;

/**
 * 订单
 * Created by bingshanguxue on 9/22/16.
 */

public class AbsOrder extends MfhEntity<Long>{
    private Double amount; //订单原价、最终是订单实际的支付金额
    private String adjPrice; //手工修改的折扣金额，和discount对应
    private Double disAmount;//券、促销规则等计算出的优惠金额,这部分金额由发券方承担，但应计入营业额
    private String couponsIds; //领用的优惠券领用号列表(不是优惠券本身的编号)，若有多个逗号分隔
    private Double score = 0D; //订单总积分
    private Double discount; //订单总的折扣率 ,收银员可能有手工修改某个或某些明细，最终汇总到整个订单有明细,默认 = 1.0，和adjPrice对应；如果直接有个整个订单折扣，则各明细折扣率一样。这部分金额不应该计入营业额
    private Double bcount = 1D; //订单内明细总件数,默认是1

    private Integer paystatus;     //付款状态,0=未付款 1=已付款 MfhOrderConstant.MFHORDER_PAYSTATUS_NO

    private String barcode; //订单条形码
    private Integer btype; //订单物件类型 StockConstant.ORDER_TYPE_
    private Long humanId; //订单主人，订单的创建人是createby，两者不一定相等；在pc洗衣下单时，两者肯定不一样。在手机上，自己下单两者一致；但代购的话又不一致。
    private Integer status;       //订单状态，不同类型自定,3=待出库 6=已出库
    private String remark ="";    //备注

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getAdjPrice() {
        return adjPrice;
    }

    public void setAdjPrice(String adjPrice) {
        this.adjPrice = adjPrice;
    }

    public Double getDisAmount() {
        return disAmount;
    }

    public void setDisAmount(Double disAmount) {
        this.disAmount = disAmount;
    }

    public String getCouponsIds() {
        return couponsIds;
    }

    public void setCouponsIds(String couponsIds) {
        this.couponsIds = couponsIds;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getBcount() {
        return bcount;
    }

    public void setBcount(Double bcount) {
        this.bcount = bcount;
    }

    public Integer getPaystatus() {
        return paystatus;
    }

    public void setPaystatus(Integer paystatus) {
        this.paystatus = paystatus;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Integer getBtype() {
        return btype;
    }

    public void setBtype(Integer btype) {
        this.btype = btype;
    }

    public Long getHumanId() {
        return humanId;
    }

    public void setHumanId(Long humanId) {
        this.humanId = humanId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
