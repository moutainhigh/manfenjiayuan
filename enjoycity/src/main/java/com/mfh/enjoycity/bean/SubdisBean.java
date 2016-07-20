package com.mfh.enjoycity.bean;

/**
 * 小区
 * Created by Nat.ZZN(bingshanguxue) on 2015/5/14.
 *
 */
public class SubdisBean implements java.io.Serializable{
    private Long id;//小区编号
    private String subdisName;//小区名
    private String street;//小区地址


    public String getSubdisName() {
        return subdisName;
    }

    public void setSubdisName(String subdisName) {
        this.subdisName = subdisName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
