package com.manfenjiayuan.mixicook_vip.model;

import com.mfh.framework.api.pmcstock.MarketRules;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bingshanguxue on 15/9/30.
 */
public class OrderMarketRules implements Serializable {
    //前台自定义字段
    private Double finalAmount;//订单调价后金额

    //后台返回数据字段
    private List<MarketRules> results;

    public Double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public List<MarketRules> getResults() {
        return results;
    }

    public void setResults(List<MarketRules> results) {
        this.results = results;
    }
}
