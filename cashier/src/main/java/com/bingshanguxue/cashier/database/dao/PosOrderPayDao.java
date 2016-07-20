package com.bingshanguxue.cashier.database.dao;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;

import java.util.List;

/**
 * POS--订单支付流水
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosOrderPayDao extends BaseSeqAbleDao<PosOrderPayEntity, String> {

    private static final String TABLE_NAME = "tb_pos_order_pay_v2";


    public static final String ORDER_BY_UPDATEDDATE_ASC = "updatedDate asc";//升序
    public static final String ORDER_BY_UPDATEDATE_DESC = "updatedDate desc";
    public static final String ORDER_BY_CREATEDDATE_ASC = "createdDate asc";
    public static final String ORDER_BY_CREATEDDATE_DESC = "createdDate desc";


    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("收银订单支付记录", TABLE_NAME);
    }

    @Override
    protected Class<PosOrderPayEntity> initPojoClass() {
        return PosOrderPayEntity.class;
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
    public List<PosOrderPayEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<PosOrderPayEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(PosOrderPayEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }
    public List<PosOrderPayEntity> queryAll() {
        return queryAllBy(null);
    }
    public List<PosOrderPayEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosOrderPayEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }
    public List<PosOrderPayEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getFinalDb().findAllByWhere(PosOrderPayEntity.class, strWhere, orderBy);
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PosOrderPayEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosOrderPayEntity.class, strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }
}
