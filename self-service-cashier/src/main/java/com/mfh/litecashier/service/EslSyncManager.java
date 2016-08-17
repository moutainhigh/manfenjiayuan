package com.mfh.litecashier.service;


import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.litecashier.hardware.GreenTags.GreenTagsApiImpl2;

import org.century.schemas.ArrayOfGoodsInfoEX;
import org.century.schemas.ArrayOfProperty;
import org.century.schemas.GoodsInfoEX;
import org.century.schemas.Property;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 绿泰价签数据同步管理
 * <ol>
 * 适用场景
 * <li>同步商品数据到绿泰价签管理后台</li>
 * </ol>
 * <p/>
 * <ol>
 * 同步策略
 * <li>全量更新，每次都会同步所有数据。</li>
 * <li>增量更新，每天首次开机全量同步一次（保证数据一致性）,之后仅同步有更新的数据。</li>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class EslSyncManager {
    //每次同步最大数量
    public static final int ESL_MAX_SYNC_PAGESIZE = 20;


    public GreenTagsApiImpl2.ESLPushGoodsInfoExPackResult makeEslPushRequest(List<PosProductEntity> goodsList, Date newCursor) {
        if (goodsList == null || goodsList.size() < 1) {
            return new GreenTagsApiImpl2.ESLPushGoodsInfoExPackResult(false);
        }

        ArrayOfGoodsInfoEX arrayOfGoodsInfoEX = new ArrayOfGoodsInfoEX();
        for (PosProductEntity goods : goodsList) {
            //记录最大时间游标
            if (newCursor == null || goods.getUpdatedDate() == null
                    || newCursor.compareTo(goods.getUpdatedDate()) <= 0) {
                newCursor = goods.getUpdatedDate();
            }

            arrayOfGoodsInfoEX.add(createFromPosProductEntity(goods));
        }
        try {
            return GreenTagsApiImpl2.ESLPushGoodsInfoExPack2(arrayOfGoodsInfoEX, newCursor);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return new GreenTagsApiImpl2.ESLPushGoodsInfoExPackResult(false);
        } catch (IOException e) {
            e.printStackTrace();
            return new GreenTagsApiImpl2.ESLPushGoodsInfoExPackResult(false);
        }
    }


    public static GoodsInfoEX createFromPosProductEntity(PosProductEntity goods) {
        GoodsInfoEX googsInfoEX = new GoodsInfoEX();
        googsInfoEX.setGoodsCode(goods.getBarcode());

        //更新商品属性
        ArrayOfProperty propertyList = new ArrayOfProperty();
        propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_GOODSCODE, goods.getBarcode()));
        propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_NAME, goods.getName()));
//        propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_ORIGIN, "米西厨房"));

        //2016-07-16 计重商品单位按“斤”显示，价格／2
        if (PriceType.WEIGHT.equals(goods.getPriceType())){
            propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_PRICE,
                    MUtils.formatDouble(goods.getCostPrice()/2, "0")));
            propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_UNIT, "斤"));
        }
        else{
            propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_PRICE,
                    MUtils.formatDouble(goods.getCostPrice(), "0")));
            propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_UNIT, goods.getUnit()));
        }

        googsInfoEX.setProperties(propertyList);

        return googsInfoEX;
    }

}
