package com.bingshanguxue.cashier.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.abs.MfhEntity;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.core.utils.StringUtils;

/**
 * POS--销售订单明细
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name = "tb_pos_order_item_2")
public class PosOrderItemEntity extends MfhEntity<Long> implements ILongId {
    /**
     * POS唯一订单号(12位字符串),流水号，可拆分成多个订单,拆分后的订单共用一个posTradeNo
     */
    private String orderBarCode;

    /**
     * 关联订单编号<br>
     * 日结单和订单流水共用一套序列
     * 关联收银流水订单{@link PosOrderEntity}/
     */
    private Long orderId;

    private Long goodsId;   //商品主键
    private Long productId; //产品编号
    private Long proSkuId; //商品sku编号
    private Long providerId;//商品供应商编号

    private String barcode; //商品条形码,最小商品库存单元的条形码
    private String name; // 商品名称
    private String skuName;
    private String shortName;
    private String unit; // 单位，如箱、瓶
    private int priceType = PriceType.PIECE;//价格类型0-计件 1-计重
    private Double bcount = 0D; //商品数量,quantity
    /**
     * 档案价格(商品零售价格)。
     */
    private Double costPrice = 0D;
    /**
     * 会员价
     */
    private Double customerPrice = 0D;
    private Double amount = 0D; //档案价格总价.按零售价计算得出
    /**订单价（店收银默认取商品的档案价格，可以手动修改，只影响当前订单）*/
//    private Double adjustPrice = 0D;
//    private Double adjustAmount = 0D; //按订单价计算的总金额
    /**
     * 成交价（根据订单商品，匹配后台营销策略，获取商品的最终成交价格）
     */
    private Double finalPrice = 0D;
    private Double finalAmount = 0D; //总价.按成交价计算得出
    private Double finalCustomerPrice = 0D;

    // 2016-05-19 新增商品类目类型字段，支持按类目进行账务清分
    @Deprecated
    private Integer cateType = CateApi.BACKEND_CATE_BTYPE_NORMAL;
    //2016-08-01，新增产品线编号清分
    private Integer prodLineId = 0;//产品线编号,产品线的商品默认都归到0，相当于原来的标超
    private Integer needWait = 0;
    //2017-04-18 营业额统计方式调整
    /**
     * 该条订单明细流水具体的会员折扣规则优惠情况，可能会有多条会员折扣规则适用，其中key是规则id，value是该规则的产生的优惠金额
     */
    private String ruleAmountMap;
    private Double vipAmount = 0D;//会员优惠金额

    public String getOrderBarCode() {
        return orderBarCode;
    }

    public void setOrderBarCode(String orderBarCode) {
        this.orderBarCode = orderBarCode;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getBarcode() {
        if (barcode == null) {
            return "";
        }
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public Double getBcount() {
        if (bcount == null) {
            return 0D;
        }
        return bcount;
    }

    public void setBcount(Double bcount) {
        this.bcount = bcount;
    }

    public Double getCostPrice() {
        if (costPrice == null) {
            return 0D;
        }
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public Double getCustomerPrice() {
        if (customerPrice == null) {
            return 0D;
        }
        return customerPrice;
    }

    public void setCustomerPrice(Double customerPrice) {
        this.customerPrice = customerPrice;
    }

    public Double getFinalCustomerPrice() {
        if (finalCustomerPrice == null) {
            return 0D;
        }
        return finalCustomerPrice;
    }

    public void setFinalCustomerPrice(Double finalCustomerPrice) {
        this.finalCustomerPrice = finalCustomerPrice;
    }

    public String getName() {
        if (name == null) {
            return "";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSkuName() {
        //2016-12-25 兼容旧版本已经下载过商品档案的情况，skuName和shortName没有数据的问题
        if (StringUtils.isEmpty(skuName)) {
            return name;
        }
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getUnit() {
        if (unit == null) {
            return "";
        }
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }


    public Double getFinalPrice() {
        if (finalPrice == null) {
            return 0D;
        }
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Double getFinalAmount() {
        if (finalAmount == null) {
            return 0D;
        }
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public int getPriceType() {
        return priceType;
    }

    public void setPriceType(int priceType) {
        this.priceType = priceType;
    }

    public Integer getCateType() {
        return cateType;
    }

    public void setCateType(Integer cateType) {
        this.cateType = cateType;
    }

    public Integer getProdLineId() {
        if (prodLineId == null) {
            return 0;
        }
        return prodLineId;
    }

    public void setProdLineId(Integer prodLineId) {
        this.prodLineId = prodLineId;
    }

    public Integer getNeedWait() {
        if (needWait == null) {
            return 0;
        }
        return needWait;
    }

    public void setNeedWait(Integer needWait) {
        this.needWait = needWait;
    }

    public String getRuleAmountMap() {
        return ruleAmountMap;
    }

    public void setRuleAmountMap(String ruleAmountMap) {
        this.ruleAmountMap = ruleAmountMap;
    }

    public Double getVipAmount() {
        return vipAmount;
    }

    public void setVipAmount(Double vipAmount) {
        this.vipAmount = vipAmount;
    }
}
