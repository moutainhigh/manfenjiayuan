package com.bingshanguxue.cashier.v2;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.v1.CashierAgent;
import com.bingshanguxue.cashier.model.OrderMarketRules;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.bingshanguxue.cashier.model.wrapper.CouponRule;
import com.bingshanguxue.cashier.model.wrapper.DiscountInfo;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.anlaysis.logger.ZLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 收银订单
 * Created by Administrator on 2015/5/14.
 */
public class CashierOrderInfo implements java.io.Serializable {
    //以下字段支付的时候才会赋值
    //业务类型
    private Integer bizType;
    //订单条码
    private String posTradeNo;
    private String subject;

    //拆分子订单明细
    private List<CashierOrderItemInfo> mCashierOrderItemInfos;
    //当mCashierOrderItemInfos发生改变的时候，更新下面数据信息
    private Double bCount = 0D;//商品总数量
    private Double retailAmount = 0D;//商品零售总金额
    private Double finalAmount = 0D;//商品调价后总金额
    private Double adjustAmount = 0D;//价格调整优惠
    //已支付金额
    private Double paidAmount = 0D;

    private List<DiscountInfo> mDiscountInfos;

    public Double getbCount() {
        return bCount;
    }

    public void setbCount(Double bCount) {
        this.bCount = bCount;
    }

    public Double getRetailAmount() {
        return retailAmount;
    }

    public void setRetailAmount(Double retailAmount) {
        this.retailAmount = retailAmount;
    }

    public Double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public Double getAdjustAmount() {
        return adjustAmount;
    }

    public void setAdjustAmount(Double adjustAmount) {
        this.adjustAmount = adjustAmount;
    }

    public List<DiscountInfo> getDiscountInfos() {
        return mDiscountInfos;
    }

    public void setDiscountInfos(List<DiscountInfo> discountInfos) {
        discountInfos = discountInfos;
    }

    public Double getChange() {
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    private void onDatasetChanged(){
        Double bCount = 0D, retailAmount = 0D, finalAmount = 0D, adjustAmount = 0D;
        List<DiscountInfo> discountInfos = new ArrayList<>();
        if (mCashierOrderItemInfos != null && mCashierOrderItemInfos.size() > 0) {
            for (CashierOrderItemInfo itemInfo : mCashierOrderItemInfos) {
                bCount += itemInfo.getbCount();
                retailAmount += itemInfo.getRetailAmount();
                finalAmount += itemInfo.getFinalAmount();
                adjustAmount += itemInfo.getAdjustDiscountAmount();
                discountInfos.add(itemInfo.getDiscountInfo());
            }
        }

        this.bCount = bCount;
        this.retailAmount = retailAmount;
        this.finalAmount = finalAmount;
        this.adjustAmount = adjustAmount;
        this.mDiscountInfos = discountInfos;
    }


    //找零金额
    private Double change = 0D;

    //会员消费
    private Human vipMember;//会员

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }


    public String getPosTradeNo() {
        return posTradeNo;
    }

    public void setPosTradeNo(String posTradeNo) {
        this.posTradeNo = posTradeNo;
    }

    public List<CashierOrderItemInfo> getCashierOrderItemInfos() {
        return mCashierOrderItemInfos;
    }

