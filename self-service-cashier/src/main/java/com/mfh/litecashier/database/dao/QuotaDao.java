package com.mfh.litecashier.database.dao;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;
import com.mfh.litecashier.database.entity.QuotaEntity;

import java.util.List;

/**
 * 金额授权模式-现金额度
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class QuotaDao extends BaseSeqAbleDao<QuotaEntity, String> {

    private static final String TABLE_NAME = "tb_quota";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("金额授权模式-现金额度", TABLE_NAME);
    }

    @Override
    protected Class<QuotaEntity> initPojoClass() {
        return QuotaEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    //和PosOrderDao使用同一个序列
//    @Override
//    public String getSequeceName() {
//        return "tb_pos_order_v3";
//    }


    /**
     * 查询指定session下的消息类比，按照逆序
     * @param pageInfo
     * @return
     */
    public List<QuotaEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<QuotaEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(QuotaEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<QuotaEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<QuotaEntity> queryAllBy(String strWhere) {
        return queryAllBy(strWhere, "createdDate asc");
    }
    public List<QuotaEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getFinalDb().findAllByWhere(QuotaEntity.class, strWhere, orderBy);
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

}
