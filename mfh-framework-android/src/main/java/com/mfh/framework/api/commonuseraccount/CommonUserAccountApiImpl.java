package com.mfh.framework.api.commonuseraccount;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import static com.mfh.framework.api.commonuseraccount.CommonUserAccountApi.URL_ACTIVATEACCOUNT;


/**
 * Created by bingshanguxue on 14/10/2016.
 */

public class CommonUserAccountApiImpl extends CommonUserAccountApi{
    /**
     * 开卡并激活用户账户
     *
     * @param shortNo 卡面号
     * @param cardId  卡芯片号
     * @param humanId 用户编号 {@see com.mfh.litecashier.bean.Human}
     * @see #URL_ACTIVATEACCOUNT
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
     * @see URL_TRANSFERFROMMYACCOUNT
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
     * @param bizType 业务类型
     * @param version 版本号，1,只返回payAmount; 2,返回运费优惠券费用
     * @param jsonStr    订单明细信息
     * @param couponsIds 卡券领用号
     * @param ruleIds    规则
     */
    public static void getPayAmountByOrderInfo(int version, String jsonStr,
                                               String couponsIds, String ruleIds,
                                               AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("version", String.valueOf(version));
        params.put("jsonStr", jsonStr);
        if (!StringUtils.isEmpty(couponsIds)){
            params.put("couponsIds", couponsIds);
        }
        if (!StringUtils.isEmpty(ruleIds)){
            params.put("ruleIds", ruleIds);
        }

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_GETPAYAMOUNT_BYORDERINFO, params, responseCallback);
    }

    public static void getPayAmountByOrderInfos(Integer bizType, String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("bizType", String.valueOf(bizType));
        params.put("jsonStr", jsonStr);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_GET_PAYAMOUNT_BY_ORDERINFOS, params, responseCallback);
    }

    /**
     * 商城订单支付（满分账户）
     * @param humanId 人员编号
     * @param orderId 订单编号，多个订单以逗号隔开。
     * @param accountPassword 支付密码
     * @param couponId 优惠券ID
     * */
    public static void scAccountPay(Integer bizType, String orderId, Long humanId,  String accountPassword,
                                  AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("orderId", orderId);
        params.put("accountPassword", accountPassword);
//        if (couponId != null){
//            params.put("couponId", couponId);
//        }
//        StringBuilder bType = new StringBuilder();
//        if (!StringUtils.isEmpty(orderId)){
//            String[] ids = orderId.split(",");
//            for (int i = 0; i < ids.length; i++){
//                bType.append(String.valueOf(BizType.SC));
//                if (i < ids.length - 1){
//                    bType.append(",");
//                }
//            }
//        }
        params.put("btype", String.valueOf(bizType));//订单类型
//        params.put("isCash", "0");//是否现金支付,0-否 1-是 默认为0

        AfinalFactory.postDefault(URL_SCACCOUNTPAY, params, responseCallback);
    }
}
