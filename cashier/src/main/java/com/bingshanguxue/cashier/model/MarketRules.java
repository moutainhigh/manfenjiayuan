package com.bingshanguxue.cashier.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bingshanguxue on 15/9/30.
 */
public class MarketRules implements Serializable {
//    {"ruleBeans":[{}, {}], "coupBeans":[{}, {}], "factor":{}}

    private List<RuleBean> ruleBeans;
    private List<CoupBean> coupBeans;

    public List<RuleBean> getRuleBeans() {
        return ruleBeans;
    }

    public void setRuleBeans(List<RuleBean> ruleBeans) {
        this.ruleBeans = ruleBeans;
    }

    public List<CoupBean> getCoupBeans() {
        return coupBeans;
    }

    public void setCoupBeans(List<CoupBean> coupBeans) {
        this.coupBeans = coupBeans;
    }
}
