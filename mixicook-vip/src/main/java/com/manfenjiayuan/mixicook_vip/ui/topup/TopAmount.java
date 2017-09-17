package com.manfenjiayuan.mixicook_vip.ui.topup;

import com.manfenjiayuan.business.bean.wrapper.SelectionItem;

/**
 * Created by bingshanguxue on 24/10/2016.
 */

public class TopAmount extends SelectionItem {
    private Double amount;

    public TopAmount(Double amount, boolean isSelected) {
        this.amount = amount;
        this.setSelected(isSelected);
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
