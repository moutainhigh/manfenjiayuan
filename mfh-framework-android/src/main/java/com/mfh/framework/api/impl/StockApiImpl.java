package com.mfh.framework.api.impl;

import com.mfh.framework.api.StockApi;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 库存Api
 * Created by bingshanguxue on 4/22/16.
 */
public class StockApiImpl extends StockApi{
    /**
     * 查找可出库的或已出库的包裹列表
     *
     * @param curStock 是否查询当前仓储中的物件,true-可出库;false-已出库
     * @param humanId  明确查询某个人的包裹，可空
     * @param itemType 查询何种包裹,可空，1-洗衣包裹 2-快递包裹 3-商城包裹 6-皮具包裹
     */
    public static void findStockOut(boolean curStock, String humanId, String itemType, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("curStock", String.valueOf(curStock));
        if (!StringUtils.isEmpty(humanId)) {
            params.put("humanId", humanId);
        }
        if (!StringUtils.isEmpty(itemType)) {
            params.put("itemType", itemType);
        }
        params.put("stockIds", MfhLoginService.get().getStockIds());//stockIds:针对哪些仓库，逗号分隔
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
//        fh.addHeader("Cookie", SharedPreferencesManager.getLastSessionId());
        AfinalFactory.postDefault(URL_STOCK_FIND_STOCKOUT, params, responseCallback);
    }
    /**
     * 查找可出库的或已出库的包裹列表
     * @param curStock 是否查询当前仓储中的物件,true-可出库;false-已出库
     * @param humanId 明确查询某个人的包裹，可空
     * @param itemType 查询何种包裹,可空，1-洗衣包裹 2-快递包裹 3-商城包裹 6-皮具包裹
     * */
    public static void findStockOutByCode(String queryCon, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("curStock", "true");
        if (!StringUtils.isEmpty(queryCon)) {
            params.put("queryCon", queryCon);
        }
        params.put("stockIds", MfhLoginService.get().getStockIds());//stockIds:针对哪些仓库，逗号分隔
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_STOCK_FIND_STOCKOUT, params, responseCallback);
    }

    /**
     * 出库
     */
    public static void stockOut(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("jsonStr", jsonStr);

       AfinalFactory.postDefault(URL_STOCK_OUT, params, responseCallback);
    }
    /**
     * 更新商品信息:
     * /pmcstock/findConpousByOrderInfo?jsonStr={productId:[1,2,3], officeId:.., shopId:..., orderAmount:...} 其中officeId和shopId可以只提供一个，也可以同时为空
     */
    public static void updateStockGoods(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonStr);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
       AfinalFactory.postDefault(URL_INVSKUSTORE_UPDATE, params, responseCallback);
    }

    /**
     * 加载批次明细:
     */
    public static void findStockOut(Long batchId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("batchId", String.valueOf(batchId));//查询指定批次
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
       AfinalFactory.postDefault(URL_STOCK_FIND_STOCKOUT, params, responseCallback);
    }

    /**
     * 货架绑定商品:
     * /pmcstock/findConpousByOrderInfo?jsonStr={productId:[1,2,3], officeId:.., shopId:..., orderAmount:...} 其中officeId和shopId可以只提供一个，也可以同时为空
     */
    public static void bindRackNo(String barcode, String rackNo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        params.put("rackNo", rackNo);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
       AfinalFactory.postDefault(URL_GOODSSKU_BINDRACKNO, params, responseCallback);
    }

    /**
     * 根据条码查询库存商品,如果库存中没有则从租户档案中自动建立库存。门店和批发都适用
     * 适用场景:批发商PDA查询商品
     * */
    public static void getByBarcodeMust(String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
       AfinalFactory.postDefault(URL_INVSKUSTORE_GETBYBARCODEMUST, params, responseCallback);
    }

    /**
     * 智能订货
     * */
    public static void autoAskSendOrder(Long chainCompanyId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("chainCompanyId", String.valueOf(chainCompanyId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
       AfinalFactory.postDefault(URL_INVSKUSTORE_AUTOASKSENDORDER, params, responseCallback);
    }

    /**
     * 库存转换
     * <ol>
     *     适用场景
     *     <li>零售商PDA库存转换</li>
     * </ol>
     * */
    public static void changeSkuStore(String sendItems, String receiveItem, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("sendItems", sendItems);
        params.put("receiveItem", receiveItem);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
       AfinalFactory.postDefault(URL_INVSKUSTORE_CHANGESKUSTORE, params, responseCallback);
    }
}
