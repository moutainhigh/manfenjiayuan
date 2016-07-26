package com.bingshanguxue.pda.database.dao;

import com.bingshanguxue.pda.database.InvIoGoodsEntity;
import com.manfenjiayuan.pda_supermarket.database.entity.InvIoGoodsEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 出入库订单
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class InvIoGoodsDao extends BaseSeqAbleDao<InvIoGoodsEntity, String> {

    private static final String TABLE_NAME = "pda_supermarket_invio_goods_v1";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("出入库单", TABLE_NAME);
    }

    @Override
    protected Class<InvIoGoodsEntity> initPojoClass() {
        return InvIoGoodsEntity.class;
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
    public List<InvIoGoodsEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<InvIoGoodsEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(InvIoGoodsEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<InvIoGoodsEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<InvIoGoodsEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvIoGoodsEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<InvIoGoodsEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvIoGoodsEntity.class, strWhere, "createdDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
