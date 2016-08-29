package com.mfh.litecashier.presenter;

import com.alibaba.fastjson.JSONArray;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.PosCategory;
import com.mfh.litecashier.bean.wrapper.CashierFunctional;
import com.mfh.litecashier.database.entity.PosProductEntity;
import com.mfh.litecashier.database.entity.PosProductSkuEntity;
import com.mfh.litecashier.database.logic.PosProductSkuService;
import com.mfh.litecashier.mode.CashierMode;
import com.mfh.litecashier.ui.view.ICashierView;
import com.mfh.litecashier.utils.ACacheHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 门店商品
 * Created by bingshanguxue on 16/3/17.
 */
public class CashierPresenter {
    private ICashierView iCashierView;
    private CashierMode iCashierMode;

    public CashierPresenter(ICashierView iCashierView) {
        this.iCashierView = iCashierView;
        this.iCashierMode = new CashierMode();
    }

    /**
     * 查询商品
     * */
    public synchronized void findGoods(String barcode){
        if (StringUtils.isEmpty(barcode)) {
            ZLogger.d("参数无效");
            return;
        }

        int packFlag = 0;//是否是箱规：0不是；1是
        //Step 1:查询商品
        PosProductEntity entity = this.iCashierMode.findGoods(barcode);
        if (entity == null){
            // Step 2: 查询主条码
            List<PosProductSkuEntity> entityList = PosProductSkuService.get()
                    .queryAllByDesc(String.format("otherBarcode = '%s'", barcode));
            if (entityList != null && entityList.size() > 0) {
                PosProductSkuEntity posProductSkuEntity = entityList.get(0);
                String mainBarcode = posProductSkuEntity.getMainBarcode();
                packFlag = posProductSkuEntity.getPackFlag();
                ZLogger.d(String.format("找到%d个主条码%s", entityList.size(), mainBarcode));

                //Step 3:根据主条码再次查询商品
                entity = this.iCashierMode.findGoods(mainBarcode);
            }
        }

        if (entity != null){
            //Step 4:找到商品
            if (iCashierView != null){
                iCashierView.onFindGoods(entity, packFlag);
            }
        }
        else{
            //Step 5:未找到商品
            if (iCashierView != null){
                iCashierView.onFindGoodsEmpty(barcode);
            }
        }
    }

    /**
     * 加载收银机前台类目：私有功能＋公共类目＋自定义类目
     * */
    public synchronized List<CashierFunctional> getCashierFunctions(){
        List<CashierFunctional> functionalList = new ArrayList<>();
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PURCHASE_SEND,
                "采购订单", R.mipmap.ic_service_purchase_send));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PURCHASE_INTELLIGENT,
                "智能订货", R.mipmap.ic_service_intelligent_order));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_FRESH,
                "生鲜", R.mipmap.ic_service_fresh));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PURCHASE_RECEIPT,
                "采购收货", R.mipmap.ic_service_purchase_receipt));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PURCHASE_RETURN,
                "采购退货", R.mipmap.ic_service_purchase_return));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_INVENTORY_TRANS_IN, "调入", R.mipmap.ic_service_inventory_trans_in));
//        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_INVENTORY_TRANS_OUT,
//                "调拨", R.mipmap.ic_service_inventory_trans));
//        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_MALL,
//                "进货优惠", R.mipmap.ic_service_mall));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_RETURN_GOODS,
                "退货", R.mipmap.ic_service_returngoods));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_MEMBER_CARD,
                "会员卡", R.mipmap.ic_service_membercard));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_RECHARGE,
                "转账充值", R.mipmap.ic_service_recharge));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_STORE_PROMOTION,
                "门店促销", R.mipmap.ic_service_store_promotion));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_DEFECTIVE,
                "报损", R.mipmap.ic_service_defective));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_LAUNDRY,
                "洗衣", R.mipmap.ic_service_laundry));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PACKAGE,
                " 取包裹", R.mipmap.ic_service_package));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_COURIER,
                "快递代收", R.mipmap.ic_service_courier));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_EXPRESS,
                "寄快递", R.mipmap.ic_service_express));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_RECEIVE_GOODS,
                "商品领取", R.mipmap.ic_service_receivegoods));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_FEEDPAPER,
                "走纸", R.mipmap.ic_service_feedpaper));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PAYBACK, "返货", R.mipmap.ic_service_payback));
//        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PRIVATE,
//                "我的", R.mipmap.ic_service_private));

        //公共前台类目
        String publicCateCache = ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .getAsString(ACacheHelper.CK_PUBLIC_FRONT_CATEGORY);
        List<PosCategory> publicData = JSONArray.parseArray(publicCateCache, PosCategory.class);
        if (publicData != null && publicData.size() > 0){
            for (PosCategory category : publicData){
                functionalList.add(CashierFunctional.generate(category));
            }
        }
        //私有前台类目
        String customCateCache = ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .getAsString(ACacheHelper.CK_CUSTOM_FRONT_CATEGORY);
        List<PosCategory> customData = JSONArray.parseArray(customCateCache, PosCategory.class);
        if (customData != null && customData.size() > 0){
            for (PosCategory category : customData){
                functionalList.add(CashierFunctional.generate(category));
            }
        }
        return functionalList;
    }

}
