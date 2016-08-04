package com.mfh.framework.api.invSendOrder;

import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.invSendOrder.InvSendOrderItem;

import java.io.Serializable;
import java.util.List;

/**
 * 采购订单明细
 * <pre>
 *     {
 "receiveNetName": "满分测试POS",
 "sendNetName": "满分供应链",
 "sendCompanyName": "",
 "sendNetAddr": "苏州市工业园区大大的",
 "sendMobile": "1247595555555555",
 "statusCaption": "审批通过",
 "items": [
 {
 "imgUrl": "http://chunchunimage.b0.upaiyun.com/product/2030.png!small",
 "orderId": "90",
 "proSkuId": "1306",
 "chainSkuId": "2",
 "productName": "HD 送非机动囧囧囧哦_32g(包)",
 "unitSpec": null,
 "singleCount": null,
 "packCount": null,
 "totalCount": 2,
 "price": 0,
 "amount": 0,
 "receiveCount": null,
 "receiveAmount": null,
 "barcode": "1855020201203",
 "tenantId": "133424",
 "id": "161",
 "createdBy": "",
 "createdDate": "2016-01-06 11:45:54",
 "updatedBy": "",
 "updatedDate": null
 }
 ],
 "bizType": 0,
 "name": "满分供应链(大仓)配送销售开票单",
 "status": 1,
 "sendNetId": "133461",
 "receiveNetId": "132079",
 "contact": "辛道柱 ",
 "receiveMobile": "18021273683",
 "receiveAddr": "容易",
 "transFee": 30,
 "totalCount": 3,
 "goodsFee": 5,
 "finishTime": null,
 "totalFee": 35,
 "sendType": 1,
 "receiveCount": 0,
 "receiveGoodsFee": 0,
 "tenantId": "133424",
 "sendTenantId": null,
 "id": "90",
 "createdBy": "132079",
 "createdDate": "2015-12-16 13:59:10",
 "updatedBy": "",
 "updatedDate": "2016-01-04 17:17:32"
 }
 * </pre>
 * Created by bingshanguxue on 15/9/22.
 */
public class InvSendOrderItemBrief implements ILongId, Serializable {

    private Long id;//订单编号
    private String barcode;//单号
    private Double goodsFee;//商品金额
//    private Double transFee;//配送费
    private Double totalFee;//总金额
    private Double totalCount;//总数
    private Integer status;//订单状态

    private List<InvSendOrderItem> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Double getGoodsFee() {
        return goodsFee;
    }

    public void setGoodsFee(Double goodsFee) {
        this.goodsFee = goodsFee;
    }

//    public Double getTransFee() {
//        return transFee;
//    }
//
//    public void setTransFee(Double transFee) {
//        this.transFee = transFee;
//    }

    public Double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Double totalFee) {
        this.totalFee = totalFee;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Double totalCount) {
        this.totalCount = totalCount;
    }

    public List<InvSendOrderItem> getItems() {
        return items;
    }

    public void setItems(List<InvSendOrderItem> items) {
        this.items = items;
    }
}
