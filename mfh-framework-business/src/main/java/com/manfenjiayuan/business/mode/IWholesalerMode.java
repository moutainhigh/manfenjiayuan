package com.manfenjiayuan.business.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

/**
 * 批发商
 * Created by bingshanguxue on 16/3/17.
 */
public interface IWholesalerMode<D> {

    /**
     * 获取批发商数据
     * */
    void getWholesalers(String abilityItem, PageInfo pageInfo, String shortCodeLike, OnPageModeListener<D> listener);
}
