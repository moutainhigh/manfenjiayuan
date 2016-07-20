package com.manfenjiayuan.pda_supermarket.database.dao;

import com.manfenjiayuan.pda_supermarket.database.entity.ShelveEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 绑定货架
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class ShelveDao extends BaseSeqAbleDao<ShelveEntity, String> {

    private static final String TABLE_NAME = "sheleve_v1";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("商品绑定货架", TABLE_NAME);
    }

    @Override
    protected Class<ShelveEntity> initPojoClass() {
        return ShelveEntity.class;
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
    public List<ShelveEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<ShelveEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(ShelveEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<ShelveEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<ShelveEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(ShelveEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }
    public List<ShelveEntity> queryAllBy(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(ShelveEntity.class, strWhere, "createdDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
    public List<ShelveEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(ShelveEntity.class, strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
