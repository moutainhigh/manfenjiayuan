package com.mfh.litecashier.database.dao;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;
import com.mfh.litecashier.database.entity.DailysettleEntity;

import java.util.List;

/**
 * <h1>POS--日结</h1><br>
 * <p>
 *
 * </p>
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class DailysettleDao extends BaseSeqAbleDao<DailysettleEntity, String> {

    private static final String TABLE_NAME = "tb_pos_dailysettle_v0";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("POS--日结", TABLE_NAME);
    }

    @Override
    protected Class<DailysettleEntity> initPojoClass() {
        return DailysettleEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }


    //和PosOrderDao使用同一个序列
    @Override
    public String getSequeceName() {
        return "tb_pos_order_v2";
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     * @param pageInfo
     * @return
     */
    public List<DailysettleEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<DailysettleEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(DailysettleEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }
    public List<DailysettleEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(DailysettleEntity.class, strWhere, "updatedDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }public List<DailysettleEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(DailysettleEntity.class, strWhere, "updatedDate desc", pageInfo);//降序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
    public List<DailysettleEntity> queryAll() {
        return queryAllBy(null);
    }
    public List<DailysettleEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(DailysettleEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
    public List<DailysettleEntity> syncQueryBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(DailysettleEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
}
