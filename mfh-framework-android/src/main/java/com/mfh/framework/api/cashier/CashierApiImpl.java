package com.mfh.framework.api.cashier;


import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 满分POS－－API接口--网络请求
 * Created by NAT.ZZN(bingshanguxue) on 2015/9/14.
 */
public class CashierApiImpl extends CashierApi {
    /**
     * 提交收银订单
     * */
    public static void batchInOrders(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("jsonStr", jsonStr);

        AfinalFactory.getHttp(true).post(URL_POS_BATCH_INORDERS, params, responseCallback);
    }


    public static void findHumanBySecret(String secret, String stockId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("secret", secret);
        params.put("stockId", stockId);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_STOCK_FINDHUMAN_BYSECRET, params, responseCallback);
    }

    public static void findHumanInfoByMobile(String mobile, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("companyId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put("mobile", mobile);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_RECEIVE_ORDER_FIND_HUMANINFO_BY_MOBILE, params, responseCallback);
    }

    public static void findCompanyByHumanId(Long humanId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FIND_COMPANY_BY_HUMANID, params, responseCallback);
    }

    /**
     * 创建批次
     */
    public static void receiveBatchCreateAndFee(String stockId, Long humanId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("stockId", stockId);
        jsonObject.put("humanId", humanId);
        jsonObject.put("stockType", 2);//1-订单类 2-快递类 99-库存类
        jsonObject.put("income", 0);
        jsonObject.put("accountPassword", "");

        params.put("jsonStr", jsonObject.toJSONString());

        AfinalFactory.postDefault(URL_RECEIVE_BATCH_CREATE_AND_FEE, params, responseCallback);
    }

    /**
     * 入库
     */
    public static void receiveOrderStockInItems(String batchId, String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("batchId", batchId);
        params.put("jsonStr", jsonStr);

        AfinalFactory.postDefault(URL_STOCK_RECEIVEORDER_STOCKINITEMS, params, responseCallback);
    }


    /**
     * 新增快递公司
     * humanId参数不能为空!
     */
    public static void receiveBatchSaveHumanFDCompany(Long humanId, Long companyId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("companyId", companyId == null ? "" : String.valueOf(companyId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_RECEIVEBATCH_SAVE_HUMANFDCOMPANY, params, responseCallback);
    }

    /**
     * 查询周边小区
     */
    public static void findSubdisCodesByNetId(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FIND_SUBDISCODES_BY_NETID, params, responseCallback);
    }

    public static void refundAlipayOrder(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
//        params.put("netId", MfhLoginService.get().getCurOfficeId() == null ? "" : String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FIND_SUBDISCODES_BY_NETID, params, responseCallback);
    }


    /**
     * 收货时租户sku档案登记,参数同上，除了没有库存信息
     * /scMartGoodsSku/saveTenantSku?jsonStr={
     * "product":{},
     * "defaultSku":{},
     * "tenantSku":{}}
     */
    public static void saveTenantSku(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonStr);
//        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_SCMARTGOODSSKU_SAVETENANTSKU, params, responseCallback);
    }


    /**
     * 查询批次流水：出库/入库单
     * @param orderType orderType=0|1|2  0-入库 1-出库 2-直接设置
     * */
//    public static void queryInvIoOrder(int orderType, AjaxCallBack<? extends Object> responseCallback){
//        AjaxParams params = new AjaxParams();
//        params.put("orderType", String.valueOf(orderType));
//        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
//
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
//        AfinalFactory.getHttp(true).post(URL_INVIOORDER_LIST, params, responseCallback);
//    }

    /**
     * 检查衣袋编号是否重复
     */
    public static void checkPackageCodeExist(String packageCode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("packagecode", packageCode);

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_MFHORDER_CHECK_PACKAGECODE, params, responseCallback);
    }

    /**
     * 保存洗衣订单，物业下单,其中companyId是服务供应商，skuId是产品skuId, goodsId是供应链商提供的服务skuId
     */
    public static void saveLaundryOrder(String order, String items, String couponId, String ruleIds, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("order", order);
        params.put("items", items);
//        params.put("couponId", items);
//        params.put("ruleIds", ruleIds);

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_MFHORDER_SAVE_LAUNDRYORDER, params, responseCallback);
    }


    /**
     * 创建订单
     * */
    public static void createPayOrder(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("jsonStr", jsonStr);

        AfinalFactory.getHttp(true).post(URL_PAYORDER_CREATE, params, responseCallback);
    }

    /**
     * 查询订单
     * */
    public static void listPayOrder(Integer bizType, Integer status, PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("status", String.valueOf(status));
        params.put("bizType", String.valueOf(bizType));
        params.put("sellOffice", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        AfinalFactory.getHttp(true).post(URL_PAYORDER_LIST, params, responseCallback);
    }

    /**
     * 判断是否需要锁定pos，由pos端主动发起询问
     * */
    public static void needLockPos(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_NEEDLOCKPOS, params, responseCallback);
    }

    /**
     * 查询限额情况,第一个是限额（0代表没有设置或限额无穷大），第二个是未缴现金
     * /scNetRealInfo/queryLimitInfo
     * */
    public static void queryLimitInfo(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_QUERYLIMITINFO, params, responseCallback);
    }

    /**
     * 查pos订单的现金流水：
     * /orderPayWay/list?payType=1&officeId=136076&orderby=CREATED_DATE&orderbydesc=true
     * */
    public static void listOrderPayWay(Integer payType, PageInfo pageInfo,
                                       AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("payType", String.valueOf(payType));
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_ORDERPAYWAY_LIST, params, responseCallback);
    }

}
