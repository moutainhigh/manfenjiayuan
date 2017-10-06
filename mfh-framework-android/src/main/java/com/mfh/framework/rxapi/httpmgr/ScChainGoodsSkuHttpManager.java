package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class ScChainGoodsSkuHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final ScChainGoodsSkuHttpManager INSTANCE = new ScChainGoodsSkuHttpManager();
    }

    //获取单例
    public static ScChainGoodsSkuHttpManager getInstance() {
        return ScChainGoodsSkuHttpManager.SingletonHolder.INSTANCE;
    }

    private interface ScChainGoodsSkuService{
        /**
         * 查询供应链商品
         * /scChainGoodsSku/findPublicChainGoodsSku?
         */
        @GET("scChainGoodsSku/findPublicChainGoodsSku")
        Observable<MResponse<MRspQuery<ChainGoodsSku>>>
        findPublicChainGoodsSku(@QueryMap Map<String, String> options);

        /**
         * 查询一个产品sku有哪些批发商供应
         * /scChainGoodsSku/findSupplyChainGoodsSku?barcode=|proSkuId=|nameLike=
         * <p>
         * 注意，这个接口和上面的"findPublicChainGoodsSku"接口返回的结果是一样的。
         * findPublicChainGoodsSku历史原因，里面包含了很多附加逻辑，譬如自动根据当前登录用户
         * 附加了一些条件，使用起来不方便
         */
        @GET("scChainGoodsSku/findSupplyChainGoodsSku")
        Observable<MResponse<MRspQuery<ChainGoodsSku>>>
        findSupplyChainGoodsSku(@QueryMap Map<String, String> options);

    }

    public void findPublicChainGoodsSku(Map<String, String> options, MQuerySubscriber<ChainGoodsSku> subscriber) {
        ScChainGoodsSkuService mfhApi = RxHttpManager.createService(ScChainGoodsSkuService.class);
        Observable observable = mfhApi.findPublicChainGoodsSku(options)
                .map(new MQueryResponseFunc<ChainGoodsSku>());
        toSubscribe(observable, subscriber);
    }

    public void findSupplyChainGoodsSku(Map<String, String> options, MQuerySubscriber<ChainGoodsSku> subscriber) {
        ScChainGoodsSkuService mfhApi = RxHttpManager.createService(ScChainGoodsSkuService.class);
        Observable observable = mfhApi.findSupplyChainGoodsSku(options)
                .map(new MQueryResponseFunc<ChainGoodsSku>());
        toSubscribe(observable, subscriber);
    }



}
