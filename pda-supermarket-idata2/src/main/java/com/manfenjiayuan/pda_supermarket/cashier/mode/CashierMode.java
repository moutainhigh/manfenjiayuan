package com.manfenjiayuan.pda_supermarket.cashier.mode;


import com.manfenjiayuan.pda_supermarket.cashier.database.entity.PosProductEntity;
import com.manfenjiayuan.pda_supermarket.cashier.database.service.PosProductService;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.List;

/**
 *
 * Created by bingshanguxue on 16/3/17.
 */
public class CashierMode {
    /**
     * 查询本地商品库搜索商品
     * @param barcode 商品条码
     * @return PosProductEntity 如果找到多个返回第一个商品；没有找到返回null.
     * */
    public PosProductEntity findGoods(String barcode) {
        //注意，这里的租户默认是当前登录租户
        List<PosProductEntity> entities = PosProductService.get()
                .queryAllByDesc(String.format("barcode = '%s' and tenantId = '%d'",
                        barcode, MfhLoginService.get().getSpid()));
        if (entities != null && entities.size() > 0) {
            PosProductEntity goods = entities.get(0);
            ZLogger.d(String.format("找到%d个商品:%s[%s][%s]",
                    entities.size(), barcode, goods.getAbbreviation(), goods.getSkuName()));
            return goods;
        }
        else{
            ZLogger.d(String.format("未找到商品:%s", barcode));
        }

        return null;
    }
}
