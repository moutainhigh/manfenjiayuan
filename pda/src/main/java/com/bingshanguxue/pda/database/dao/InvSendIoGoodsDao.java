package com.bingshanguxue.pda.database.dao;

import com.bingshanguxue.pda.database.entity.InvSendIoGoodsEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 发货拣货商品
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class InvSendIoGoodsDao extends BaseSeqAbleDao<InvSendIoGoodsEntity, String> {

    private static final String TABLE_NAME = "pda_invsendio_goods_v0001";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("发货", TABLE_NAME);
    }

    @Override
    protected Class<InvSendIoGoodsEntity> initPojoClass() {
        return InvSendIoGoodsEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     *
     * @param pageInfo
     * @return List<InvSendIoGoodsEntity>
     */
    public List<InvSendIoGoodsEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<InvSendIoGoodsEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(InvSendIoGoodsEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<InvSendIoGoodsEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<InvSendIoGoodsEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvSendIoGoodsEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<InvSendIoGoodsEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvSendIoGoodsEntity.class, strWhere, "createdDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public void deleteBy(String strWhere) {
        getFinalDb().deleteByWhere(this.pojoClass, strWhere);
    }
}
