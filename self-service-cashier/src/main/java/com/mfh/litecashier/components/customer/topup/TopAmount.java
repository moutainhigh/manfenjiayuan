package com.mfh.litecashier.components.customer.topup;

import com.manfenjiayuan.business.bean.wrapper.SelectionItem;

/**
 * Created by bingshanguxue on 24/10/2016.
 */

public class TopAmount extends SelectionItem {
    private Double original;
    private Double current;
    private boolean editabled;//可编辑

    public TopAmount(Double amount, boolean isSelected) {
        this.current = amount;
        this.setSelected(isSelected);
    }

    public TopAmount(Double original, Double current, boolean editabled, boolean isSelected) {
        this.original = original;
        this.current = current;
        this.editabled = editabled;
        this.setSelected(isSelected);
    }


    public Double getOriginal() {
        return original;
    }

    public void setOriginal(Double original) {
        this.original = original;
    }

    public Double getCurrent() {
        return current;
    }

    public void setCurrent(Double current) {
        this.current = current;
    }

    public boolean isEditabled() {
        return editabled;
    }

    public void setEditabled(boolean editabled) {
        this.editabled = editabled;
    }
}
