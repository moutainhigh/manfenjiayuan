package com.bingshanguxue.cashier.database.entity;

import com.bingshanguxue.cashier.model.PayStatus;
import com.bingshanguxue.cashier.model.SyncStatus;
import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.abs.MfhEntity;

/**
 * POS充值记录
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
@Table(name="tb_pos_topup_v1")
public class PosTopupEntity extends MfhEntity<Long> implements ILongId {

    /**
     * 本地商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。<br>
     * 每一次支付记录都对应一个商户订单号
     * 终端号＋订单号＋时间戳（13位）
     * */
    private String outTradeNo = "";
    private Integer bizType = -1;
    private Integer subBizType = -1;
    private Double amount = 0D;//支付/找零金额

    //支付状态
    /**
     * POS支付方式{@link Enumerate#WAY_TYPE_CASH}
     * */
    private Integer payType = WayType.NA;
    private int paystatus = PayStatus.INIT;
    private int syncStatus = SyncStatus.INIT;

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

    public Integer getSubBizType() {
        return subBizType;
    }

    public void setSubBizType(Integer subBizType) {
        this.subBizType = subBizType;
    }
}
