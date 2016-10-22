package com.mfh.framework.api.anon.storeRack;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bingshanguxue on 09/10/2016.
 */

public class StoreRackDataInfo implements Serializable{
    private List<StoreRackCard> dataInfo;

    public List<StoreRackCard> getDataInfo() {
        return dataInfo;
    }

    public void setDataInfo(List<StoreRackCard> dataInfo) {
        this.dataInfo = dataInfo;
    }
}
