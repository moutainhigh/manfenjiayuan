package com.mfh.framework.api.cashier;

import com.mfh.framework.api.pmcstock.MarketRules;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bingshanguxue on 15/9/30.
 */
public class MarketRulesWrapper implements Serializable {
    //后台返回数据字段
    private List<MarketRules> results;

    public List<MarketRules> getResults() {
        return results;
    }

    public void setResults(List<MarketRules> results) {
        this.results = results;
    }
}
