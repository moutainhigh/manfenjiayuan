package com.mfh.litecashier.database.dao;

import com.mfh.litecashier.database.entity.PosCategoryEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * POS--POS前台类目
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosCategoryDao extends BaseSeqAbleDao<PosCategoryEntity, String> {

    private static final String TABLE_NAME = "tb_pos_category";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("POS前台类目", TABLE_NAME);
    }

    @Override
    protected Class<PosCategoryEntity> initPojoClass() {
        return PosCategoryEntity.class;
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
    public List<PosCategoryEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<PosCategoryEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(PosCategoryEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<PosCategoryEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<PosCategoryEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosCategoryEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
