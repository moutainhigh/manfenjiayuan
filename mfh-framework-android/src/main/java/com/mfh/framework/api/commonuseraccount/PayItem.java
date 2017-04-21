package com.mfh.framework.api.commonuseraccount;

import java.io.Serializable;
import java.util.Map;

/**
 * 会员订单支付补充信息，详细告知每个商品明细是否有适用的促销规则
 * <pre><code>
 *     {
 *          "skuId":1289,
 *          "goodsId":null,
 *          "bcount":1.0,
 *          "factAmount":25.0,
 *          "ruleAmount":{
 *              "161":3.74976,
 *              "162":3.74976
 *          },
 *          "saleAmount":21.250239999999998
 *      }
 * </code></pre>
 *
 * <ol>
 *     字段名字解释
 *     <li>{@link #saleAmount} factAmount 是商品本次销售原价金额(例如抹零)</li>
 *     <li>{@link #saleAmount} saleAmount 则是减去累计优惠(会员优惠)后剩下的金额，这个金额也就是实际的销售额</li>
 *     <li>{@link #ruleAmountMap} ruleAmountMap，key是规则编号，value是该规则作用于该订单商品明细上的优惠金额，可以有多条规则</li>
 * </ol>
 *
 * Created by bingshanguxue on 18/04/2017.
 */

public class PayItem implements Serializable {
    private Long skuId;
    private Long goodsId;
    private Double bcount;
    private Double factAmount;
    private Double saleAmount;
    private Map<String, Double> ruleAmountMap;

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Double getBcount() {
        return bcount;
    }

    public void setBcount(Double bcount) {
        this.bcount = bcount;
    }

    public Double getFactAmount() {
        return factAmount;
    }

    public void setFactAmount(Double factAmount) {
        this.factAmount = factAmount;
    }

    public Double getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(Double saleAmount) {
        this.saleAmount = saleAmount;
    }

    public Map<String, Double> getRuleAmountMap() {
        return ruleAmountMap;
    }

    public void setRuleAmountMap(Map<String, Double> ruleAmountMap) {
        this.ruleAmountMap = ruleAmountMap;
    }

}
