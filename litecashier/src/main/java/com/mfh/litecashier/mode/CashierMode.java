package com.mfh.litecashier.mode;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.database.entity.PosProductEntity;
import com.mfh.litecashier.database.logic.PosProductService;

import java.util.List;

/**
 * 库存商品：库存成本，批次流水，库存调拨
 * Created by bingshanguxue on 16/3/17.
 */
public class CashierMode implements ICashierMode {


    @Override
    public PosProductEntity findGoods(String barcode) {
        List<PosProductEntity> entityList = PosProductService.get()
                .queryAllByDesc(String.format("barcode = '%s' and tenantId = '%d'",
                        barcode, MfhLoginService.get().getSpid()));
        if (entityList != null && entityList.size() > 0) {
            ZLogger.d(String.format("找到%d个商品:%s", entityList.size(), barcode));
            return entityList.get(0);
        }

        ZLogger.d(String.format("找到%d个商品:%s", entityList.size(), barcode));
        return null;
    }

    @Override
    public String findMainBarcode(String otherBarcode) {
        return null;
    }
}
