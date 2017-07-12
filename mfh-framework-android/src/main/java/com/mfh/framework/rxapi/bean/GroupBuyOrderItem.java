package com.mfh.framework.rxapi.bean;

import com.mfh.framework.api.abs.MfhEntity;

import me.drakeet.multitype.Item;

/**
 * 团购活动订单明细
 * Created by bingshanguxue on 03/07/2017.
 */

public class GroupBuyOrderItem extends MfhEntity<Long> implements Item {
    //商品id,对应商城和pos订单来说就是biz_inv_sku_store的主键；对于洗衣等服务类订单来说目前是tenantSkuId，即sc_chain_goods_sku的主键。
    private Long goodsId;
    //订单ID
    protected Long orderId ;
    //物品ID，spuId或service的产品id等
    protected Long productId ;
    //物品名称
    private String productName;
    //件数，对于服务型订单一个订单项一般就是针对一件物品；而商品型可以多个
    private Double bcount ;
    //订单计算明细价格（原价*件数），可能为null，因为还没有设置过价格
    private Double amount ;
    //该订单明细的实际原始销售价, 例如收银员手工可能有修改金额（实际原始销售价未考虑统一促销、卡券等因素，这部分未包含）
    private Double factAmount;
    //会员单价，pos机上传或后台动态再计算
    private Double customerPrice;
    //实际销售金额，如果当前是会员，则是customerPrice，如果不是则是factAmount；新增
    private Double saleAmount;
    //商品单价（原价），可能为null或0，因为还没有设置过价格
    private Double price;
    //备注
    private String remark;
    //产品sku编号,注意不是商品sku，和tenantId结合起来唯一
    private Long skuId;

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getBcount() {
        return bcount;
    }

    public void setBcount(Double bcount) {
        this.bcount = bcount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getFactAmount() {
        return factAmount;
    }

    public void setFactAmount(Double factAmount) {
        this.factAmount = factAmount;
    }

    public Double getCustomerPrice() {
        return customerPrice;
    }

    public void setCustomerPrice(Double customerPrice) {
        this.customerPrice = customerPrice;
    }

    public Double getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(Double saleAmount) {
        this.saleAmount = saleAmount;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
