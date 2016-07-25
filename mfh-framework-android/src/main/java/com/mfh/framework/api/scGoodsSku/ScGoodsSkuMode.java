package com.mfh.framework.api.scGoodsSku;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品
 * Created by bingshanguxue on 16/3/17.
 */
public class ScGoodsSkuMode {
    /**
     * 查询商品
     * */
    public void findGoodsList(String barcode, PageInfo pageInfo, final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (StringUtils.isEmpty(barcode)){
            if (listener != null) {
                listener.onError("缺少barcode参数");
            }
            return;
        }


        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                //此处在主线程中执行。
                List<ScGoodsSku> scGoodsSkus = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ScGoodsSku> wrapper : rs.getRowDatas()) {
                        scGoodsSkus.add(wrapper.getBean());
                    }
                }
                if (listener != null) {
                    listener.onSuccess(pageInfo, scGoodsSkus);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载类目商品失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ScGoodsSku.class, MfhApplication.getAppContext());

        ScGoodsSkuApiImpl.findGoodsList(barcode, pageInfo, queryRsCallBack);
    }

    /**
     * 查询商品
     * */
    public void getGoodsByBarCode(String barcode,  final OnModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (StringUtils.isEmpty(barcode)){
            if (listener != null) {
                listener.onError("缺少barcode参数");
            }
            return;
        }

        NetCallBack.NetTaskCallBack queryResCallback = new NetCallBack.NetTaskCallBack<ScGoodsSku,
                NetProcessor.Processor<ScGoodsSku>>(
                new NetProcessor.Processor<ScGoodsSku>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        // {"code":"0","msg":"查询成功!","version":"1","data":null}
                        ScGoodsSku goodsSku = null;
                        if (rspData != null){
                            RspBean<ScGoodsSku> retValue = (RspBean<ScGoodsSku>) rspData;
                            goodsSku = retValue.getValue();
                        }
                        if (listener != null) {
                            listener.onSuccess(goodsSku);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                    ZLogger.df("查询失败: " + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , ScGoodsSku.class
                , MfhApplication.getAppContext()) {
        };

        ScGoodsSkuApiImpl.getGoodsByBarCode(barcode, queryResCallback);
    }

}
