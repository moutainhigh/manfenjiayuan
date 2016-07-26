package com.mfh.litecashier.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.core.MfhEntity;

/**
 * 金额授权模式-现金额度
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name="tb_quota")
public class QuotaEntity extends MfhEntity<Long> implements ILongId{

    //最大现金额度，后面可能要在云端后台配置
    public static final Double MAX_QUOTA = 10D;

    //总的金额
    private Double amount = 0D;


    public static final Integer PAY_STATYS_INIT = 0;
    public static final Integer PAY_STATYS_PROCESS = 1;//可以继续支付
    public static final Integer PAY_STATYS_FAILED = 2;//可以继续支付
    public static final Integer PAY_STATYS_PAID = 3;
    //支付状态：1-已支付，0-未支付
    private Integer payStatus = PAY_STATYS_INIT;

    public Double getAmount() {
        if (amount == null){
            return 0D;
        }
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }
}
