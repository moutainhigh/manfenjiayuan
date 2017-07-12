package com.bingshanguxue.cashier.presenter;

import com.bingshanguxue.cashier.view.ICashierView;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.entity.PosProductSkuEntity;
import com.bingshanguxue.cashier.database.service.PosProductSkuService;
import com.bingshanguxue.cashier.mode.CashierMode;
import com.manfenjiayuan.business.utils.BarcodeUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;

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
        //生鲜商品条码是以'2'开头并且是13位，F CCCCCC XXXXX CD
        if (BarcodeUtils.getType(barcode) == BarcodeUtils.BARCODE_DIGI) {
            findFreshGoods(barcode);
        } else {
            findNormalGoods(barcode);
        }
    }

    /**
     * 查询商品
     *
     * @param barcode 商品条码
     */
    private void findNormalGoods(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            ZLogger.d("商品条码无效");
            return;
        }

        ZLogger.d(String.format("搜索标准商品 条码：%s", barcode));

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
                ZLogger.d(String.format("找到%d个主条码%s", entityList.size(), mainBarcode));

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
     */
    private void findFreshGoods(String barcode) {
        if (StringUtils.isEmpty(barcode) || barcode.length() != 13) {
            ZLogger.d("参数无效");
            return;
        }

        try {
            String plu = barcode.substring(1, 7);
//            String plu = BarcodeUtils.getDigiPlu(barcode);
            //有小数点，单位克转换成千克。
            String weightStr = String.format("%s.%s", barcode.substring(7, 9), barcode.substring(9, 12));
            Double weight = Double.valueOf(weightStr);
            ZLogger.d(String.format("搜索生鲜商品 条码：%s, PLU码：%s, 重量：%f",
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
                    ZLogger.d(String.format("找到%d个主条码%s", entityList.size(), mainBarcode));

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
        } catch (Exception e) {
            ZLogger.e(e.toString());
            if (iCashierView != null) {
                iCashierView.onFindGoodsEmpty(barcode);
            }
        }
    }


}
