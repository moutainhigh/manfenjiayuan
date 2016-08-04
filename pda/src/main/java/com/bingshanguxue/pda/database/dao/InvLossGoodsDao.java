package com.bingshanguxue.pda.database.dao;

import com.bingshanguxue.pda.database.entity.InvLossGoodsEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 报损订单明细
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class InvLossGoodsDao extends BaseSeqAbleDao<InvLossGoodsEntity, String> {

    private static final String TABLE_NAME = "pda_invloss_goods_v1";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("报损订单明细", TABLE_NAME);
    }

    @Override
    protected Class<InvLossGoodsEntity> initPojoClass() {
        return InvLossGoodsEntity.class;
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
    public List<InvLossGoodsEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<InvLossGoodsEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(InvLossGoodsEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<InvLossGoodsEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<InvLossGoodsEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvLossGoodsEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<InvLossGoodsEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvLossGoodsEntity.class, strWhere, "createdDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
