package com.mfh.litecashier.database.dao;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;
import com.mfh.litecashier.database.entity.PosOrderEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;

import java.util.List;

/**
 * POS--订单销售流水
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosOrderDao extends BaseSeqAbleDao<PosOrderEntity, String> {

    private static final String TABLE_NAME = "tb_pos_order_v2";

    public static final String ORDER_BY_UPDATEDDATE_ASC = "updatedDate asc";//升序
    public static final String ORDER_BY_UPDATEDATE_DESC = "updatedDate desc";
    public static final String ORDER_BY_CREATEDDATE_ASC = "createdDate asc";
    public static final String ORDER_BY_CREATEDDATE_DESC = "createdDate desc";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("POS销售订单流水", TABLE_NAME);
    }

    @Override
    protected Class<PosOrderEntity> initPojoClass() {
        return PosOrderEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    @Override
    public String getSequeceName() {
        return "tb_pos_order_v2";
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     *
     * @param pageInfo
     * @return
     */
    public List<PosOrderEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<PosOrderEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(PosOrderEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<PosOrderEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(PosOrderEntity.class, strWhere, "updatedDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PosOrderEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(PosOrderEntity.class, strWhere, "updatedDate desc", pageInfo);//降序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PosOrderEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<PosOrderEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosOrderEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PosOrderEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getFinalDb().findAllByWhere(PosOrderEntity.class, strWhere, orderBy);
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PosOrderEntity> syncQueryBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosOrderEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

}
