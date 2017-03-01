package com.mfh.framework.api.account;

import com.mfh.framework.api.abs.MfhEntity;
import com.mfh.framework.api.commonuseraccount.CommonUserAccountApi;

/**
 * 用户账号信息
 * Created by bingshanguxue on 7/5/16.
 * {@link CommonUserAccountApi}
 */
public class UserAccount extends MfhEntity<Long> {
    /**
     * ownerId / humanId*/
    private Long ownerId;
//    private Integer accountType;//账号类型


    private Double cur_cash = 0D;//当前余额
    private Double con_cash = 0D;//已消费金额
    private Long cur_score = 0L;//当前积分
    private Long con_score = 0L;//已消费积分

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }


    public Double getCur_cash() {
        return cur_cash;
    }

    public void setCur_cash(Double cur_cash) {
        this.cur_cash = cur_cash;
    }

    public Double getCon_cash() {
        return con_cash;
    }

    public void setCon_cash(Double con_cash) {
        this.con_cash = con_cash;
    }

    public Long getCur_score() {
        return cur_score;
    }

    public void setCur_score(Long cur_score) {
        this.cur_score = cur_score;
    }

    public Long getCon_score() {
        return con_score;
    }

    public void setCon_score(Long con_score) {
        this.con_score = con_score;
    }

}
