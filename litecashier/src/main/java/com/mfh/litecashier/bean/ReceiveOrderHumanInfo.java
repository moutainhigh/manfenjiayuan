package com.mfh.litecashier.bean;

/**
 * 用户信息
 * Created by Administrator on 2015/5/14.
 *
 */
public class ReceiveOrderHumanInfo implements java.io.Serializable{
    private Long humanid;
    private Long addrid;
    private Long addressId;
    private String name;//姓名
    private String mobile;//手机号
    private String shortAddr;
    private String address;
    private Integer isBlack;
    private String subdisName;
    private Long subdisId;
    private Integer bindwx;

    public Long getHumanid() {
        return humanid;
    }

    public void setHumanid(Long humanid) {
        this.humanid = humanid;
    }

    public Long getAddrid() {
        return addrid;
    }

    public void setAddrid(Long addrid) {
        this.addrid = addrid;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getShortAddr() {
        return shortAddr;
    }

    public void setShortAddr(String shortAddr) {
        this.shortAddr = shortAddr;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getIsBlack() {
        return isBlack;
    }

    public void setIsBlack(Integer isBlack) {
        this.isBlack = isBlack;
    }

    public String getSubdisName() {
        return subdisName;
    }

    public void setSubdisName(String subdisName) {
        this.subdisName = subdisName;
    }

    public Long getSubdisId() {
        return subdisId;
    }

    public void setSubdisId(Long subdisId) {
        this.subdisId = subdisId;
    }

    public Integer getBindwx() {
        return bindwx;
    }

    public void setBindwx(Integer bindwx) {
        this.bindwx = bindwx;
    }
}
