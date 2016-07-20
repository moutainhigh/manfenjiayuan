package com.mfh.enjoycity.database;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.IStringId;
import com.mfh.framework.core.MfhEntity;

/**
 * 匿名用户·地址表
 * Created by Nat.ZZN on 15-8-6..
 */
@Table(name="annoymous_address")
public class AnonymousAddressEntity extends MfhEntity<String> implements IStringId{
    private Long subdisId; //小区编号
    private String subName; //小区名
    private String addrName; //楼幢地址名


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

    public String getAddrName() {
        return addrName;
    }

    public void setAddrName(String addrName) {
        this.addrName = addrName;
    }

}
