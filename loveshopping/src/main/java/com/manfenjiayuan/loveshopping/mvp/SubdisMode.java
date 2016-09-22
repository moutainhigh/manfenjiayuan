package com.manfenjiayuan.loveshopping.mvp;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Subdis;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.api.ScApi;

import net.tsz.afinal.http.AjaxParams;

import java.util.ArrayList;
import java.util.List;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/17.
 */
public class SubdisMode{

    public void listSubdis(PageInfo pageInfo, String cityId, String subdisName,
                           final OnPageModeListener<Subdis> listener) {
        if (listener != null) {
            listener.onProcess();
        }
        AjaxParams params = new AjaxParams();
        params.put("cityID", cityId);
        params.put("subdisName", subdisName);
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        //回调
        NetCallBack.QueryRsCallBack responseCallback = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<Subdis>(new PageInfo(1, 100)) {
                    //                处理查询结果集，子类必须继承
                    @Override
                    public void processQueryResult(RspQueryResult<Subdis> rs) {//此处在主线程中执行。
                        //此处在主线程中执行。
                        List<Subdis> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<Subdis> wrapper : rs.getRowDatas()) {
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
                        ZLogger.d("加载网点失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , Subdis.class
                , MfhApplication.getAppContext());

        NetFactory.getHttp().post(ScApi.URL_SUBDIST_LIST, params, responseCallback);

//        ScApi.listSubdis(cityId, subdisName, responseCallback);
    }
}
