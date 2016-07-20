package com.mfh.litecashier.database.dao;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;
import com.mfh.litecashier.database.entity.PurchaseShopcartEntity;

import java.util.List;

/**
 * 采购订单购物车商品明细
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PurchaseShopcartDao extends BaseSeqAbleDao<PurchaseShopcartEntity, String> {

    private static final String TABLE_NAME = "tb_purchase_shopcart_v02";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("采购订单购物车商品明细", TABLE_NAME);
    }

    @Override
    protected Class<PurchaseShopcartEntity> initPojoClass() {
        return PurchaseShopcartEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    @Override
    public String getSequeceName() {
        return "tb_purchase_shopcart";
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     *
     * @param pageInfo
     * @return
     */
    public List<PurchaseShopcartEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<PurchaseShopcartEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(PurchaseShopcartEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<PurchaseShopcartEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(PurchaseShopcartEntity.class, strWhere, "updatedDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PurchaseShopcartEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(PurchaseShopcartEntity.class, strWhere, "updatedDate desc", pageInfo);//降序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PurchaseShopcartEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<PurchaseShopcartEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PurchaseShopcartEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PurchaseShopcartEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getFinalDb().findAllByWhere(PurchaseShopcartEntity.class, strWhere, orderBy);
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PurchaseShopcartEntity> syncQueryBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PurchaseShopcartEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
}
