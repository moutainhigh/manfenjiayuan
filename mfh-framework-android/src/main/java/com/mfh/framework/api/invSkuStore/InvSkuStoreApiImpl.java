package com.mfh.framework.api.invSkuStore;

import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 7/28/16.
 */
public class InvSkuStoreApiImpl extends InvSkuStoreApi{
    /**
     * 更新商品信息:
     * /pmcstock/findConpousByOrderInfo?jsonStr={productId:[1,2,3], officeId:.., shopId:..., orderAmount:...} 其中officeId和shopId可以只提供一个，也可以同时为空
     */
    public static void update(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonStr);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_UPDATE, params, responseCallback);
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
     */
    public static void getByBarcodeMust(String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSKUSTORE_GETBYBARCODEMUST, params, responseCallback);
    }

    /**
     * 智能订货
     */
    public static void autoAskSendOrder(Long chainCompanyId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("chainCompanyId", String.valueOf(chainCompanyId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSKUSTORE_AUTOASKSENDORDER, params, responseCallback);
    }

    /**
     * 库存转换
     * <ol>
     * 适用场景
     * <li>零售商PDA库存转换</li>
     * </ol>
     */
    public static void changeSkuStore(String sendItems, String receiveItem, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("sendItems", sendItems);
        params.put("receiveItem", receiveItem);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSKUSTORE_CHANGESKUSTORE, params, responseCallback);
    }
}
