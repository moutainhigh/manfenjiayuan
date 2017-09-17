package com.mfh.framework.api;

import com.mfh.framework.BizConfig;
import com.mfh.framework.api.account.UserApi;
import com.mfh.framework.api.analysis.AnalysisApi;
import com.mfh.framework.api.anon.sc.ProductCatalogApi;
import com.mfh.framework.api.anon.sc.product.ScProductApi;
import com.mfh.framework.api.anon.sc.productPrice.ScProductPriceApi;
import com.mfh.framework.api.anon.sc.storeRack.ScStoreRackApi;
import com.mfh.framework.api.cashier.CashierApi;
import com.mfh.framework.api.category.ScCategoryInfoApi;
import com.mfh.framework.api.clientLog.ClientLogApi;
import com.mfh.framework.api.commonuseraccount.CommonUserAccountApi;
import com.mfh.framework.api.companyInfo.CompanyInfoApi;
import com.mfh.framework.api.invCheckOrder.InvCheckOrderApi;
import com.mfh.framework.api.invCompProvider.InvComProviderApi;
import com.mfh.framework.api.invFindOrder.InvFindOrderApi;
import com.mfh.framework.api.invIoOrder.InvIoOrderApi;
import com.mfh.framework.api.invOrder.InvOrderApi;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApi;
import com.mfh.framework.api.invSendOrder.InvSendOrderApi;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApi;
import com.mfh.framework.api.netInfo.NetInfoApi;
import com.mfh.framework.api.payOrder.PayOrderApi;
import com.mfh.framework.api.pmcstock.PmcStockApi;
import com.mfh.framework.api.posRegister.PosRegisterApi;
import com.mfh.framework.api.posorder.PosOrderApi;
import com.mfh.framework.api.reciaddr.ReciaddrApi;
import com.mfh.framework.api.res.ResApi;
import com.mfh.framework.api.scChainGoodsSku.ScChainGoodsSkuApi;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApi;
import com.mfh.framework.api.scOrder.ScOrderApi;
import com.mfh.framework.api.shoppingCart.ShoppingCartApi;
import com.mfh.framework.api.sms.HumanAuthTempApi;
import com.mfh.framework.api.stock.StockApi;
import com.mfh.framework.api.subdist.SubdistApi;
import com.mfh.framework.network.NetFactory;


/**
 * 满分家园后台接口
 * Created by bingshanguxue on 2015/6/11.
 */
public class MfhApi implements ApiParams {
    public static String URL_TENANT = NetFactory.getServerUrl();

    //UBS:默认从配置文件读取，也可以配置
    public static String URL_BASE_SERVER = NetFactory.getServerUrl();
    //网络电话
    public static String URL_NET_PHONE = URL_BASE_SERVER.replace(":8080/pmc", "")
            + "/msgcore/embYtx/getYuninfoByGuid";
    //域名
    public static String URL_DEFAULT = "http://devmobile.manfenjiayuan.cn/";
    /**
     * 渠道编号
     */
    public static String CHANNEL_ID = NetFactory.getChannelId();
    public static String WXPAY_CHANNEL_ID = NetFactory.getWxPayChannelId();
    public static String ALIPAY_CHANNEL_ID = NetFactory.getAliPayChannelId();

    //注册消息桥参数
    public static String PARAM_VALUE_QUEUE_NAME_DEF = "pmc-app-queue";

    static{
        if(BizConfig.RELEASE){
            URL_DEFAULT = "http://mobile.manfenjiayuan.cn/";
            PARAM_VALUE_QUEUE_NAME_DEF = "pmc-app-queue";
        }else{
            URL_DEFAULT = "http://devmobile.manfenjiayuan.cn/";
            PARAM_VALUE_QUEUE_NAME_DEF = "pmc-app-queue-test";
        }
    }

    /**
     * 注册接口
     * 2016-11-04 新增了多租户功能，切换域名后需要重新注册接口*/
    public static void register(){
        UserApi.register();
        AnalysisApi.register();
        ScProductApi.register();
        ScProductPriceApi.register();
        ScStoreRackApi.register();
        ProductCatalogApi.register();
        CashierApi.register();
        ScCategoryInfoApi.register();
        ClientLogApi.register();
        CommonUserAccountApi.register();
        CompanyInfoApi.register();
        InvCheckOrderApi.register();
        InvComProviderApi.register();
        InvFindOrderApi.register();
        InvIoOrderApi.register();
        InvSendIoOrderApi.register();
        InvSendOrderApi.register();
        InvSkuStoreApi.register();
        NetInfoApi.register();
        PayOrderApi.register();
        PmcStockApi.register();
        PosRegisterApi.register();
        ReciaddrApi.register();
        ResApi.register();
        ScChainGoodsSkuApi.register();
        ScGoodsSkuApi.register();
        ScOrderApi.register();
        ShoppingCartApi.register();
        HumanAuthTempApi.register();
        StockApi.register();
        SubdistApi.register();

        InvOrderApi.register();

        InvSkuLabelApi.register();
        PosOrderApi.register();
        ScApi.register();
    }
}
