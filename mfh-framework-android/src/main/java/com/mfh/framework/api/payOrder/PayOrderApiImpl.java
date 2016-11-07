package com.mfh.framework.api.payOrder;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 04/11/2016.
 */

public class PayOrderApiImpl extends PayOrderApi{
    /**
     * 预支付(充值), 返回给支付宝客户端签名后的充值订单信息，可以直接使用
     * @param humanId 人员编号
     * @param amount 充值金额(充值金额必须为数字！单位为元，最小金额为0.01元。)
     * @param wayType 支付途径
     * @param nonceStr 随机字符串（32位,不能为空!）
     *
     * */
    public static void prePay(Long configId, Long humanId, String amount, int wayType, String nonceStr,
                              AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("configId", String.valueOf(configId));
        params.put("humanId", String.valueOf(humanId));
        params.put("amount", amount);
        params.put("nonceStr", nonceStr);//
        params.put("wayType", String.valueOf(wayType));
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));

        AfinalFactory.postDefault(URL_PREPAY, params, responseCallback);
    }

    /**
     * 充值预支付(app端微信支付)
     * @param configId 使用哪一个微信支付的编号默认为空后台自动根据当前生态租户选取。
     * @param humanId 人员编号
     * @param amount 充值金额(充值金额必须为数字！单位为元，最小金额为0.01元。)
     * @param wayType 支付途径(1-支付宝 2-微信 21-app端调用微信支付),可不填，默认为2-微信
     * @param nonceStr 随机字符串（32位,不能为空!）
     * @param bizType 业务类型,默认是99充值,商城订单支付是3
     * */
    public static void prePayForApp(Long configId, Long humanId, String amount,
                                    int wayType, String nonceStr, Integer bizType,
                                    AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("configId", String.valueOf(configId));
        params.put("humanId", String.valueOf(humanId));
        params.put("amount", amount);
        params.put("nonceStr", nonceStr);//随机字符串（32位,不能为空!）
        params.put("wayType", String.valueOf(wayType));
        // {"code":"1","msg":"For input string: \"null\"","data":null,"version":1}
        if (bizType != null){
            params.put("bizType", String.valueOf(bizType));
        }

        AfinalFactory.postDefault(URL_PREPAY_FORAPP, params, responseCallback);
    }

    /**
     * 创建订单
     * */
    public static void create(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("jsonStr", jsonStr);

        AfinalFactory.getHttp(true).post(URL_CREATE, params, responseCallback);
    }

    /**
     * 查询订单
     * */
    public static void list(Integer bizType, Integer status, PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("status", String.valueOf(status));
        params.put("bizType", String.valueOf(bizType));
        params.put("sellOffice", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        AfinalFactory.getHttp(true).post(URL_LIST, params, responseCallback);
    }
}
