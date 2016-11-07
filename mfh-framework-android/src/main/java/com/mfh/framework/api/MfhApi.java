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
import com.mfh.framework.api.invCompProvider.InvComProviderApi;
import com.mfh.framework.api.invCompany.InvCompanyApi;
import com.mfh.framework.api.invFindOrder.InvFindOrderApi;
import com.mfh.framework.api.invIoOrder.InvIoOrderApi;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApi;
import com.mfh.framework.api.invSendOrder.InvSendOrderApi;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApi;
import com.mfh.framework.api.netInfo.NetInfoApi;
import com.mfh.framework.api.pay.PayApi;
import com.mfh.framework.api.payOrder.PayOrderApi;
import com.mfh.framework.api.pmcstock.PmcStockApi;
import com.mfh.framework.api.posRegister.PosRegisterApi;
import com.mfh.framework.api.reciaddr.ReciaddrApi;
import com.mfh.framework.api.res.ResApi;
import com.mfh.framework.api.scChainGoodsSku.ScChainGoodsSkuApi;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApi;
import com.mfh.framework.api.scOrder.ScOrderApi;
import com.mfh.framework.api.shoppingCart.ShoppingCartApi;
import com.mfh.framework.api.sms.EmbWxUserRegisterApi;
import com.mfh.framework.api.sms.HumanAuthTempApi;
import com.mfh.framework.api.stock.StockApi;
import com.mfh.framework.api.subdist.SubdistApi;
import com.mfh.framework.network.NetFactory;


/**
 * 满分家园后台接口
 * Created by bingshanguxue on 2015/6/11.
 */
public class MfhApi {
    //UBS:默认从配置文件读取，也可以配置
    public static String URL_BASE_SERVER = NetFactory.getServerUrl();

    //网络电话
    public static String URL_NET_PHONE = URL_BASE_SERVER.replace(":8080/pmc", "") + "/msgcore/embYtx/getYuninfoByGuid";

    //域名
    public static String DOMAIN = "devmobile.manfenjiayuan.cn";
    public static String URL_DEFAULT = "http://devmobile.manfenjiayuan.cn/";

    //渠道编号
    public final static String PARAM_KEY_CHANNEL_ID = "channelid";
    public static String CHANNEL_ID = NetFactory.getChannelId();

    //注册消息桥参数
    public static String PARAM_VALUE_QUEUE_NAME_DEF = "pmc-app-queue";
    public final static String PARAM_KEY_QUEUE_NAME = "queuename";
    public final static String PARAM_KEY_JSONSTR = "jsonStr";
    public final static String PARAM_KEY_JSESSIONID = "JSESSIONID";

    static{
        if(BizConfig.RELEASE){
            DOMAIN = "mobile.manfenjiayuan.cn";
            URL_DEFAULT = "http://mobile.manfenjiayuan.cn/";
            PARAM_VALUE_QUEUE_NAME_DEF = "pmc-app-queue";
        }else{
            DOMAIN = "devmobile.manfenjiayuan.com";
            URL_DEFAULT = "http://devmobile.manfenjiayuan.cn/";
            PARAM_VALUE_QUEUE_NAME_DEF = "pmc-app-queue-test";
        }
//        URL_BASE_SERVER = NetFactory.getServerUrl();
    }


    //登录参数
    public final static String PARAM_KEY_USERNAME = "username";
    public final static String PARAM_KEY_PASSWORD = "password";
    public final static String PARAM_KEY_LOGIN_TYPE = "loginType";
    public final static String PARAM_KEY_LOGIN_KIND = "loginKind";
    public final static String PARAM_VALUE_LOGIN_TYPE_PMC = "PMC";

