package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.api.invSendOrder.InvSendOrderItemBrief;
import com.mfh.framework.api.invSkuStore.InvSkuBizBean;
import com.mfh.framework.api.invSkuStore.InvSkuGoods;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.func.MResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class InvSkuStoreHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final InvSkuStoreHttpManager INSTANCE = new InvSkuStoreHttpManager();
    }

    //获取单例
    public static InvSkuStoreHttpManager getInstance() {
        return InvSkuStoreHttpManager.SingletonHolder.INSTANCE;
    }

    private interface InvSkuStoreService{
        /**
         * 当前登录人员，把平台商品导入到本店仓储中
         * /invSkuStore/importFromCenterSkus?proSkuIds=111,222
         */
        @GET("invSkuStore/importFromCenterSkus")
        Observable<MResponse<String>> importFromCenterSkus(@QueryMap Map<String, String> options);
        /**
         * 更新商品信息－－
         * /invSkuStore/update?jsonStr={"id":...,"costPrice"...,"quantity":...,"lowerLimit":...., "tenantId":....},
         * 注意：这里的tenantId就是店的租户，不是CATEGORY_TENANT_ID(130222)
         */
        @GET("invSkuStore/update")
        Observable<MResponse<String>> update(@QueryMap Map<String, String> options);
        @GET("invSkuStore/updateStatus")
        Observable<MResponse<String>> updateStatus(@QueryMap Map<String, String> options);
        /**
         * 根据条码查询库存商品,如果库存中没有则从租户档案中自动建立库存。门店和批发都适用
         * /invSkuStore/getByBarcodeMust?barcode=998800000000
         */
        @GET("invSkuStore/getByBarcodeMust")
        Observable<MResponse<InvSkuGoods>> getByBarcodeMust(@QueryMap Map<String, String> options);

        /**
         * /invSkuStore/listBeans?skuName= 根据商品名称返回可能存在的多个商品详情信息
         * 根据条码获取详情bean，包括会员价、销量信息等
         */
        @GET("invSkuStore/listBeans")
        Observable<MResponse<MRspQuery<InvSkuBizBean>>> listBeans(@QueryMap Map<String, String> options);

        /**
         * /invSkuStore/getBeanByBizKeys?barcode=
         * 根据条码获取详情bean，包括会员价、销量信息等
         */
        @GET("invSkuStore/getBeanByBizKeys")
        Observable<MResponse<InvSkuBizBean>> getBeanByBizKeys(@QueryMap Map<String, String> options);


        /**
         *
         * 当前登录网点的操作人员，通过指定一个批发商，自动生成配送单
         * /invSkuStore/autoAskSendOrder?chainCompanyId=134651
         */
        @GET("invSkuStore/autoAskSendOrder")
        Observable<MResponse<InvSendOrderItemBrief>> autoAskSendOrder(@QueryMap Map<String, String> options);
    }

    public void importFromCenterSkus(Map<String, String> options, Subscriber<String> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.importFromCenterSkus(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void update(Map<String, String> options, Subscriber<String> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.update(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void updateStatus(Map<String, String> options, Subscriber<String> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.updateStatus(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void getByBarcodeMust(Map<String, String> options, Subscriber<InvSkuGoods> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.getByBarcodeMust(options)
                .map(new MResponseFunc<InvSkuGoods>());
        toSubscribe(observable, subscriber);
    }
    public void listBeans(Map<String, String> options, MQuerySubscriber<InvSkuBizBean> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.listBeans(options)
                .map(new MQueryResponseFunc<InvSkuBizBean>());
        toSubscribe(observable, subscriber);
    }
    public void getBeanByBizKeys(Map<String, String> options, Subscriber<InvSkuBizBean> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.getBeanByBizKeys(options)
                .map(new MResponseFunc<InvSkuBizBean>());
        toSubscribe(observable, subscriber);
    }
    public void autoAskSendOrder(Map<String, String> options, Subscriber<InvSendOrderItemBrief> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.autoAskSendOrder(options)
                .map(new MResponseFunc<InvSendOrderItemBrief>());
        toSubscribe(observable, subscriber);
    }

}
