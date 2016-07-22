package com.mfh.litecashier.database.dao;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;
import com.mfh.litecashier.database.entity.PurchaseGoodsEntity;

import java.util.List;

/**
 * 采购订单购物车商品明细
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PurchaseGoodsDao extends BaseSeqAbleDao<PurchaseGoodsEntity, String> {

    private static final String TABLE_NAME = "tb_purchase_goods_v0100";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("采购订单购物车商品明细", TABLE_NAME);
    }

    @Override
    protected Class<PurchaseGoodsEntity> initPojoClass() {
        return PurchaseGoodsEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    @Override
    public String getSequeceName() {
        return "tb_purchase_goods_v0100";
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     *
     * @param pageInfo
     * @return
     */
    public List<PurchaseGoodsEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<PurchaseGoodsEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(PurchaseGoodsEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<PurchaseGoodsEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(PurchaseGoodsEntity.class, strWhere, "updatedDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PurchaseGoodsEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(PurchaseGoodsEntity.class, strWhere, "updatedDate desc", pageInfo);//降序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PurchaseGoodsEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<PurchaseGoodsEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PurchaseGoodsEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PurchaseGoodsEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getFinalDb().findAllByWhere(PurchaseGoodsEntity.class, strWhere, orderBy);
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PurchaseGoodsEntity> syncQueryBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PurchaseGoodsEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
}
