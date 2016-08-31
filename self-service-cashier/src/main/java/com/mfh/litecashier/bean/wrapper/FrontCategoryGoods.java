package com.mfh.litecashier.bean.wrapper;

import com.mfh.framework.api.anon.PubSkus;

/**
 * Created by bingshanguxue on 8/15/16.
 */
public class FrontCategoryGoods extends PubSkus {
    private boolean isSelected = false;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
