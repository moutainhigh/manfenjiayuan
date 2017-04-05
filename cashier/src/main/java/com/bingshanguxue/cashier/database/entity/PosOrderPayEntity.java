package com.bingshanguxue.cashier.database.entity;

import com.bingshanguxue.cashier.model.wrapper.PayWayType;
import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.abs.MfhEntity;
import com.mfh.framework.api.constant.WayType;

/**
 * POS支付记录
 *
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..<br>
 * <table>
 *     <tr>
 *         <th>id</th>
 *     </tr>
 *     <tr>
 *         <td>Long</td>
 *     </tr>
 *     <tr>
 *         <td>自增主键</td>
 *     </tr>
 * </table>
 * ------------------------------------------------------------------------<br>
 * String       | int         | int      | Double   | String       | String<br>
 * orderBarCode | paystatus   | payType  | amount   | outTradeNo   | remark<br>
 * ------------------------------------------------------------------------<br>
 */
@Table(name="tb_pos_order_pay_v2")
public class PosOrderPayEntity extends MfhEntity<Long> implements ILongId {

    /**订单编号<br>
     * 日结单和订单流水共用一套序列
     * 关联收银流水订单{@link PosOrderEntity}/
     * 日结单{@link com.mfh.litecashier.database.entity.DailysettleEntity}
     * */
    private Long orderId;

    /**
     * 本地商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。<br>
     * 每一次支付记录都对应一个商户订单号
     * 终端号＋订单号＋时间戳（13位）
     * */
    private String outTradeNo = "";


    public static final int PAY_STATUS_INIT     = 0;//初始状态
    public static final int PAY_STATUS_STAY_PAY = 1;//等待支付
    public static final int PAY_STATUS_PROCESS  = 2;//支付处理中
    public static final int PAY_STATUS_EXCEPTION= 3;//支付异常
    public static final int PAY_STATUS_FINISH   = 4;//支付完成
    public static final int PAY_STATUS_FAILED   = 5;//支付失败
    public static final int PAY_STATUS_CANCELED = 6;//交易取消
    public static final int PAY_STATUS_REFUND   = 7;//退款
    /**支付状态*/
    private int paystatus = PAY_STATUS_INIT;

    /**
     * POS支付方式{@link Enumerate#WAY_TYPE_CASH}
     * */
    private Integer payType;
    private Double amount = 0D;//支付/找零金额


    /**支付记录所属类别
     * {@link PayWayType}*/
    private Integer amountType = PayWayType.TYPE_NA;

    /**
     * 会员ID<br>
     * 赊账记录都对应一个会员ID
     * */
    private Long customerHumanId;
    private String couponsIds;//优惠券ids
    private String ruleIds;//促销规则

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public Double getAmount() {
        if (amount == null){
            return 0D;
        }
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getAmountType() {
        return amountType;
    }

    public void setAmountType(Integer amountType) {
        this.amountType = amountType;
    }

    public String getOutTradeNo() {
        if (outTradeNo == null){
            return "";
        }
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public Long getCustomerHumanId() {
        return customerHumanId;
    }

    public void setCustomerHumanId(Long customerHumanId) {
        this.customerHumanId = customerHumanId;
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

    /**
     * 获取支付状态描述
     * @param payStatus 支付状态<br>
     * {@link #PAY_STATUS_INIT}<br>
     * {@link #PAY_STATUS_STAY_PAY}<br>
     * {@link #PAY_STATUS_PROCESS}<br>
     * {@link #PAY_STATUS_EXCEPTION}<br>
     * {@link #PAY_STATUS_FINISH}<br>
     * {@link #PAY_STATUS_FAILED}<br>
     * */
    public static String getPayStatusDesc(int payStatus){
        if (payStatus == PosOrderPayEntity.PAY_STATUS_INIT) {
            return "初始状态";
        } else if (payStatus == PosOrderPayEntity.PAY_STATUS_STAY_PAY) {
            return "等待支付";
        } else if (payStatus == PosOrderPayEntity.PAY_STATUS_PROCESS) {
            return "支付处理中";
        } else if (payStatus == PosOrderPayEntity.PAY_STATUS_EXCEPTION) {
            return "支付异常";
        } else if (payStatus == PosOrderPayEntity.PAY_STATUS_FINISH) {
            return "支付成功";
        } else if (payStatus == PosOrderPayEntity.PAY_STATUS_FAILED) {
            return "支付失败";
        }
        return "";
    }
}
