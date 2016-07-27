package com.mfh.litecashier.event;

/**
 * 常用商品
 * Created by kun on 15/9/23.
 */
public class CommonlyGoodsEvent {
    public static final int EVENT_ID_RELOAD_DATA = 0X01;//初始化数据
    public static final int EVENT_ID_REFRESH_DATA = 0X02;//刷新数据
    public static final int EVENT_ID_NORMAL_STATUS = 0X03;// 正常模式
    public static final int EVENT_ID_REMOVE_STATUS = 0X04;//删除模式

    private int affairId;

    public CommonlyGoodsEvent(int affairId) {
        this.affairId = affairId;
    }

    public int getAffairId() {
        return affairId;
    }
}
