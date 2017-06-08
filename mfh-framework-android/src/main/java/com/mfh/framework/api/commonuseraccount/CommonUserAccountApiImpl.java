package com.mfh.framework.api.commonuseraccount;

import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;


/**
 * Created by bingshanguxue on 14/10/2016.
 */

public class CommonUserAccountApiImpl extends CommonUserAccountApi{
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

        AfinalFactory.postDefault(URL_SCACCOUNT_PAY, params, responseCallback);
    }

    /**
     * 满分家园账户充值
     * @param preOrderId 预支付订单编号
     * @param orderIds 订单id,多个以英文,隔开(必填)
     * @param btype 业务类型, 3-商城(必填)
     * @param token
     *
     * */
    public static void mfhAccountPay(Long preOrderId, String orderIds, int btype,
                                     String token,
                                     AjaxCallBack<? extends Object> responseCallback){

        AjaxParams params = new AjaxParams();
        params.put("preOrderId", String.valueOf(preOrderId));
        params.put("orderId", orderIds);
        params.put("token", token);
        params.put("btype", String.valueOf(btype));

        AfinalFactory.postDefault(URL_MFACCOUNT_PAY, params, responseCallback);
    }
}
