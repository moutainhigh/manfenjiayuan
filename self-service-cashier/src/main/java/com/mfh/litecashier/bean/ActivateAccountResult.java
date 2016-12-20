package com.mfh.litecashier.bean;


/**
 * 用户开卡结果
 * Created by Administrator on 2015/5/14.
 *
 * <p>
 */
public class ActivateAccountResult implements java.io.Serializable{
    private Long id;
    private Long ownerId;
    private Integer accountType;
    public ActivateAccountResult(){
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }
}
