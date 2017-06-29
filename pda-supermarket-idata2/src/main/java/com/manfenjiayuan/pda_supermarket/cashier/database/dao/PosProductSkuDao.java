package com.manfenjiayuan.pda_supermarket.cashier.database.dao;

import com.manfenjiayuan.pda_supermarket.cashier.database.entity.PosProductSkuEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseDbDao;

import java.util.List;

/**
 * POS--箱规商品库
 * 主键由后台数据提供
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosProductSkuDao extends BaseDbDao<PosProductSkuEntity, String> {

    private static final String TABLE_NAME = "tb_pos_procuct_sku";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("POS箱规商品库", TABLE_NAME);
    }

    @Override
    protected Class<PosProductSkuEntity> initPojoClass() {
        return PosProductSkuEntity.class;
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
    public List<PosProductSkuEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<PosProductSkuEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(PosProductSkuEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<PosProductSkuEntity> queryAll() {
        return queryAllBy(null);
    }
    public List<PosProductSkuEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosProductSkuEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<PosProductSkuEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(PosProductSkuEntity.class, strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
