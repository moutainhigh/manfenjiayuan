package com.mfh.framework.api.invSkuStore;

import com.mfh.framework.core.utils.StringUtils;
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

    /**
     * 当前登录人员，把平台商品导入到本店仓储中
     * <ol>
     * 适用场景
     * <li>需要登录</li>
     * </ol>
     */
    public static void importFromCenterSkus(String proSkuIds,  AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(proSkuIds)){
            params.put("proSkuIds", proSkuIds);
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_IMPORT_FROMCENTERSKUS, params, responseCallback);
    }
}
