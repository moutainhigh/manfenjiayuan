package com.bingshanguxue.cashier.database.dao;

import com.mfh.framework.database.dao.BaseSeqAbleDao;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;

import java.util.List;

/**
 * POS--销售订单明细
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosOrderItemDao extends BaseSeqAbleDao<PosOrderItemEntity, String> {

    private static final String TABLE_NAME = "tb_pos_order_item_2";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("销售订单明细", TABLE_NAME);
    }

    @Override
    protected Class<PosOrderItemEntity> initPojoClass() {
        return PosOrderItemEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     * @param pageInfo
     * @return
     */
    public List<PosOrderItemEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<PosOrderItemEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(PosOrderItemEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }
    public List<PosOrderItemEntity> queryAll() {
        return queryAllBy(null);
    }
    public List<PosOrderItemEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosOrderItemEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<PosOrderItemEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosOrderItemEntity.class, strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
