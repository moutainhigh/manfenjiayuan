package com.mfh.framework.api.scGoodsSku;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 5/20/16.
 */
public class ScGoodsSkuApiImpl extends ScGoodsSkuApi {
    /**
     * 根据条码查找租户是否已经发布过该商品，若存在返回信息
     */
    public static void getLocalByBarcode(String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_GETLOCAL_BYBARCODE, params, responseCallback);
    }

    /**
     * 查询前台类目商品
     * @param categoryId 前台类目编号
     * */
    public static void findGoodsListByFrontCategory(Long categoryId, PageInfo pageInfo,
                                     AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (categoryId != null) {
            params.put("categoryId", String.valueOf(categoryId));
        }
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FINDGOODSLIST, params, responseCallback);
    }

    /**
     * 查询后台类目商品
     * @param procateId 后台类目编号
     * */
    public static void findGoodsListByBackendCategory(Long procateId, PageInfo pageInfo,
                                                    AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (procateId != null) {
            params.put("procateId", String.valueOf(procateId));
        }
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FINDGOODSLIST, params, responseCallback);
    }

    /**
     * 查询商品
     * @param barcode 商品条码
     * */
    public static void findGoodsListByBarcode(String barcode, PageInfo pageInfo,
                                     AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(barcode)) {
            params.put("barcode", barcode);
        }
        params.put("needSellNum", "true");//销量
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FINDGOODSLIST, params, responseCallback);
    }

    /**
     * 根据商品名称查询商品，模糊查询
     * */
    public static void findGoodsListByName(String name, PageInfo pageInfo,
                                     AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(name)) {
            params.put("name", name);
        }
        params.put("needSellNum", "true");
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FINDGOODSLIST, params, responseCallback);
    }

    /**
     * 店家商品建档入库
     *
     * <ol>
     *     备注
     *     <li>网店商品档案有，只可以修改零售价，其他无效。</li>
     *     <li>网店商品档案没有,平台档案和租户商品档案有，可以修改零售价,初始库存，初始成本价，其他无效。</li>
     *     <li>网店商品档案，平台档案和租户商品档案都没有，所有信息都可以修改。</li>
     * </ol>
     *
     * @param jsonStr 其中product为产品本身信息；proSku为产品sku信息；
     *                skuInfo为店家商品sku信息(costPrice为售价，buyPrice为采购价，quantity为入库数量,lowerLimit为最低安全库存);
     *                mfhSupply为是否需要满分配货(0-不需要，1-需要)，mfhPrice为满分价。
     * @param storeType 默认不传为0代表零售商，如果是批发商建档则storeType=1
     *
     */
    public static void storeIn(String jsonStr, Integer storeType,
                               AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonStr);
        if (storeType != null){
            params.put("storeType", String.valueOf(storeType));
        }
//        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_STOREIN, params, responseCallback);
    }


    /**
     * 查询指定网点可同步sku总数
     */
    public static void countNetSyncAbleSkuNum(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_COUNTNETSYNCABLESKUNUM, params, responseCallback);
    }

    /**
     * 查询库存商品
     *  <ul>
     * 适用场景：
     * <li>查询库存商品</li>
     * </ul>
     * @param joinFlag false,只查网点商品
     */
    public static void listScGoodsSku(PageInfo pageInfo, Long categoryId, String barcode, String name,
                                      String orderby, boolean orderbyDesc, boolean joinFlag,
                                      String priceType, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        //类目
        if (categoryId != null) {
            params.put("categoryId", String.valueOf(categoryId));
        }
//        价格类型0-计件 1-计重
        params.put("priceType", priceType);
        //排序
        if (!StringUtils.isEmpty(orderby)) {
            params.put("orderby", orderby);
            params.put("orderbydesc", String.valueOf(orderbyDesc));
        }
        //gku.sell_day_num
        params.put("joinFlag", String.valueOf(joinFlag));// 只查网点商品
        params.put("barcode", barcode);
        params.put("name", name);
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_LIST, params, responseCallback);
    }

    public static void listScGoodsSku(Long categoryId, PageInfo pageInfo, boolean joinFlag,
                                      AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        //类目
        if (categoryId != null) {
            params.put("categoryId", String.valueOf(categoryId));
        }
        //gku.sell_day_num
        params.put("joinFlag", String.valueOf(joinFlag));// 只查网点商品
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_LIST, params, responseCallback);
    }

    /**
     * 查询租户的商品档案
     * <ol>
     * 适用场景
     * <li>门店商品报损</li>
     * <li>门店发送商品</li>
     * <li>PDA盘点商品</li>
     * <li>PDA商品绑定货架</li>
     * </ol>
     */
    public static void checkWithBuyInfoByBarcode(String barcode,
                                         AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_CHECKWITHBUYINFO_BYBARCODE, params, responseCallback);
    }

    /**
     * 查询租户的商品档案
     * <ol>
     * 适用场景
     * <li>门店商品报损</li>
     * <li>门店发送商品</li>
     * <li>PDA盘点商品</li>
     * <li>PDA商品绑定货架</li>
     * </ol>
     */
    public static void getGoodsByBarCode(String barcode,
                                                   AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_GETGOODS_BYBARCODE, params, responseCallback);
    }

    /**
     * 根据条码查找租户是否已经发布过该商品，若存在返回信息
     * 适用场景：
     * <ol>
     *     <li>
     *         门店收银自采商品（建档&入库）
     *     </li>
     *     <li>价签打印</li>
     * </ol>
     */
    public static void getByBarcode(String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_GET_BYBARCODE, params, responseCallback);
    }

    /**
     * 从批发商导入某个类目的商品到当前门店
     * @param sendTenantId 平台上的某个批发商
     * @param cateType 类目
     * @param startCursor 时间戳
     * */
    public static void importFromChainSku(Long sendTenantId, String cateType, String startCursor,
                                          AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("sendTenantId", String.valueOf(sendTenantId));
        //2016-07-06,不传cateType,由后台控制，导入全部类目商品
//        if (!StringUtils.isEmpty(cateType)){
//            params.put("cateType", cateType);
//        }
        if (!StringUtils.isEmpty(startCursor)){
            params.put("startCursor", startCursor);
        }
        else{
            params.put("startCursor", "2015-01-01 10:00:00");
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_IMPORT_FROM_CHAINSKU, params, responseCallback);
    }

    /**
     * 批量查询网点商品信息
     * @param netId 店铺编号
     * @param proSkuIds 商品编号
     * */
    public static void findOnlineGoodsList(Long netId, String proSkuIds, PageInfo pageInfo,
                                           AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(proSkuIds)) {
            params.put("proSkuIds", proSkuIds);
        }
        params.put("netId", String.valueOf(netId));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FINDONLINEGOODSLIST, params, responseCallback);
    }
    /**
     * 批量查询网点商品信息
     * @param netId 店铺编号
     * @param frontCategoryId 类目编号
     * */
    public static void findOnlineGoodsList2(Long netId, Long frontCategoryId, PageInfo pageInfo,
                                           AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("netId", String.valueOf(netId));
        params.put("frontCategoryId", String.valueOf(frontCategoryId));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FINDONLINEGOODSLIST, params, responseCallback);
    }
}
