package com.manfenjiayuan.mixicook_vip.ui.order;

import com.mfh.framework.api.pmcstock.MarketRules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingshanguxue on 14/10/2016.
 */

public class MarketRuleBrief implements Serializable{
    private MarketRules marketRules;
    List<Long> ruleIds = new ArrayList<>();//选中
    List<Long> couponIds = new ArrayList<>();//选中

    public MarketRules getMarketRules() {
        return marketRules;
    }

    public void setMarketRules(MarketRules marketRules) {
        this.marketRules = marketRules;
    }

    public List<Long> getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(List<Long> ruleIds) {
        this.ruleIds = ruleIds;
    }

    public List<Long> getCouponIds() {
        return couponIds;
    }

    public void setCouponIds(List<Long> couponIds) {
        this.couponIds = couponIds;
    }
}
