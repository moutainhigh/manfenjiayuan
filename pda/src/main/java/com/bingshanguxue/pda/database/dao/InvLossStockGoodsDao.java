package com.bingshanguxue.pda.database.dao;

import com.bingshanguxue.pda.database.entity.InvLossStockGoodsEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 报损订单明细
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class InvLossStockGoodsDao extends BaseSeqAbleDao<InvLossStockGoodsEntity, String> {

    private static final String TABLE_NAME = "pda_invloss_stock_goods_v0001";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("报损盘点明细", TABLE_NAME);
    }

    @Override
    protected Class<InvLossStockGoodsEntity> initPojoClass() {
        return InvLossStockGoodsEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     * @param pageInfo 翻页信息
     * @return List<InvLossGoodsEntity>
     */
    public List<InvLossStockGoodsEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<InvLossStockGoodsEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(InvLossStockGoodsEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<InvLossStockGoodsEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<InvLossStockGoodsEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvLossStockGoodsEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<InvLossStockGoodsEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvLossStockGoodsEntity.class, strWhere, "createdDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
