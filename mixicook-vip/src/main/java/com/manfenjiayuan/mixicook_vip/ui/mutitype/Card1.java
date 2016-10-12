package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import java.io.Serializable;
import java.util.List;

import me.drakeet.multitype.Item;

/**
 * Created by bingshanguxue on 10/10/2016.
 */

public class Card1 implements Serializable, Item {
    private List<Card1Item> items;

    public List<Card1Item> getItems() {
        return items;
    }

    public void setItems(List<Card1Item> items) {
        this.items = items;
    }
}
