package com.manfenjiayuan.business.bean.wrapper;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 14/05/2017.
 */

public class SelectionItem implements Serializable {
    private boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
