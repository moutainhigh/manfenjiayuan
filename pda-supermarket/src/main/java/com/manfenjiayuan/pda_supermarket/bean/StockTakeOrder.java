package com.manfenjiayuan.pda_supermarket.bean;

import com.mfh.comn.bean.IStringId;

import java.util.Date;

/**
 * 库存商品
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 */
public class StockTakeOrder implements IStringId, java.io.Serializable {
//    {
//        "code": "0",
//            "msg": "查询成功!",
//            "version": "1",
//            "data": {
//                "status": 0,
//                "orderName": "满分测试POS2015-10-28盘点单",
//                "netId": "132079",
//                "profitNum": null,
//                "lossNum": null,
//                "sameNum": null,
//                "tenantId": "132079",
//                "id": "3",
//                "createdBy": "132079",
//                "createdDate": "2015-10-28 23:36:16",
//                "updatedBy": "",
//                "updatedDate": "2015-10-28 23:35:57"
//    }
//    }


    private String id;//盘点单号
    private String orderName;//盘点名称
    private String netId;//网点编号
    private String tenantId;//所属租户
    private String createdBy;//创建人id
    private Date createdDate;
    private Date updatedDate;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}
