package com.manfenjiayuan.business.mode;

import com.manfenjiayuan.business.bean.ChainGoodsSku;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.impl.MerchandiseApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * 库存商品：库存成本，批次流水，库存调拨
 * Created by bingshanguxue on 16/3/17.
 */
public class ChainGoodsSkuMode implements IChainGoodsSkuMode<ChainGoodsSku> {

    @Override
    public void loadLaundryGoods(PageInfo pageInfo, Long frontCategoryId, Long netId,
                                 final OnPageModeListener<ChainGoodsSku> listener) {

        if (listener != null) {
            listener.onProcess();
        }

        //检查参数：
        if (frontCategoryId == null) {
            if (listener != null) {
                listener.onError("缺少必要参数frontCategoryId");
            }
            return;
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ChainGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ChainGoodsSku> rs) {
                //此处在主线程中执行。
                List<ChainGoodsSku> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ChainGoodsSku> wrapper : rs.getRowDatas()) {
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
                ZLogger.d("加载洗衣类目商品失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ChainGoodsSku.class, MfhApplication.getAppContext());

        MerchandiseApiImpl.findPublicChainGoodsSku(frontCategoryId, netId, pageInfo, queryRsCallBack);
    }


    @Override
    public void loadCompanyChainSkuGoods(PageInfo pageInfo, Long frontCategoryId, Long companyId,
                                         String barcode,
                                 final OnPageModeListener<ChainGoodsSku> listener) {

        if (listener != null) {
            listener.onProcess();
        }

        //检查参数：
        if (frontCategoryId == null) {
            if (listener != null) {
                listener.onError("缺少必要参数frontCategoryId");
            }
            return;
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ChainGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ChainGoodsSku> rs) {
                //此处在主线程中执行。
                List<ChainGoodsSku> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ChainGoodsSku> wrapper : rs.getRowDatas()) {
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
                ZLogger.d("加载洗衣类目商品失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ChainGoodsSku.class, MfhApplication.getAppContext());

        MerchandiseApiImpl.findPublicChainGoodsSku2(frontCategoryId, companyId, barcode, pageInfo,
                queryRsCallBack);
    }


    @Override
    public void findTenantSku(PageInfo pageInfo, Long companyId, String barcode,
                                  final OnPageModeListener<ChainGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        //检查参数：
//        if (companyId == null) {
//            if (listener != null) {
//                listener.onError("缺少必要参数companyId");
//            }
//            return;
//        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ChainGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ChainGoodsSku> rs) {
                //此处在主线程中执行。
                List<ChainGoodsSku> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ChainGoodsSku> wrapper : rs.getRowDatas()) {
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
                ZLogger.d("加载采购商品失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ChainGoodsSku.class, MfhApplication.getAppContext());

        MerchandiseApiImpl.findTenantSku(barcode, companyId, pageInfo, queryRsCallBack);
    }

    public void getTenantSkuMust(Long tenantId, String barcode,
                              final OnModeListener<ChainGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        //检查参数：
//        if (companyId == null) {
//            if (listener != null) {
//                listener.onError("缺少必要参数companyId");
//            }
//            return;
//        }


        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<ChainGoodsSku,
                NetProcessor.Processor<ChainGoodsSku>>(
                new NetProcessor.Processor<ChainGoodsSku>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //查询失败
                        ZLogger.d("加载商品失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"新增成功!","version":"1","data":{"val":"463"}}
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}
//                        animProgress.setVisibility(View.GONE);
                        ChainGoodsSku chainGoodsSku = null;
                        if (rspData != null) {
                            RspBean<ChainGoodsSku> retValue = (RspBean<ChainGoodsSku>) rspData;
                            chainGoodsSku = retValue.getValue();
                        }
                        if (listener != null) {
                            listener.onSuccess(chainGoodsSku);
                        }
                    }
                }
                , ChainGoodsSku.class
                , MfhApplication.getAppContext()) {
        };

        MerchandiseApiImpl.getTenantSkuMust(barcode, tenantId, responseCallback);
    }
}
