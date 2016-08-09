package com.bingshanguxue.cashier.database.dao;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseDbDao;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;

import java.util.List;

/**
 * POS--商品库
 * 主键由后台数据提供
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosProductDao extends BaseDbDao<PosProductEntity, String> {

    private static final String TABLE_NAME = "tb_pos_procuct_v1";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("POS商品库", TABLE_NAME);
    }

    @Override
    protected Class<PosProductEntity> initPojoClass() {
        return PosProductEntity.class;
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
    public List<PosProductEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(PosProductEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<PosProductEntity> queryAll() {
        return queryAllBy(null);
    }
    public List<PosProductEntity> queryAllBy(String strWhere, String orderBy, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(PosProductEntity.class, strWhere, orderBy, pageInfo);
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<PosProductEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosProductEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<PosProductEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(PosProductEntity.class, strWhere, "updatedDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PosProductEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosProductEntity.class, strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
