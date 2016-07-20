package com.mfh.enjoycity.adapter;

import com.mfh.enjoycity.database.HistorySearchEntity;

import java.util.List;

/**
 * 搜索商品
 * Created by NAT.ZZN on 15/8/11.
 */
public class SearchProductBean {
    private String shopId;
    private String shopName;

    private List<String> hotData;
    private List<HistorySearchEntity> historyData;

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public List<String> getHotData() {
        return hotData;
    }

    public void setHotData(List<String> hotData) {
        this.hotData = hotData;
    }

    public List<HistorySearchEntity> getHistoryData() {
        return historyData;
    }

    public void setHistoryData(List<HistorySearchEntity> historyData) {
        this.historyData = historyData;
    }

    public int getHotCount(){
        return hotData == null ? 0 : 1;
    }

    public int getHistoryCount(){
        return historyData == null ? 0 : 1;
    }

}
