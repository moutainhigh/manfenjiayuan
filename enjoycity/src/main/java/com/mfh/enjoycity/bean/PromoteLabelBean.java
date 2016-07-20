package com.mfh.enjoycity.bean;

/**
 * Created by Administrator on 2015/6/9.
 */
public class PromoteLabelBean implements java.io.Serializable {
    private Long proId;
    private Long label;

    public Long getProId() {
        return proId;
    }

    public void setProId(Long proId) {
        this.proId = proId;
    }

    public Long getLabel() {
        return label;
    }

    public void setLabel(Long label) {
        this.label = label;
    }
}
