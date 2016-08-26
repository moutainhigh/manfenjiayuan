package com.mfh.litecashier.bean.wrapper;

import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;

/**
 * Created by bingshanguxue on 8/15/16.
 */
public class FrontCategoryGoods extends ChainGoodsSku {
    private boolean isSelected = false;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