    /**
     * 注册接口
     * 2016-11-04 新增了多租户功能，切换域名后需要重新注册接口*/
    public static void register(){
        CompanyHumanApi.URL_COMPANYHUMAN = MfhApi.URL_BASE_SERVER + "/companyHuman/";
        InvOrderApi.URL_INVCHECKORDER = MfhApi.URL_BASE_SERVER + "/invCheckOrder/";
        InvOrderApi.URL_INVCHECKORDER_ITEM = MfhApi.URL_BASE_SERVER + "/invCheckOrderItem/";
        InvOrderApi.URL_INVLOSSORDER = MfhApi.URL_BASE_SERVER + "/invLossOrder/";
        InvOrderApi.URL_INVLOSSORDER_ITEM = MfhApi.URL_BASE_SERVER + "/invLossOrderItem/";
        InvOrderApi.URL_INVIOORDER_ITEM = MfhApi.URL_BASE_SERVER + "/invIoOrderItem/";
        InvSkuLabelApi.URL_INVSKULABEL = MfhApi.URL_BASE_SERVER + "/invSkuLabel/";
        PosOrderApi.URL_POSORDER = MfhApi.URL_BASE_SERVER + "/posOrder/";
        ScApi.URL_WX_SHOP_DEVICE_PAGE = MfhApi.URL_BASE_SERVER + "/wxShopDevicePage/list";
        ScApi.URL_PRODUCT_AGGDATE_LIST = MfhApi.URL_BASE_SERVER + "/productAggDate/list";
        UserApi.register();
        AnalysisApi.register();
        ProductCatalogApi.register();
        ScProductApi.URL_ANON_SC_PRODUCT = MfhApi.URL_BASE_SERVER + "/anon/sc/product/";
        ScProductPriceApi.URL_ANON_SC_PRODUCTPRICE = MfhApi.URL_BASE_SERVER + "/anon/sc/productPrice/";
        ScStoreRackApi.register();
        CashierApi.register();
        ScCategoryInfoApi.URL_SC_CATEGORYINFO = MfhApi.URL_BASE_SERVER + "/scCategoryInfo/";
        ClientLogApi.URL_CLIENTLOG = MfhApi.URL_BASE_SERVER + "/clientLog/";
        CommonUserAccountApi.URL_COMMONUSERACCOUNT = MfhApi.URL_BASE_SERVER + "/commonuseraccount/";
        CompanyInfoApi.URL_COMPANYINFO = MfhApi.URL_BASE_SERVER + "/companyInfo/";
        InvCompanyApi.URL_INV_COMPANY = MfhApi.URL_BASE_SERVER + "/invCompany/";
        InvComProviderApi.URL_INV_COMPROVIDER = MfhApi.URL_BASE_SERVER + "/invCompProvider/";
        InvFindOrderApi.URL_INVFINDORDER = MfhApi.URL_BASE_SERVER + "/invFindOrder/";
        InvIoOrderApi.URL_INVIOORDER = MfhApi.URL_BASE_SERVER + "/invIoOrder/";
        InvSendIoOrderApi.URL_INVSENDIOORDER = MfhApi.URL_BASE_SERVER + "/invSendIoOrder/";
        InvSendOrderApi.URL_INVSENDORDER = MfhApi.URL_BASE_SERVER + "/invSendOrder/";
        InvSkuStoreApi.URL_INVSKUSTORE = MfhApi.URL_BASE_SERVER + "/invSkuStore/";
        NetInfoApi.URL_NETINFO = MfhApi.URL_BASE_SERVER + "/netInfo/";
        PayApi.URL_ALIPAY_BARPAY = MfhApi.URL_BASE_SERVER + "/toAlipayBarTradePay/barPay";
        PayApi.URL_ALIPAY_QUERY = MfhApi.URL_BASE_SERVER + "/toAlipayBarTradePay/query";
        PayApi.URL_ALIPAY_CANCEL = MfhApi.URL_BASE_SERVER + "/toAlipayBarTradePay/cancelOrder";
        PayApi.URL_WXBARPAY_PAY = MfhApi.URL_BASE_SERVER + "/toWxpayBarTradePay/barPay";
        PayApi.URL_WXBARPAY_QUERY = MfhApi.URL_BASE_SERVER + "/toWxpayBarTradePay/query";
        PayApi.URL_WXBARPAY_CANCEL = MfhApi.URL_BASE_SERVER + "/toWxpayBarTradePay/cancelOrder";
        PayOrderApi.URL_PAYORDER = MfhApi.URL_BASE_SERVER + "/payOrder/";
        PmcStockApi.URL_PMCSTOCK = MfhApi.URL_BASE_SERVER + "/pmcstock/";
        PosRegisterApi.URL_POS_REGISTER = MfhApi.URL_BASE_SERVER + "/posRegister/";
        ReciaddrApi.URL_IRECIADDR = MfhApi.URL_BASE_SERVER + "/reciaddr/";
        ResApi.URL_REMOTE = MfhApi.URL_BASE_SERVER + "/res/remote/";
        ResApi.URL_REMOTESAVE = MfhApi.URL_BASE_SERVER + "/res/remotesave/";
        ScChainGoodsSkuApi.URL_SCCHAINGOODSSKU = MfhApi.URL_BASE_SERVER + "/scChainGoodsSku/";
        ScChainGoodsSkuApi.URL_INVSKUPROVIDER_LIST = MfhApi.URL_BASE_SERVER + "/invSkuProvider/list";
        ScGoodsSkuApi.URL_SCGOODSSKU = MfhApi.URL_BASE_SERVER + "/scGoodsSku/";
        ScOrderApi.URL_SCORDER = MfhApi.URL_BASE_SERVER + "/scOrder/";
        ShoppingCartApi.URL_SHOPPING_CART = MfhApi.URL_BASE_SERVER + "/shoppingCart/";
        EmbWxUserRegisterApi.URL_EMB_WXUSER_REGISTER = MfhApi.URL_BASE_SERVER + "/embWxUserRegister/";
        HumanAuthTempApi.URL_HUMANAUTHTEMP = MfhApi.URL_BASE_SERVER + "/humanAuthTemp/";
        HumanAuthTempApi.URL_LOGINBYSMS = MfhApi.URL_BASE_SERVER + "/loginBySms";
        StockApi.URL_STOCK = MfhApi.URL_BASE_SERVER + "/stock/";
        SubdistApi.URL_SUBDIST = MfhApi.URL_BASE_SERVER + "/subdist/";
    }
}
