package com.manfenjiayuan.business.bean;

/**
 * 满分账号支付订单响应
 * Created by NAT.ZZN on 2015/5/14.
 *
 */
public class AccountPayResponse implements java.io.Serializable{
    private String id;//订单编号

    public AccountPayResponse(){
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
