package com.mfh.litecashier.bean;


/**
 * 用户开卡结果
 * Created by Administrator on 2015/5/14.
 *
 * <p>
 {<br>
    "ownerId":132660,<br>
     "accountType":2,<br>
     "accountCode":null,<br>
     "accountPassword":null,<br>
     "remark":"冰珊孤雪账户",<br>
     "cur_cash":300.0,<br>
     "con_cash":0.0,<br>
     "cur_score":109,<br>
     "con_score":0,<br>
     "inCash":300.0,<br>
     "freezeCash":0.0,<br>
     "miniRequestCash":0.0,<br>
     "ownerName":null,<br>
     "availableCash":0.0,<br>
     "id":3263,<br>
     "createdBy":"sys",<br>
     "createdDate":"2015-06-18 14:31:04",<br>
     "updatedBy":"sys",<br>
     "updatedDate":"2016-03-01 13:57:59"<br>
 }<br>
 *</p>
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
