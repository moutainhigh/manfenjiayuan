package com.mfh.litecashier.mode;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.PosProductService;

import java.util.List;

/**
 * 库存商品：库存成本，批次流水，库存调拨
 * Created by bingshanguxue on 16/3/17.
 */
public class CashierMode implements ICashierMode {


    @Override
    public PosProductEntity findGoods(String barcode) {
        List<PosProductEntity> entities = PosProductService.get()
                .queryAllByDesc(String.format("barcode = '%s' and tenantId = '%d'",
                        barcode, MfhLoginService.get().getSpid()));
        if (entities != null && entities.size() > 0) {
            ZLogger.df(String.format("找到%d个商品:%s", entities.size(), barcode));
            return entities.get(0);
        }
        else{
            ZLogger.df(String.format("未找到商品:%s", barcode));
        }

        return null;
    }

    @Override
    public String findMainBarcode(String otherBarcode) {
        return null;
    }
}
