package com.mfh.litecashier.ui.fragment.goods;

/**
 * 类目商品
 * Created by bingshanguxue on 15/9/23.
 */
public class FrontCategoryGoodsEvent {
    public static final int EVENT_ID_RELOAD_DATA = 0X01;//初始化数据
    public static final int EVENT_ID_REFRESH_DATA = 0X02;//刷新数据

    private int affairId;
    private Long categoryId;
    private String categoryCode;//==categoryId

    public FrontCategoryGoodsEvent(int affairId, Long categoryId) {
        this.affairId = affairId;
        this.categoryId = categoryId;
    }

    public FrontCategoryGoodsEvent(int affairId, String categoryCode) {
        this.affairId = affairId;
        this.categoryCode = categoryCode;
    }

    public int getAffairId() {
        return affairId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }
}
