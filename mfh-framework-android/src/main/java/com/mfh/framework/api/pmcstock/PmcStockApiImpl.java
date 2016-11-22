package com.mfh.framework.api.pmcstock;

import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 17/10/2016.
 */

public class PmcStockApiImpl extends PmcStockApi {
    public static void findHumanBySecret(String secret, String stockId,
                                         AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("secret", secret);
        params.put("stockId", stockId);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_STOCK_FINDHUMAN_BYSECRET, params, responseCallback);
    }
    /**
     * 根据订单编号，查询订单基本信息和详情包括订单的商品明细{@link #URL_FINDGOODSORDERLIST_BYHUMAN}
     * @param id 订单编号
     * */
    public static void getGoodsOrderListByHuman(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FINDGOODSORDERLIST_BYHUMAN, params, responseCallback);
    }
    /**
     * 根据订单编号，查询订单基本信息和详情包括订单的商品明细{@link #URL_FINDGOODSORDERLIST_BYHUMAN}
     * @param barcode 订单条码
     * @param humanId 人员编号
     * */
    public static void getGoodsOrderListByHuman(String barcode, Long humanId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        params.put("status", "0");
        params.put("humanId", String.valueOf(humanId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FINDGOODSORDERLIST_BYHUMAN, params, responseCallback);
    }

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
//        fh.addHeader("Cookie", SharedPrefesManagerFactory.getLastSessionId());
        AfinalFactory.postDefault(URL_STOCK_FIND_STOCKOUT, params, responseCallback);
    }

    /**
     * 查找可出库的或已出库的包裹列表
     *
     * @param curStock 是否查询当前仓储中的物件,true-可出库;false-已出库
     * @param humanId  明确查询某个人的包裹，可空
     * @param itemType 查询何种包裹,可空，1-洗衣包裹 2-快递包裹 3-商城包裹 6-皮具包裹
     */
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
     * 加载批次明细:
     */
    public static void findStockOut(Long batchId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("batchId", String.valueOf(batchId));//查询指定批次
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
     * 查询卡券
     * http://devnew.manfenjiayuan.cn/pmc/pmcstock/findConpousByOrderInfo?JSESSIONID=68403b19-bc96-41c5-a7f3-c70cc84ae9ca
     * &jsonStr={"officeId":132079,"orderAmount":25,"productId":[753]}
     * &humanId=133122
     *
     * @param humanId 客户编号
     * @param jsonStr 订单基础信息
     */
    public static void findCouponsByOrderInfo(String humanId, String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", humanId);
        params.put("jsonStr", jsonStr);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FINDCOUPONS_BYORDERINFO, params, responseCallback);
    }


    /**
     * 订单预支付
     * @param humanId 人员编号
     * @param orderIds 订单id,多个以英文,隔开(必填)
     * @param btype 业务类型, 3-商城(必填)
     * @param wayType 支付途径 {@link com.mfh.framework.api.constant.WayType}
     *
     * */
    public static void prePayOrder(Long humanId, int wayType, Long configId,
                                   String orderIds, int btype, String nonceStr,
                                   AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("wayType", String.valueOf(wayType));
        params.put("configId", String.valueOf(configId));
//            //微信jsapi
//            params.put("wxopenid", String.valueOf(humanId));

        params.put("nonceStr", nonceStr);//随机字符串（32位,不能为空!）
        params.put("orderIds", orderIds);
        params.put("btype", String.valueOf(btype));

        AfinalFactory.postDefault(PmcStockApi.URL_PRE_PAY_ORDER, params, responseCallback);
    }
}
