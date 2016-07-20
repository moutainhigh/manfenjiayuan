package com.mfh.litecashier.bean;

import java.util.List;

/**
 * 用户信息
 * Created by Administrator on 2015/5/14.
 *
 */
public class HumanCompany implements java.io.Serializable{
    private List<HumanCompanyOption> options;

    public HumanCompany(){
    }

    public List<HumanCompanyOption> getOptions() {
        return options;
    }

    public void setOptions(List<HumanCompanyOption> options) {
        this.options = options;
    }
}
