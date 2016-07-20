package com.manfenjiayuan.pda_wholesaler.bean;

/**
 * 出库－－快递
 * Created by Administrator on 2015/5/14.
 *
 */
public class StockOutItem implements java.io.Serializable{
    private Long goodsId;//订单编号
    private String barcode;//条码
    private String humanPhone;//
    private String receiveName;// 收件人
    private String receivePhone;//收件人手机号
    private String transportName;//物流公司
    private Long itemType;//1-洗衣包裹 2-快递包裹 3-商城包裹 6-皮具包裹
    private String itemTypeName;//包裹类型
    private String createdDate;//入库时间
    private Integer paystatus;//支付状态：1-已支付；0-－未支付
    private Long stockId;//仓库编号
    private String items;//内部明细ID
    private Long transportId;//物流公司ID
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

    public String getReceiveName() {
        return receiveName;
    }

    public void setReceiveName(String receiveName) {
        this.receiveName = receiveName;
    }

    public String getReceivePhone() {
        return receivePhone;
    }

    public void setReceivePhone(String receivePhone) {
        this.receivePhone = receivePhone;
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
