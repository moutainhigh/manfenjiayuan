package com.mfh.litecashier.bean;

/**
 * 出库－－快递批次明细
 * Created by bingshanguxue on 2015/5/14.
 *
 */
public class StockOutItem implements java.io.Serializable{
//    {
//        "barcode": "q343452435rrr",
//            "curStock": null,
//            "stockName": null,
//            "receiveHumanId": null,
//            "itemType": 2,
//            "items": null,
//            "secret": null,
//            "humanId": 94182,
//            "humanPhone": "15057196735",
//            "humanName": "张永智",
//            "receivePhone": null,
//            "receiveName": null,
//            "transportName": "申通快递",
//            "itemTypeName": "快递包裹",
//            "paystatus": 1,
//            "itemCount": 1,
//            "orderItems": null,
//            "itemId": 777760,
//            "stockId": 1217,
//            "goodsId": 778765,
//            "stockType": 0,
//            "operType": 0,
//            "transportId": 130001,
//            "transHumanId": 94182,
//            "takenType": 1,
//            "status": 3,
//            "msgid": "100_673236",
//            "remark": null,
//            "flowNum": 1,
//            "mustsms": 0,
//            "needmsg": 0,
//            "sendmsg": 1,
//            "batchId": 70328,
//            "id": 1526429,
//            "createdBy": "",
//            "createdDate": "2015-12-02 22:02:59",
//            "updatedBy": "132079",
//            "updatedDate": "2015-12-13 20:10:15"
//    }
    private Long goodsId;//订单编号
    private String barcode;//面单号
    private String humanName;// 收件人
    private String humanPhone;//收件人手机号
    private String transportName;//物流公司
    private Long itemType;//1-洗衣包裹 2-快递包裹 3-商城包裹 6-皮具包裹
    private String itemTypeName;//包裹类型
    private String createdDate;//入库时间
    private Integer paystatus;//支付状态：1-已支付；0-－未支付
    private Long stockId;//仓库编号
    private String items;//内部明细ID
    private Long transportId;
    private Long transHumanId;//物流承担者人或车辆Id
    private Integer status;//

    //FOR UI
    private boolean bSelected;//是否被选中


    public StockOutItem(){
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getHumanPhone() {
        return humanPhone;
    }

    public void setHumanPhone(String humanPhone) {
        this.humanPhone = humanPhone;
    }


    public String getHumanName() {
        return humanName;
    }

    public void setHumanName(String humanName) {
        this.humanName = humanName;
    }



    public String getTransportName() {
        return transportName;
    }

    public void setTransportName(String transportName) {
        this.transportName = transportName;
    }

    public Long getItemType() {
        return itemType;
    }

    public void setItemType(Long itemType) {
        this.itemType = itemType;
    }

    public String getItemTypeName() {
        return itemTypeName;
    }

    public void setItemTypeName(String itemTypeName) {
        this.itemTypeName = itemTypeName;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getPaystatus() {
        return paystatus;
    }

    public void setPaystatus(Integer paystatus) {
        this.paystatus = paystatus;
    }

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public Long getTransHumanId() {
        return transHumanId;
    }

    public void setTransHumanId(Long transHumanId) {
        this.transHumanId = transHumanId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    public boolean isbSelected() {
        return bSelected;
    }

    public void setbSelected(boolean bSelected) {
        this.bSelected = bSelected;
    }
}
