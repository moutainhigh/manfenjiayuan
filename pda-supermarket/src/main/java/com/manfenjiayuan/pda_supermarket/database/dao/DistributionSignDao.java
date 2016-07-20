package com.manfenjiayuan.pda_supermarket.database.dao;

import com.manfenjiayuan.pda_supermarket.database.entity.DistributionSignEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 商品配送－－签收
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class DistributionSignDao extends BaseSeqAbleDao<DistributionSignEntity, String> {

    private static final String TABLE_NAME = "pda_supermarket_distribution_sign_v002";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("收货", TABLE_NAME);
    }

    @Override
    protected Class<DistributionSignEntity> initPojoClass() {
        return DistributionSignEntity.class;
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
    public List<DistributionSignEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<DistributionSignEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(DistributionSignEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<DistributionSignEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<DistributionSignEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(DistributionSignEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<DistributionSignEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(DistributionSignEntity.class, strWhere, "createdDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
