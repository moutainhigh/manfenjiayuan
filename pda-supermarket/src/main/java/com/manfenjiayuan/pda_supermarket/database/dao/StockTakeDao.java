package com.manfenjiayuan.pda_supermarket.database.dao;

import com.manfenjiayuan.pda_supermarket.database.entity.StockTakeEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 盘点记录
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class StockTakeDao extends BaseSeqAbleDao<StockTakeEntity, String> {

    private static final String TABLE_NAME = "pda_supermarket_stocktakeitem_v3";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("盘点记录", TABLE_NAME);
    }

    @Override
    protected Class<StockTakeEntity> initPojoClass() {
        return StockTakeEntity.class;
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
    public List<StockTakeEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<StockTakeEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(StockTakeEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<StockTakeEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<StockTakeEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(StockTakeEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }
    public List<StockTakeEntity> queryAllBy(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(StockTakeEntity.class, strWhere, "createdDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
    public List<StockTakeEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(StockTakeEntity.class, strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
