package com.manfenjiayuan.pda_supermarket.database.dao;

import com.manfenjiayuan.pda_supermarket.database.entity.InstockTempEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 骑手妥投
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class InstockTempDao extends BaseSeqAbleDao<InstockTempEntity, String> {

    private static final String TABLE_NAME = "tb_instock_0003";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("骑手妥投", TABLE_NAME);
    }

    @Override
    protected Class<InstockTempEntity> initPojoClass() {
        return InstockTempEntity.class;
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
    public List<InstockTempEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<InstockTempEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(InstockTempEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<InstockTempEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<InstockTempEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InstockTempEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }
    public List<InstockTempEntity> queryAllBy(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(InstockTempEntity.class, strWhere, "createdDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
    public List<InstockTempEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(InstockTempEntity.class, strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
