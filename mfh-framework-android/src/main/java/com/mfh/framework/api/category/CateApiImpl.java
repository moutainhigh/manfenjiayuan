package com.mfh.framework.api.category;

import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 5/5/16.
 */
public class CateApiImpl extends CateApi{


    /**
     * 查询类目
     * */
    public static void comnqueryCategory(int domain, int cateType, int catePosition,
                                     int deep, Long tenantId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("kind", "code");
        params.put("domain", String.valueOf(domain));
        params.put("cateType", String.valueOf(cateType));
        params.put("catePosition", String.valueOf(catePosition));
        params.put("deep", String.valueOf(deep));//层级
        if (tenantId != null){
            params.put("tenantId", String.valueOf(tenantId));
        }

        AfinalFactory.postDefault(ScCategoryInfoApi.URL_COMNQUERY, params, responseCallback);
    }

}
