package com.bingshanguxue.pda.database.dao;

import com.bingshanguxue.pda.database.entity.InvCheckGoodsEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 盘点记录
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class InvCheckGoodsDao extends BaseSeqAbleDao<InvCheckGoodsEntity, String> {

    private static final String TABLE_NAME = "pda_invcheck_goods_v1";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("盘点记录", TABLE_NAME);
    }

    @Override
    protected Class<InvCheckGoodsEntity> initPojoClass() {
        return InvCheckGoodsEntity.class;
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
    public List<InvCheckGoodsEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<InvCheckGoodsEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(InvCheckGoodsEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<InvCheckGoodsEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<InvCheckGoodsEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvCheckGoodsEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }
    public List<InvCheckGoodsEntity> queryAllBy(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(InvCheckGoodsEntity.class, strWhere, "createdDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
    public List<InvCheckGoodsEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvCheckGoodsEntity.class, strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
