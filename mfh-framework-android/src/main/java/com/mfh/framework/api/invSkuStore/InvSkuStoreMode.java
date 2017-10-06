package com.mfh.framework.api.invSkuStore;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invSendOrder.InvSendOrderItemBrief;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.ExceptionHandle;
import com.mfh.framework.rxapi.httpmgr.InvSkuStoreHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.rxapi.subscriber.MSubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;

/**
 * Created by bingshanguxue on 7/28/16.
 */
public class InvSkuStoreMode {
    /**
     * 智能订货
     *
     * @param chainCompanyId
     * @param listener
     */
    public void autoAskSendOrder(Long chainCompanyId,
                                 final OnModeListener<InvSendOrderItemBrief> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        options.put("chainCompanyId", String.valueOf(chainCompanyId));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSkuStoreHttpManager.getInstance().autoAskSendOrder(options,
                new MSubscriber<InvSendOrderItemBrief>() {
//                    @Override
//                    public void onError(Throwable e) {
//
//                        if (listener != null) {
//                            listener.onError(e.getMessage());
//                        }
//                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable responeThrowable) {
                        ZLogger.d("智能订货失败：" + responeThrowable.toString());

                        if (listener != null) {
                            listener.onError(responeThrowable.getMessage());
                        }
                    }

                    @Override
                    public void onNext(InvSendOrderItemBrief invSendOrderItemBrief) {
                        if (listener != null) {
                            listener.onSuccess(invSendOrderItemBrief);
                        }
                    }

                });
    }

    public void getByBarcodeMust(String barcode, final OnModeListener<InvSkuGoods> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        //检查参数：
        if (StringUtils.isEmpty(barcode)) {
            if (listener != null) {
                listener.onError("条码不能为空");
            }
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("barcode", barcode);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSkuStoreHttpManager.getInstance().getByBarcodeMust(options,
                new MSubscriber<InvSkuGoods>() {

//                    @Override
//                    public void onError(Throwable e) {
//                        ZLogger.d("查询商品失败：" + e.toString());
//
//                        if (listener != null) {
//                            listener.onError(e.getMessage());
//                        }
//                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable throwable) {
                        ZLogger.d("查询商品失败：" + throwable.toString());

                        if (listener != null) {
                            listener.onError(throwable.getMessage());
                        }
                    }

                    @Override
                    public void onNext(InvSkuGoods invSkuGoods) {
                        if (listener != null) {
                            listener.onSuccess(invSkuGoods);
                        }
                    }
                });
    }

    /**
     * @param barcode 商品条码或者商品名称
     */
    public void getBeanByBizKeys(String barcode, final OnModeListener<InvSkuBizBean> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        //检查参数：
//        if (StringUtils.isEmpty(barcode)) {
//            if (listener != null) {
//                listener.onError("条码不能为空");
//            }
//            return;
//        }

        Map<String, String> options = new HashMap<>();
        if (!StringUtils.isEmpty(barcode)) {
//            if (RegularUtils.matcher(barcode, RegularUtils.PATTERN_BARCODE)) {
//                options.put("barcode", barcode);
//            } else {
//                options.put("skuName", barcode);
//            }
            options.put("barcode", barcode);

        }

        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSkuStoreHttpManager.getInstance().getBeanByBizKeys(options,
                new MSubscriber<InvSkuBizBean>() {
//                    @Override
//                    public void onError(Throwable e) {
//                        ZLogger.d("查询商品失败：" + e.toString());
//
//                        if (listener != null) {
//                            listener.onError(e.getMessage());
//                        }
//                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
                        ZLogger.d("查询商品失败：" + e.toString());

                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(InvSkuBizBean invSkuBizBean) {
                        if (listener != null) {
                            listener.onSuccess(invSkuBizBean);
                        }
                    }
                });
    }

    /**
     * @param skuName 商品名称
     */
    public void listBeans(String skuName, PageInfo pageInfo, final OnPageModeListener<InvSkuBizBean> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        if (!StringUtils.isEmpty(skuName)) {
            options.put("skuName", skuName);
        }

        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSkuStoreHttpManager.getInstance().listBeans(options,
                new MQuerySubscriber<InvSkuBizBean>(pageInfo) {

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.d("查询商品失败：" + e.toString());

                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<InvSkuBizBean> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }

                });
    }
}
