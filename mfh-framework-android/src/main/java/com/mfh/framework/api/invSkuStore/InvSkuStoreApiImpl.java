package com.mfh.framework.api.invSkuStore;

import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 7/28/16.
 */
public class InvSkuStoreApiImpl extends InvSkuStoreApi{
    /**
     * 当前登录用户更改其本店库
     * 存商品的线上售卖状态,0代表下线，1代表上线
     * /invSkuStore/updateStatus?status=0|1&id=11111
     * 或/invSkuStore/updateStatus?status=0|1&barcode=11111
     */
    public static void updateStatusByBarcode(Integer status, String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("status", String.valueOf(status));
        params.put("barcode", barcode);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_UPDATE_STATUS, params, responseCallback);
    }

    /**
     * 当前登录用户更改其本店库存商品的线上售卖状态,0代表下线，1代表上线
     * /invSkuStore/updateStatus?status=0|1&id=11111
     * 或/invSkuStore/updateStatus?status=0|1&barcode=11111
     */
    public static void updateStatusById(Integer status, Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("status", String.valueOf(status));
        params.put("id", String.valueOf(id));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_UPDATE_STATUS, params, responseCallback);
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
