package com.manfenjiayuan.mixicook_vip.ui.topup;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 24/10/2016.
 */

public class TopAmount implements Serializable {
    private Double amount;
    private boolean isSelected;

    public TopAmount(Double amount, boolean isSelected) {
        this.amount = amount;
        this.isSelected = isSelected;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
