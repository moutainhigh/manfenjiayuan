package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import com.mfh.framework.api.anon.sc.storeRack.StoreRackCardItem;

import java.io.Serializable;
import java.util.List;

import me.drakeet.multitype.Item;

/**
 * 促销卡片
 * Created by bingshanguxue on 10/10/2016.
 */
public class Card6 implements Serializable, Item {
    private Integer type;//卡片类型不同样式布局
    private Long netId;//指定该卡片是在哪个网点购买
    private String netName;//网点名称
    private List<StoreRackCardItem> items;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getNetId() {
        return netId;
    }

    public void setNetId(Long netId) {
        this.netId = netId;
    }

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
    }

    public void setItems(List<StoreRackCardItem> items) {
        this.items = items;
    }

    public List<StoreRackCardItem> getItems() {
        return items;
    }
}
