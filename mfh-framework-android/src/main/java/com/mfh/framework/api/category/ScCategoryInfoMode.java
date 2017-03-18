package com.mfh.framework.api.category;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.ScCategoryInfoHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public class ScCategoryInfoMode {

    /**pos导入商品到前台类目时，加载平台维护的POS前台类目*/
    public void list(int domain, int cateType, int catePosition,
                     int deep, Long tenantId, PageInfo pageInfo,
                     final OnPageModeListener<CategoryInfo> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();

        options.put("kind", "code");
        options.put("domain", String.valueOf(domain));
        options.put("cateType", String.valueOf(cateType));
        options.put("catePosition", String.valueOf(catePosition));
        options.put("deep", String.valueOf(deep));//层级
        options.put("parentIdNull", "1");//层级

        if (tenantId != null) {
            options.put("tenantId", String.valueOf(tenantId));
        }
        if (pageInfo != null) {
            ZLogger.d(String.format("加载类目开始:page=%d/%d", pageInfo.getPageNo(), pageInfo.getTotalPage()));
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        else{
            ZLogger.d("加载类目开始");
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        ScCategoryInfoHttpManager.getInstance().list(options,
                new MQuerySubscriber<CategoryInfo>(pageInfo) {
                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<CategoryInfo> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        ZLogger.df("查询类目失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.toString());
                        }
                    }
                });

    }


}
