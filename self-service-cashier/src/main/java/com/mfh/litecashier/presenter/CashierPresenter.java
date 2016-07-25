package com.mfh.litecashier.presenter;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.CashierFunctional;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.entity.PosProductSkuEntity;
import com.mfh.litecashier.database.logic.PosProductSkuService;
import com.bingshanguxue.cashier.mode.CashierMode;
import com.mfh.litecashier.ui.view.ICashierView;

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
     */
    public synchronized void findGoods(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            ZLogger.d("商品条码无效");
            return;
        }

        //生鲜商品条码是以'2'开头并且是13位，F CCCCCC XXXXX CD
        if (barcode.startsWith("2") && barcode.length() == 13){
            findFreshGoods(barcode);
            return;
        }
        ZLogger.df(String.format("搜索生鲜商品 条码：%s", barcode));

        int packFlag = 0;//是否是箱规：0不是；1是
        //Step 1:查询商品
        PosProductEntity entity = this.iCashierMode.findGoods(barcode);
        if (entity == null) {
            // Step 2: 查询主条码
            List<PosProductSkuEntity> entityList = PosProductSkuService.get()
                    .queryAllByDesc(String.format("otherBarcode = '%s'", barcode));
            if (entityList != null && entityList.size() > 0) {
                PosProductSkuEntity posProductSkuEntity = entityList.get(0);
                String mainBarcode = posProductSkuEntity.getMainBarcode();
                packFlag = posProductSkuEntity.getPackFlag();
                ZLogger.df(String.format("找到%d个主条码%s", entityList.size(), mainBarcode));

                //Step 3:根据主条码再次查询商品
                entity = this.iCashierMode.findGoods(mainBarcode);
            }
        }

        if (entity != null) {
            //Step 4:找到商品
            if (iCashierView != null) {
                iCashierView.onFindGoods(entity, packFlag);
            }
        } else {
            //Step 5:未找到商品
            if (iCashierView != null) {
                iCashierView.onFindGoodsEmpty(barcode);
            }
        }
    }

    /**
     * 生鲜商品电子秤打印条码是以2开头的13位码：F CCCCCC XXXXX CD(13)
     * */
    private void findFreshGoods(String barcode){
        if (StringUtils.isEmpty(barcode) || barcode.length() != 13) {
            ZLogger.d("参数无效");
            return;
        }

        try{
            String plu = barcode.substring(1, 7);
            String weightStr = String.format("%s.%s", barcode.substring(7, 9), barcode.substring(9, 12));
            Double weight = Double.valueOf(weightStr);
            ZLogger.df(String.format("搜索生鲜商品 条码：%s, PLU码：%s, 重量：%f",
                    barcode, plu, weight));

            int packFlag = 0;//是否是箱规：0不是；1是
            //Step 1:查询商品
            PosProductEntity entity = this.iCashierMode.findGoods(plu);
            if (entity == null) {
                // Step 2: 查询主条码
                List<PosProductSkuEntity> entityList = PosProductSkuService.get()
                        .queryAllByDesc(String.format("otherBarcode = '%s'", plu));
                if (entityList != null && entityList.size() > 0) {
                    PosProductSkuEntity posProductSkuEntity = entityList.get(0);
                    String mainBarcode = posProductSkuEntity.getMainBarcode();
                    packFlag = posProductSkuEntity.getPackFlag();
                    ZLogger.df(String.format("找到%d个主条码%s", entityList.size(), mainBarcode));

                    //Step 3:根据主条码再次查询商品
                    entity = this.iCashierMode.findGoods(mainBarcode);
                }
            }

            if (entity != null) {
                //Step 4:找到商品
                if (iCashierView != null) {
                    iCashierView.onFindFreshGoods(entity, weight);
                }
            } else {
                //Step 5:未找到商品
                if (iCashierView != null) {
                    iCashierView.onFindGoodsEmpty(barcode);
                }
            }
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            if (iCashierView != null) {
                iCashierView.onFindGoodsEmpty(barcode);
            }
        }
    }

    /**
     * 加载收银机前台类目：私有功能＋公共类目＋自定义类目
     */
    public synchronized List<CashierFunctional> getCashierFunctions() {
        List<CashierFunctional> functionalList = new ArrayList<>();
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_ONLINE_ORDER,
                "线上订单", R.mipmap.ic_service_online_order));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_GOODS_LIST,
                "商品列表", R.mipmap.ic_service_goodslist));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_REGISTER_VIP,
                "注册", R.mipmap.ic_service_register_vip));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_MEMBER_CARD,
                "办卡", R.mipmap.ic_service_membercard));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_HANGUP_ORDER,
                "挂单", R.mipmap.ic_service_hangup_order));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_RETURN_GOODS,
                "退货", R.mipmap.ic_service_returngoods));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_FEEDPAPER,
                "走纸", R.mipmap.ic_service_feedpaper));
//        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PACKAGE,
//                "包裹", R.mipmap.ic_service_package));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_MONEYBOX,
                "钱箱", R.mipmap.ic_service_moneybox));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_BALANCE_QUERY,
                "余额查询", R.mipmap.ic_service_balance));
        functionalList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_SETTINGS,
                "设置", R.mipmap.ic_service_settings));

        return functionalList;
    }

    /**
     * 加载收银机前台类目：私有功能＋公共类目＋自定义类目
     */
    public synchronized List<CashierFunctional> getGrouponList() {
        List<CashierFunctional> grouponList = new ArrayList<>();

        for (int i= 0; i<4; i++){
            grouponList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_SYNC,
                    "同步", R.mipmap.ic_groupon_2_001));
            grouponList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_SYNC,
                    "同步", R.mipmap.ic_groupon_2_002));
            grouponList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_SYNC,
                    "同步", R.mipmap.ic_groupon_2_003));
            grouponList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_SYNC,
                    "同步", R.mipmap.ic_groupon_2_004));
            grouponList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_SYNC,
                    "同步", R.mipmap.ic_groupon_2_001));
            grouponList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_SYNC,
                    "同步", R.mipmap.ic_groupon_2_002));
            grouponList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_SYNC,
                    "同步", R.mipmap.ic_groupon_2_003));
            grouponList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_SYNC,
                    "同步", R.mipmap.ic_groupon_2_004));
            grouponList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_SYNC,
                    "注册", R.mipmap.ic_adv_beef));
        }

        //公共前台类目
//        String publicCateCache = ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
//                .getAsString(ACacheHelper.CK_PUBLIC_FRONT_CATEGORY);
//        List<PosCategory> publicData = JSONArray.parseArray(publicCateCache, PosCategory.class);
//        if (publicData != null && publicData.size() > 0) {
//            for (PosCategory category : publicData) {
//                grouponList.add(CashierFunctional.generate(category));
//            }
//        }
//        //私有前台类目
//        String customCateCache = ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
//                .getAsString(ACacheHelper.CK_CUSTOM_FRONT_CATEGORY);
//        List<PosCategory> customData = JSONArray.parseArray(customCateCache, PosCategory.class);
//        if (customData != null && customData.size() > 0) {
//            for (PosCategory category : customData) {
//                grouponList.add(CashierFunctional.generate(category));
//            }
//        }
        return grouponList;
    }

}
