package com.manfenjiayuan.mixicook_vip.ui.reserve;

import com.manfenjiayuan.business.bean.NetInfo;
import com.manfenjiayuan.mixicook_vip.database.PurchaseShopcartEntity;

import java.util.Date;
import java.util.List;

/**
 * 确认订单表单
 * Created by bingshanguxue on 7/12/16.
 */
public class OrderFormInfo {
    private NetInfo netInfo;
    private Date serviceTime;
    private String remark;
    private List<PurchaseShopcartEntity> shopcartEntities;

    public NetInfo getNetInfo() {
        return netInfo;
    }

    public void setNetInfo(NetInfo netInfo) {
        this.netInfo = netInfo;
    }

    public Date getServiceTime() {
        if (serviceTime == null){
            return new Date();
        }
        return serviceTime;
    }

    public void setServiceTime(Date serviceTime) {
        this.serviceTime = serviceTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<PurchaseShopcartEntity> getShopcartEntities() {
        return shopcartEntities;
    }

    public void setShopcartEntities(List<PurchaseShopcartEntity> shopcartEntities) {
        this.shopcartEntities = shopcartEntities;
    }
}
