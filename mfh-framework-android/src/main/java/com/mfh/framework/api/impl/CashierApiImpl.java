package com.mfh.framework.api.impl;


import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.CashierApi;
import com.mfh.framework.api.constant.AbilityItem;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.util.Date;

/**
 * 满分POS－－API接口--网络请求
 * Created by NAT.ZZN(bingshanguxue) on 2015/9/14.
 */
public class CashierApiImpl extends CashierApi {
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

    /**
     * 发货
     * http://devnew.manfenjiayuan.cn/pmc/scOrder/sendOrder?JSESSIONID=9d01cbf0-059b-4ee9-9391-1612e9276165&orderId=557612
     * 1:orderId参数不能为空!
     */
    public static void sendOrder(Long orderId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("orderId", orderId == null ? "" : String.valueOf(orderId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_SCORDER_SENDORDER, params, responseCallback);
    }


    public static void refundAlipayOrder(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
//        params.put("netId", MfhLoginService.get().getCurOfficeId() == null ? "" : String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FIND_SUBDISCODES_BY_NETID, params, responseCallback);
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
        AfinalFactory.postDefault(URL_FIND_COUPONS_BY_ORDERINFO, params, responseCallback);
    }

    /**
     * 查询卡券,订单提交前：调用方法进行拆分,并针对拆分后的每个订单信息获取可以使用的促销规则和优惠券：
     * /pmcstock/findMarketRulesByOrderInfo?
     * jsonStr={
     * "humanId":94182, "btype":3, "discount":1,"createdDate":"2015-12-10 01:00:00","subdisId":746,
     * items:[{"bcount":1,"price":25,"skuId":1289,"whereId":132079}, {"bcount":1,"price":11,"skuId":1292,"whereId":132079}]
     * }
     *
     * @param jsonStr 订单&订单明细
     */
    public static void findMarketRulesByOrderInfo(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonStr);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FIND_MARKETRULES_BY_ORDERINFO, params, responseCallback);
    }

    public static void findMarketRulesByOrderInfos(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonStr);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FIND_MARKETRULES_BY_ORDERINFOS, params, responseCallback);
    }


    /**
     * 根据订单信息计算实际金额（综合考虑优惠券、促销规则）
     * 参数为：/commonuseraccount/getPayAmountByOrderInfo?couponsIds=&ruleIds=116
     * &jsonStr={
     * "humanId":94182,
     * "btype":7,
     * "discount":1,
     * "createdDate":"2015-12-10 01:00:00",
     * "subdisId":746,
     * items:[
     * {"bcount":1,"price":25,"skuId":1289,"whereId":132079},
     * {"bcount":1,"price":11,"skuId":1292,"whereId":132079}
     * ]
     * }
     *
     * @param couponsIds 卡券领用号
     * @param ruleIds    规则
     * @param jsonStr    订单明细信息
     */
    public static void getPayAmountByOrderInfo(String couponsIds, String ruleIds, String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("couponsIds", couponsIds);
        params.put("ruleIds", ruleIds);
        params.put("jsonStr", jsonStr);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_GET_PAYAMOUNT_BY_ORDERINFO, params, responseCallback);
    }
    public static void getPayAmountByOrderInfos(Integer bizType, String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("bizType", String.valueOf(bizType));
        params.put("jsonStr", jsonStr);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_GET_PAYAMOUNT_BY_ORDERINFOS, params, responseCallback);
    }

    /**
     * 查询租户
     *
     * @param abilityItem {@link AbilityItem}
     */
    public static void findPublicCompanyInfo(String nameLike, Integer abilityItem, PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(nameLike)) {
            params.put("nameLike", nameLike);
        }
        params.put("abilityItem", String.valueOf(abilityItem));//能力
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_COMPANYINFO_FIND_PUBLICCOMPANYINFO, params, responseCallback);
    }


    /**
     * 查询租户
     *
     * @param abilityItem {@link AbilityItem}
     */
    public static void bizSupplyInfoFindPublicCompanyInfo(String shortCodeLike, String abilityItem, PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(shortCodeLike)) {
            params.put("shortCodeLike", shortCodeLike);
        }
        //retTenantId＝1，返回租户，不传retTenantId，返回网点
        params.put("retTenantId", "1");
        if (!StringUtils.isEmpty(abilityItem)) {
            params.put("abilityItem", abilityItem);//能力
        }
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_BIZSUPPLYINFO_FIND_PUBLICCOMPANYINFO, params, responseCallback);
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




    public static void getNetInfoById(String id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", id);
//        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_COMPANYINFO_GETNETINFO_BYID, params, responseCallback);
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
     * 启动交接班统计<br>
     *
     * @param shiftId   班次（POS机本地记录并累加）
     * @param startTime 交接班开始时间（默认使用上一次交接班/日结时间）
     * @param endTime   交接班结束时间 (当前系统时间)
     */
    public static void autoShiftAnalysic(int shiftId, String startTime, String endTime, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("shiftId", String.valueOf(shiftId));
        params.put("startTime", startTime);
        params.put("endTime", endTime);

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_BIZSERVIE_AUTOSHIFITANALYSIC, params, responseCallback);
    }

    /**
     * 流水分析,查询交接班业务类型统计
     *
     * @param aggDate 起始时间，格式：YYYY-MM-DD
     */
    public static void analysisAggShift(int shiftId, String aggDate, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("wrapper", "true");
        params.put("shiftId", String.valueOf(shiftId));
        params.put("aggDate", aggDate);
        params.put("createdBy", String.valueOf(MfhLoginService.get().getCurrentGuId()));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ANALYSIS_AGGSHIFT, params, responseCallback);
    }

    /**
     * 经营分析,查询交接班支付方式统计
     *
     * @param aggDate 起始时间，格式：YYYY-MM-DD
     */
    public static void accAnalysisAggShift(int shiftId, String aggDate, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("wrapper", "true");
        params.put("shiftId", String.valueOf(shiftId));
        params.put("aggDate", aggDate);
        params.put("createdBy", String.valueOf(MfhLoginService.get().getCurrentGuId()));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ACCANALYSIS_AGGSHIFT, params, responseCallback);
    }

    /**
     * 启动日结统计
     */
    public static void autoDateEnd(Date date, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("date", (date != null ? TimeCursor.FORMAT_YYYYMMDD.format(date) : ""));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_BIZSERVIE_AUTODATEEND, params, responseCallback);
    }

    /**
     * 流水分析,查询日结支付方式统计
     *
     * @param aggDate 起始时间，格式：YYYY-MM-DD
     */
    public static void analysisAccDateList(Date aggDate, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("wrapper", "true");
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put("aggDate", aggDate != null ? TimeCursor.FORMAT_YYYYMMDD.format(aggDate) : "");
//        params.put("createdBy", MfhLoginService.get().getCurrentGuId());
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ANALYSIS_ACCDATE_LIST, params, responseCallback);
    }

    /**
     * 经营分析,查询日结业务类型统计
     *
     * @param aggDate 起始时间，格式：YYYY-MM-DD
     */
    public static void analysisAggDateList(Date aggDate, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("wrapper", "true");
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put("aggDate", aggDate != null ? TimeCursor.FORMAT_YYYYMMDD.format(aggDate) : "");
//        params.put("createdBy", MfhLoginService.get().getCurrentGuId());
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ACCANALYSIS_AGGDATE_LIST, params, responseCallback);
    }

    /**
     * 日结确认
     *
     * @param date       日结日期，格式YYYY-MM-DD
     * @param outTradeNo 支付宝交易号
     */
    public static void analysisAcDateDoEnd(Date date, String outTradeNo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("date", date != null ? TimeCursor.FORMAT_YYYYMMDD.format(date) : "");
        if (!StringUtils.isEmpty(outTradeNo)) {
            params.put("outTradeNo", outTradeNo);
        }

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ANALYSISACCDATE_DOEND, params, responseCallback);
    }

    /**
     * 判读是否日结
     */
    public static void analysisAccDateHaveDateEnd(Date date, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("date", date != null ? TimeCursor.FORMAT_YYYYMMDD.format(date) : "");

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ANALYSISACCDATE_HAVEDATEEND, params, responseCallback);
    }

    /**
     * 获取最后日结日期
     */
    public static void analysisAccDateGetLastAggDate(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ANALYSISACCDATE_GETLASTAGGDATE, params, responseCallback);
    }



    /**
     * 首次对某个临时用户发送手机短信验证码
     *
     * @param mobile 手机号
     * @see CashierApi#URL_BEGINAUTHENBYSMS
     */
    public static void beginAuthenBysms(String mobile, AjaxCallBack<? extends Object> responseCallback) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mobile", mobile);
        jsonObject.put("sourceType", 10);

        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonObject.toJSONString());
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_BEGINAUTHENBYSMS, params, responseCallback);
    }

    /**
     * 对某个临时用户重新发送手机短信验证码
     *
     * @param mobile 手机号
     * @see CashierApi#URL_RETRYAUTHENBYSMS
     */
    public static void retryAuthenBysms(String mobile, Long userTmpId,
                                        AjaxCallBack<? extends Object> responseCallback) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mobile", mobile);
        jsonObject.put("userTmpId", userTmpId);

        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonObject.toJSONString());
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_RETRYAUTHENBYSMS, params, responseCallback);
    }

    /**
     * 对接收到的手机验证码进行验证
     *
     * @param token 验证码
     * @see CashierApi#URL_DOAUTHENBYSMS
     */
    public static void doAuthenBysms(String token, Long userTmpId,
                                     AjaxCallBack<? extends Object> responseCallback) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        jsonObject.put("userTmpId", userTmpId);
        jsonObject.put("sourceType", 10);

        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonObject.toJSONString());
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_DOAUTHENBYSMS, params, responseCallback);
    }

    /**
     * 开卡并激活用户账户
     *
     * @param shortNo 卡面号
     * @param cardId  卡芯片号
     * @param humanId 用户编号 {@see com.mfh.litecashier.bean.Human}
     * @see CashierApi#URL_ACTIVATEACCOUNT
     */
    public static void activateAccount(String shortNo, String cardId, Long humanId,
                                       AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("shortNo", shortNo);
        params.put("cardId", cardId);
        params.put("ownerId", String.valueOf(humanId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ACTIVATEACCOUNT, params, responseCallback);
    }

    /**
     * 会员卡充值
     *
     * @see CashierApi#URL_TRANSFERFROMMYACCOUNT
     */
    public static void transferFromMyAccount(String amount, String accountPassword, Long receiveHumanId,
                                             AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("amount", amount);
        params.put("accountPassword", accountPassword);
        params.put("receiveHumanId", String.valueOf(receiveHumanId));
        params.put("isCash", "1");
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_TRANSFERFROMMYACCOUNT, params, responseCallback);
    }

    public static void createPayOrder(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("jsonStr", jsonStr);

        AfinalFactory.getHttp(true).post(URL_PAYORDER_CREATE, params, responseCallback);
    }
}
