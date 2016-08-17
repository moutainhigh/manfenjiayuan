package com.mfh.litecashier.database.dao;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;
import com.mfh.litecashier.database.entity.PosCategoryGoodsTempEntity;

import java.util.List;

/**
 * 采购订单购物车商品明细
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosCatetoryGoodsTempDao extends BaseSeqAbleDao<PosCategoryGoodsTempEntity, String> {

    private static final String TABLE_NAME = "tb_category_goods_temp_v1";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("POS类目选择商品", TABLE_NAME);
    }

    @Override
    protected Class<PosCategoryGoodsTempEntity> initPojoClass() {
        return PosCategoryGoodsTempEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     *
     * @param pageInfo
     * @return
     */
    public List<PosCategoryGoodsTempEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<PosCategoryGoodsTempEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(PosCategoryGoodsTempEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<PosCategoryGoodsTempEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(PosCategoryGoodsTempEntity.class, strWhere, "updatedDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PosCategoryGoodsTempEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(PosCategoryGoodsTempEntity.class, strWhere, "updatedDate desc", pageInfo);//降序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PosCategoryGoodsTempEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<PosCategoryGoodsTempEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosCategoryGoodsTempEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PosCategoryGoodsTempEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getFinalDb().findAllByWhere(PosCategoryGoodsTempEntity.class, strWhere, orderBy);
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<PosCategoryGoodsTempEntity> syncQueryBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosCategoryGoodsTempEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
}
