package com.mfh.litecashier.bean;

import java.util.List;

/**
 * 快递公司
 * Created by Administrator on 2015/5/14.
 *
 */
public class ReceiveOrderCompanyWrapper implements java.io.Serializable{
    private List<HumanCompanyOption> options;

    public List<HumanCompanyOption> getOptions() {
        return options;
    }

    public void setOptions(List<HumanCompanyOption> options) {
        this.options = options;
    }
}
