package com.bingshanguxue.pda.database.dao;

import com.bingshanguxue.pda.database.entity.InvRecvGoodsEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 收货单
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class InvRecvGoodsDao extends BaseSeqAbleDao<InvRecvGoodsEntity, String> {

    private static final String TABLE_NAME = "pda_invrecv_goods_v0002";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("收货单", TABLE_NAME);
    }

    @Override
    protected Class<InvRecvGoodsEntity> initPojoClass() {
        return InvRecvGoodsEntity.class;
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
    public List<InvRecvGoodsEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<InvRecvGoodsEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(InvRecvGoodsEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<InvRecvGoodsEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<InvRecvGoodsEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvRecvGoodsEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<InvRecvGoodsEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvRecvGoodsEntity.class, strWhere, "createdDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
