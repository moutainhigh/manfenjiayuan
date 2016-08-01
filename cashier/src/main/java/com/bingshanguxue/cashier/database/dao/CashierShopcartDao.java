package com.bingshanguxue.cashier.database.dao;

import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * POS--收银台／购物车商品
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class CashierShopcartDao extends BaseSeqAbleDao<CashierShopcartEntity, String> {

    private static final String TABLE_NAME = "tb_cashier_shopcart_v01001";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("收银台／购物车商品", TABLE_NAME);
    }

    @Override
    protected Class<CashierShopcartEntity> initPojoClass() {
        return CashierShopcartEntity.class;
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
    public List<CashierShopcartEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<CashierShopcartEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(CashierShopcartEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }
    public List<CashierShopcartEntity> queryAll() {
        return queryAllBy(null);
    }
    public List<CashierShopcartEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(CashierShopcartEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.ef(ex.toString());

            return null;
        }
    }

    public List<CashierShopcartEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(CashierShopcartEntity.class,
                    strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.ef(ex.toString());

            return null;
        }
    }

}
