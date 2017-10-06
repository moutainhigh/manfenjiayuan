package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.api.scGoodsSku.PosGoods;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.func.MResponseFunc;
import com.mfh.framework.rxapi.func.MValueResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by bingshanguxue on 26/01/2017.
 */

public class ScGoodsSkuHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final ScGoodsSkuHttpManager INSTANCE = new ScGoodsSkuHttpManager();
    }

    //获取单例
    public static ScGoodsSkuHttpManager getInstance() {
        return ScGoodsSkuHttpManager.SingletonHolder.INSTANCE;
    }

    private interface ScGoodsSkuService {
        /**
         * 查询批发商采购商品
         * 适用场景：门店采购查询商品
         */
        @GET("scGoodsSku/findStoreWithChainSku")
        Observable<MResponse<MRspQuery<ScGoodsSku>>> findStoreWithChainSku(@QueryMap Map<String, String> options);

        /**
         * 查询库存商品:库存成本，批次流水，库存调拨－－
         * <p/>
         * <ul>
         * 适用场景：
         * <li>手持终端－－根据条码查询库存商品，修改商品零售价和安全库存</li>
         * </ul>
         */
        @GET("scGoodsSku/list")
        Observable<MResponse<MRspQuery<ScGoodsSku>>> list(@QueryMap Map<String, String> options);

        @GET("scGoodsSku/downLoadPosProduct")
        Observable<MResponse<MRspQuery<PosGoods>>> downLoadPosProduct(@QueryMap Map<String, String> options);

        /**
         * 查询指定网点可同步sku总数<br>
         * {"code":"0","msg":"查询成功!","version":"1","data":{"val":"701"}}
         */
        @GET("scGoodsSku/countNetSyncAbleSkuNum")
        Observable<MResponse<MValue<String>>> countNetSyncAbleSkuNum(@QueryMap Map<String, String> options);

        /**
         * 查询发布商品/scGoodsSku/getByBarcode?barcode=77777777&&JSESSIONID=7a6b9fe4-f6fb-4985-9810-6a7c544eeb0d
         * 根据条码逐级查找商品：
         * 若门店中存在该商品则返回信息中id、tenantSkuId、proSkuId、productId都不为空，且quantity和costPrice都有值
         * 若仅在租户存在则返回信息中tenantSkuId、proSkuId、productId不为空，且costPrice有值， quantity为0;
         * 若仅在产品中心中存在则返回信息中proSkuId、productId不为空, costPrice为空，quantity为0；
         * 若产品中心也不存在，则返回null<br>
         *
         * 适用场景：
         * <ol>
         *     <li>
         *         门店收银自采商品（建档&入库）
         *     </li>
         *     <li>价签打印</li>
         * </ol>
         */
        @GET("scGoodsSku/getByBarcode")
        Observable<MResponse<ScGoodsSku>> getByBarcode(@QueryMap Map<String, String> options);

        /**
         * 店家商品建档入库
         *
         * /scGoodsSku/storeIn?jsonStr=&storeType=0|1，
         * 增加一个storeType参数，默认不传为0代表零售商，如果是批发商建档则storeType=1
         *
         * <ol>
         *     备注
         *     <li>网店商品档案有，只可以修改零售价，其他无效。</li>
         *     <li>网店商品档案没有,平台档案和租户商品档案有，可以修改零售价,初始库存，初始成本价，其他无效。</li>
         *     <li>网店商品档案，平台档案和租户商品档案都没有，所有信息都可以修改。</li>
         * </ol>
         *
         * @param jsonStr 其中product为产品本身信息；proSku为产品sku信息；
         *                skuInfo为店家商品sku信息(costPrice为售价，buyPrice为采购价，quantity为入库数量,lowerLimit为最低安全库存);
         *                mfhSupply为是否需要满分配货(0-不需要，1-需要)，mfhPrice为满分价。
         * @param storeType 默认不传为0代表零售商，如果是批发商建档则storeType=1
         *
         */
        @GET("scGoodsSku/storeIn")
        Observable<MResponse<String>> storeIn(@QueryMap Map<String, String> options);
    }

    public void findStoreWithChainSku(Map<String, String> options, MQuerySubscriber<ScGoodsSku> subscriber) {
        ScGoodsSkuService mfhApi = RxHttpManager.createService(ScGoodsSkuService.class);
        Observable observable = mfhApi.findStoreWithChainSku(options)
                .map(new MQueryResponseFunc<ScGoodsSku>());
        toSubscribe(observable, subscriber);
    }

    /**
     * 查询库存商品
     *  <ul>
     * 适用场景：
     * <li>查询库存商品</li>
     * </ul>
     * @param joinFlag false,只查网点商品
     */
    public void list(Map<String, String> options, MQuerySubscriber<ScGoodsSku> subscriber) {
        ScGoodsSkuService mfhApi = RxHttpManager.createService(ScGoodsSkuService.class);
        Observable observable = mfhApi.list(options)
                .map(new MQueryResponseFunc<ScGoodsSku>());
        toSubscribe(observable, subscriber);
    }

    public void downLoadPosProduct(Map<String, String> options, MQuerySubscriber<PosGoods> subscriber) {
        ScGoodsSkuService mfhApi = RxHttpManager.createService(ScGoodsSkuService.class);
        Observable observable = mfhApi.downLoadPosProduct(options)
                .map(new MQueryResponseFunc<PosGoods>());
        toSubscribe(observable, subscriber);
    }

    public void countNetSyncAbleSkuNum(Map<String, String> options, MValueSubscriber<String> subscriber) {
        ScGoodsSkuService mfhApi = RxHttpManager.createService(ScGoodsSkuService.class);
        Observable observable = mfhApi.countNetSyncAbleSkuNum(options)
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void getByBarcode(Map<String, String> options, Subscriber<ScGoodsSku> subscriber) {
        ScGoodsSkuService mfhApi = RxHttpManager.createService(ScGoodsSkuService.class);
        Observable observable = mfhApi.getByBarcode(options)
                .map(new MResponseFunc<ScGoodsSku>());
        toSubscribe(observable, subscriber);
    }

    public void storeIn(Map<String, String> options, Subscriber<String> subscriber) {
        ScGoodsSkuService mfhApi = RxHttpManager.createService(ScGoodsSkuService.class);
        Observable observable = mfhApi.storeIn(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }
}
