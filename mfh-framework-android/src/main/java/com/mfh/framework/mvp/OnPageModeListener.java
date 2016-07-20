package com.mfh.framework.mvp;

import com.mfh.comn.bean.PageInfo;

import java.util.List;

/**
 * 
 * Created by bingshanguxue on 16/3/17.
 */
public interface OnPageModeListener<M> {
    void onProcess();
    void onSuccess(PageInfo pageInfo, List<M> dataList);
    void onError(String errorMsg);
}