    public void setCashierOrderItemInfos(List<CashierOrderItemInfo> cashierOrderItemInfos) {
        mCashierOrderItemInfos = cashierOrderItemInfos;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

    public Human getVipMember() {
        return this.vipMember;
    }

    public void setVipMember(Human vipMember) {
        this.vipMember = vipMember;
    }

    /**
     * 结算收银订单
     *
     * @param orderBarCode 订单编号
     * @param bizType    业务类型
     * @param mCashierOrderItemInfos 结算明细
     *                   TOTO,修改支付类型和已结算金额
     */
    public void initCashierSetle(String orderBarCode, Integer bizType,
                                 List<CashierOrderItemInfo> mCashierOrderItemInfos,
                                 String subject, Human vipMember, Double paidAmount) {
        this.posTradeNo = orderBarCode;
        this.bizType = bizType;
        this.mCashierOrderItemInfos = mCashierOrderItemInfos;
        this.subject = subject;
        this.vipMember = vipMember;
        this.paidAmount = paidAmount;
        onDatasetChanged();
    }

    /**
     * 初始化快捷支付
     *
     * @param bizType                业务类型
     * @param orderBarCode           订单编号（日结：机器设备号_日期）
     * @param mCashierOrderItemInfos 已支付金额
     *                               TOTO,修改支付类型和已结算金额
     */
    public void initQuickPayment(Integer bizType, String orderBarCode,
                                 List<CashierOrderItemInfo> mCashierOrderItemInfos,
                                 String subject, Human vipMember) {
        this.posTradeNo = orderBarCode;
        this.bizType = bizType;
        this.mCashierOrderItemInfos = mCashierOrderItemInfos;
        this.subject = subject;
        this.vipMember = vipMember;
        onDatasetChanged();
    }


    /**
     * 保存卡券和促销规则
     */
    public void couponPrivilege(List<OrderMarketRules> marketRulesList) {
        //没有明细，无法保存优惠券
        if (mCashierOrderItemInfos == null || mCashierOrderItemInfos.size() < 1) {
            return;
        }

        //优惠券和明细不匹配
        if (marketRulesList == null || marketRulesList.size() != mCashierOrderItemInfos.size()) {
            for (CashierOrderItemInfo cashierOrderItemInfo : mCashierOrderItemInfos) {
                cashierOrderItemInfo.setOrderMarketRules(null);
            }
            return;
        }

        for (int i = 0; i < marketRulesList.size(); i++) {
            CashierOrderItemInfo cashierOrderItemInfo = mCashierOrderItemInfos.get(i);

            OrderMarketRules orderMarketRules = marketRulesList.get(i);
            orderMarketRules.setSplitOrderId(cashierOrderItemInfo.getOrderId());
            orderMarketRules.setFinalAmount(cashierOrderItemInfo.getFinalAmount());
            cashierOrderItemInfo.setOrderMarketRules(orderMarketRules);
        }
    }

    /**
     * 保存抵用优惠券,优惠券(代金券)优惠
     * @param amountArray 优惠金额
     * @param couponsMap
     */
    public boolean saveCouponDiscount(List<PayAmount> amountArray,
                          Map<Long, List<CouponRule>> couponsMap) {
        //没有明细，无法保存优惠券计算结果
        if (mCashierOrderItemInfos == null || mCashierOrderItemInfos.size() < 1) {
            ZLogger.df("没有明细，无法保存优惠券计算结果");
            return false;
        }

        //检查输入参数是否正确
        if (amountArray == null ||
                amountArray.size() != mCashierOrderItemInfos.size()) {
            ZLogger.df("输入参数不正确");
            return false;
        }
        ZLogger.df(String.format("保存会员/优惠券优惠金额\n%s", JSON.toJSONString(amountArray)));

        for (int i = 0; i < mCashierOrderItemInfos.size(); i++) {
            CashierOrderItemInfo cashierOrderItemInfo = mCashierOrderItemInfos.get(i);
            PayAmount payAmount = amountArray.get(i);

            //2016-07-04 这里的促销卡券优惠金额都是后台返回来的，可能单个值或累加值大于应付金额，
            // 保存的时候应该有所裁剪，否则可能会影响后面的计算结果
            DiscountInfo discountInfo = cashierOrderItemInfo.getDiscountInfo();
            discountInfo.setRuleDiscountAmount(payAmount.getRuleAmount());
            discountInfo.setCouponDiscountAmount(payAmount.getCoupAmount());
            discountInfo.setEffectAmount(cashierOrderItemInfo.getFinalAmount()
                    - cashierOrderItemInfo.getPaidAmount() - payAmount.getPayAmount());
            // TODO: 5/26/16 选中的优惠券和订单可用的优惠券进行二次校验
            discountInfo.setCouponsIds(CashierAgent.getSelectCouponIds(couponsMap,
                    cashierOrderItemInfo.getOrderId()));
            discountInfo.setRuleIds(CashierAgent.getRuleIds(cashierOrderItemInfo.getOrderMarketRules()));
            cashierOrderItemInfo.setDiscountInfo(discountInfo);
        }

        onDatasetChanged();

        return true;
    }

    /**
     * VIP特权
     */
    public void vipPrivilege(Human vipMember) {
        this.vipMember = vipMember;
    }

    //支付完成
    public void paid(Integer payType, Double amount) {
        //ZLogger.d("paid.payType" + this.payType);
        this.paidAmount += amount;
    }


    @Override
    public String toString() {
        return String.format("cashierOrderInfo:\n%s", JSON.toJSONString(this));
    }
}
