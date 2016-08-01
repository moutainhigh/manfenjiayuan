package com.mfh.litecashier.ui.fragment.goods;

import com.mfh.framework.api.scGoodsSku.ScGoodsSku;

/**
 * Created by bingshanguxue on 8/1/16.
 */
public class ScGoodsSkuWrapper extends ScGoodsSku{

    private String namePinyin = "";//拼音
    private String nameSortLetter = "";//排序字段

    public String getNamePinyin() {
        return namePinyin;
    }

    public void setNamePinyin(String namePinyin) {
        this.namePinyin = namePinyin;
    }

    public String getNameSortLetter() {
        return nameSortLetter;
    }

    public void setNameSortLetter(String nameSortLetter) {
        this.nameSortLetter = nameSortLetter;
    }
}
