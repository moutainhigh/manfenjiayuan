package com.mfh.litecashier.database.dao;

import com.mfh.framework.database.dao.BaseDbDao;
import com.mfh.litecashier.database.entity.CommonlyGoodsEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;

import java.util.List;

/**
 * POS--常用商品
 * 主键由后台数据提供
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class CommonlyGoodsDao extends BaseDbDao<CommonlyGoodsEntity, String> {

    private static final String TABLE_NAME = "tb_pos_procuct_commonly_2";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("常卖商品", TABLE_NAME);
    }

    @Override
    protected Class<CommonlyGoodsEntity> initPojoClass() {
        return CommonlyGoodsEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     * @param pageInfo
     * @return List<CommonlyGoodsEntity>
     */
    public List<CommonlyGoodsEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<CommonlyGoodsEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(CommonlyGoodsEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<CommonlyGoodsEntity> queryAll() {
        return queryAllBy(null);
    }
    public List<CommonlyGoodsEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(CommonlyGoodsEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<CommonlyGoodsEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(CommonlyGoodsEntity.class, strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
