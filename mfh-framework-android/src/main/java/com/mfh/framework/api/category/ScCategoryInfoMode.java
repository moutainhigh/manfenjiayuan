package com.mfh.framework.api.category;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public class ScCategoryInfoMode {

    public void list(int domain, int cateType, int catePosition,
                     int deep, Long tenantId, PageInfo pageInfo,
                     final OnPageModeListener<CategoryInfo> listener) {
        if (listener != null) {
            listener.onProcess();
        }
        ZLogger.d(String.format("加载类目开始:page=%d/%d", pageInfo.getPageNo(), pageInfo.getTotalPage()));
        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<CategoryInfo>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<CategoryInfo> rs) {
                        //此处在主线程中执行。
                        List<CategoryInfo> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<CategoryInfo> wrapper : rs.getRowDatas()) {
                                entityList.add(wrapper.getBean());
                            }
                        }
                        if (listener != null) {
                            listener.onSuccess(pageInfo, entityList);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("查询类目失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, CategoryInfo.class, MfhApplication.getAppContext());

        ScCategoryInfoApi.list(domain, cateType, catePosition, deep, tenantId, pageInfo, queryRsCallBack);
    }


}
