package com.mfh.enjoycity.bean;

/**
 * 商品详情
 * Created by Nat.ZZN on 2015/5/14.
 *
 */
public class Product implements java.io.Serializable{

    private Long id;//商品编号
    private String name;//商品名称
    private Long procateId;//类目编号

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getProcateId() {
        return procateId;
    }

    public void setProcateId(Long procateId) {
        this.procateId = procateId;
    }
}
