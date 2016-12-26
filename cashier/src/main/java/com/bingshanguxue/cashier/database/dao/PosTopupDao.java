package com.bingshanguxue.cashier.database.dao;

import com.bingshanguxue.cashier.database.entity.PosTopupEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * POS充值记录
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosTopupDao extends BaseSeqAbleDao<PosTopupEntity, String> {

    private static final String TABLE_NAME = "tb_pos_topup_v1";

    public static final String ORDER_BY_UPDATEDDATE_ASC = "updatedDate asc";//升序
    public static final String ORDER_BY_UPDATEDATE_DESC = "updatedDate desc";
    public static final String ORDER_BY_CREATEDDATE_ASC = "createdDate asc";
    public static final String ORDER_BY_CREATEDDATE_DESC = "createdDate desc";


    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("POS充值记录", TABLE_NAME);
    }

    @Override
    protected Class<PosTopupEntity> initPojoClass() {
        return PosTopupEntity.class;
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
    public List<PosTopupEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<PosTopupEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(PosTopupEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }
    public List<PosTopupEntity> queryAll() {
        return queryAllBy(null);
    }
    public List<PosTopupEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosTopupEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }
    public List<PosTopupEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getFinalDb().findAllByWhere(PosTopupEntity.class, strWhere, orderBy);
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PosTopupEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosTopupEntity.class, strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }
}
