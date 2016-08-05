package com.manfenjiayuan.pda_wholesaler.database.dao;

import com.manfenjiayuan.pda_wholesaler.database.entity.InvIoPickGoodsEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 发货拣货商品
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class InvIoPickGoodsDao extends BaseSeqAbleDao<InvIoPickGoodsEntity, String> {

    private static final String TABLE_NAME = "pda_wholesaler_invio_pickgoods";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("发货拣货商品", TABLE_NAME);
    }

    @Override
    protected Class<InvIoPickGoodsEntity> initPojoClass() {
        return InvIoPickGoodsEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     *
     * @param pageInfo
     * @return List<InvIoPickGoodsEntity>
     */
    public List<InvIoPickGoodsEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<InvIoPickGoodsEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(InvIoPickGoodsEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<InvIoPickGoodsEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<InvIoPickGoodsEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvIoPickGoodsEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<InvIoPickGoodsEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InvIoPickGoodsEntity.class, strWhere, "createdDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public void deleteBy(String strWhere) {
        getFinalDb().deleteByWhere(this.pojoClass, strWhere);
    }
}
