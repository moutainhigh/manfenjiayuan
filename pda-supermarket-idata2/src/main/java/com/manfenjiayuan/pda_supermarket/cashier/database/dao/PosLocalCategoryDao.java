package com.manfenjiayuan.pda_supermarket.cashier.database.dao;

import com.manfenjiayuan.pda_supermarket.cashier.database.entity.PosLocalCategoryEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * POS本地类目
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosLocalCategoryDao extends BaseSeqAbleDao<PosLocalCategoryEntity, String> {

    private static final String TABLE_NAME = "tb_pos_local_category_v0001";


    public static final String ORDER_BY_UPDATEDDATE_ASC = "updatedDate asc";//升序
    public static final String ORDER_BY_UPDATEDATE_DESC = "updatedDate desc";
    public static final String ORDER_BY_CREATEDDATE_ASC = "createdDate asc";
    public static final String ORDER_BY_CREATEDDATE_DESC = "createdDate desc";


    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("POS本地类目", TABLE_NAME);
    }

    @Override
    protected Class<PosLocalCategoryEntity> initPojoClass() {
        return PosLocalCategoryEntity.class;
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
    public List<PosLocalCategoryEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(PosLocalCategoryEntity.class,
                strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }
    public List<PosLocalCategoryEntity> queryAll() {
        return queryAllBy(null, null);
    }

    public List<PosLocalCategoryEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getFinalDb().findAllByWhere(PosLocalCategoryEntity.class, strWhere, orderBy);
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PosLocalCategoryEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosLocalCategoryEntity.class, strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }
}
