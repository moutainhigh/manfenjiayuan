package com.mfh.litecashier.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.core.MfhEntity;

/**
 * POS支付记录
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..<br>
 * ------------------------------------------------------------------------<br>
 * String       | int         | int      | Double   | String       | String<br>
 * orderBarCode | paystatus   | payType  | amount   | outTradeNo   | remark<br>
 * ------------------------------------------------------------------------<br>
 */
@Table(name="tb_pos_order_pay_v1")
public class PosOrderPayEntity extends MfhEntity<Long> implements ILongId {
    /**订单条码<br>
     * 格式：机器设备号_业务类型_随机字符串<br>
     * 关联收银流水订单{@link com.mfh.litecashier.database.entity.PosOrderEntity}/
     * 日结单{@link com.mfh.litecashier.database.entity.DailysettleEntity}
     * */
    private String orderBarCode;

    /**
     * POS支付方式{@link Enumerate#WAY_TYPE_CASH}
     * */
    private Integer payType;

    //支付状态
    public static final int PAY_STATUS_INIT     = 0;//初始状态
    public static final int PAY_STATUS_STAY_PAY = 1;//等待支付
    public static final int PAY_STATUS_PROCESS  = 2;//支付处理中
    public static final int PAY_STATUS_EXCEPTION= 3;//支付异常
    public static final int PAY_STATUS_FINISH   = 4;//支付完成
    public static final int PAY_STATUS_FAILED   = 5;//支付失败
    public static final int PAY_STATUS_CANCELED = 6;//交易取消
    public static final int PAY_STATUS_REFUND   = 7;//退款
    private int paystatus = PAY_STATUS_INIT;

    private Double amount = 0D;//支付金额

    /**
     * 商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。<br>
     * 每一次支付记录都对应一个商户订单号
     * */
    private String outTradeNo = "";
    /**
     * 会员GUID<br>
     * 赊账记录都对应一个会员ID
     * */
    private String memberGUID = "";
    private String remark = ""; //备注:交易号


    public String getOrderBarCode() {
        return orderBarCode;
    }

    public void setOrderBarCode(String orderBarCode) {
        this.orderBarCode = orderBarCode;
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

    public String getOutTradeNo() {
        if (outTradeNo == null){
            return "";
        }
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getMemberGUID() {
        return memberGUID;
    }

    public void setMemberGUID(String memberGUID) {
        this.memberGUID = memberGUID;
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
