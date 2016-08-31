package com.mfh.litecashier.database.dao;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;
import com.mfh.litecashier.database.entity.PurchaseOrderEntity;

import java.util.List;

/**
 * 采购订单
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PurchaseOrderDao extends BaseSeqAbleDao<PurchaseOrderEntity, String> {

    private static final String TABLE_NAME = "tb_purchase_order_v0100";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("采购订单", TABLE_NAME);
    }

    @Override
    protected Class<PurchaseOrderEntity> initPojoClass() {
        return PurchaseOrderEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    @Override
    public String getSequeceName() {
        return "tb_purchase_order_v0100";
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     *
     * @param pageInfo
     * @return
     */
    public List<PurchaseOrderEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<PurchaseOrderEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(PurchaseOrderEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<PurchaseOrderEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(PurchaseOrderEntity.class, strWhere, "updatedDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PurchaseOrderEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(PurchaseOrderEntity.class, strWhere, "updatedDate desc", pageInfo);//降序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PurchaseOrderEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<PurchaseOrderEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PurchaseOrderEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PurchaseOrderEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getFinalDb().findAllByWhere(PurchaseOrderEntity.class, strWhere, orderBy);
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PurchaseOrderEntity> syncQueryBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PurchaseOrderEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
}
