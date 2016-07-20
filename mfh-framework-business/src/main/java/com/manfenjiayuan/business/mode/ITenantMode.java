package com.manfenjiayuan.business.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

/**
 * 门店
 * Created by bingshanguxue on 16/3/17.
 */
public interface ITenantMode<D> {

    /**
     * 获取门店数据
     * */
    void getTenants(PageInfo pageInfo, String nameLike, Integer ability, OnPageModeListener<D> listener);
}
