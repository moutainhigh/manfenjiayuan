package com.mfh.litecashier.bean.wrapper;


import java.io.Serializable;

/**
 *  生鲜预定商品明细
 *  <p>
 *      <code>{
 "imgUrl":"http://chunchunimage.b0.upaiyun.com/product/1949.JPG!small",
 "unit":"",
 "priceType":0,
 "buyUnit":"",
 "buyPriceType":0,
 "orderId":"347",
 "proSkuId":"1214",
 "chainSkuId":"39808",
 "providerId":"130222",
 "isPrivate":0,
 "productName":"澳大利亚禾富酒园南澳红牌设拉子加本力苏维翁干红葡萄酒",
 "unitSpec":null,
 "singleCount":null,
 "packCount":null,
 "totalCount":0.0,
 "askTotalCount":1.0,
 "price":128.0,
 "amount":128.0,
 "receiveCount":null,
 "receiveAmount":null,
 "barcode":"9312088451898",
 "tenantId":"135266",
 "id":"6198",
 "createdBy":"",
 "createdDate":null,
 "updatedBy":"",
 "updatedDate":null
 }</code>
 *  </p>
 * Created by bingshanguxue on 15/9/22.
 */
public class FreshScheduleGoods implements Serializable {
    private Long orderId;//订单编号
    private Long id;//product id
    private Long proSkuId;//
    private Long chainSkuId;//
    private String productName;//商品名称
    private Double receiveCount;//已签收数量
    private Double price;//价格
    private Double amount;//总价
    private String unit;//单位
    private Integer priceType;
    private Double totalCount;//采购数量
    private String buyUnit;//单位
    private Integer buyPriceType;
    private Double askTotalCount;//
    private String barcode;//条码
    private String imgUrl;//图片
    private Long providerId;//供应商编号
    private Integer isPrivate;//（0：不是 1：是）

    private Double quantityCheck;//打印数量


    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getTotalCount() {
        if (totalCount == null){
            return 0D;
        }
        return totalCount;
    }

    public void setTotalCount(Double totalCount) {
        this.totalCount = totalCount;
    }

    public Double getPrice() {
        if (price == null){
            return 0D;
        }
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getAmount() {
        if (amount == null){
            return 0D;
        }
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }


    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getPriceType() {
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public String getBuyUnit() {
        return buyUnit;
    }

    public void setBuyUnit(String buyUnit) {
        this.buyUnit = buyUnit;
    }

    public Integer getBuyPriceType() {
        return buyPriceType;
    }

    public void setBuyPriceType(Integer buyPriceType) {
        this.buyPriceType = buyPriceType;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Long getChainSkuId() {
        return chainSkuId;
    }

    public void setChainSkuId(Long chainSkuId) {
        this.chainSkuId = chainSkuId;
    }

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public Double getReceiveCount() {
        if (receiveCount == null){
            return 0D;
        }
        return receiveCount;
    }

    public void setReceiveCount(Double receiveCount) {
        this.receiveCount = receiveCount;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public Integer getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Integer isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Double getAskTotalCount() {
        return askTotalCount;
    }

    public void setAskTotalCount(Double askTotalCount) {
        this.askTotalCount = askTotalCount;
    }

    public Double getQuantityCheck() {
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }
}
