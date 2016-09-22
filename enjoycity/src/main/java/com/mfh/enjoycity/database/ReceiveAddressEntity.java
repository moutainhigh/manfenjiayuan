package com.mfh.enjoycity.database;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.IStringId;
import com.mfh.framework.api.abs.MfhEntity;

/**
 * 注册用户·地址表
 * Created by Nat.ZZN on 15-8-6..
 */
@Table(name="receive_address")
public class ReceiveAddressEntity extends MfhEntity<String> implements IStringId{
    private String receiver;//收货人
    private String telephone; //手机号

    private Long addressId;//地址编号
    private Long subdisId; //小区编号
    private String subName; //小区名
    private Long addrvalid;//公寓编号
    private String addrName; //楼幢地址名


    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }


    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Long getSubdisId() {
        return subdisId;
    }

    public void setSubdisId(Long subdisId) {
        this.subdisId = subdisId;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public Long getAddrvalid() {
        return addrvalid;
    }

    public void setAddrvalid(Long addrvalid) {
        this.addrvalid = addrvalid;
    }

    public String getAddrName() {
        return addrName;
    }

    public void setAddrName(String addrName) {
        this.addrName = addrName;
    }
}
