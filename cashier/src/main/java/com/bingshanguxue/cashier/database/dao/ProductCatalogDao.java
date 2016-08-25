package com.bingshanguxue.cashier.database.dao;

import com.bingshanguxue.cashier.database.entity.ProductCatalogEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 类目商品关系表
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class ProductCatalogDao extends BaseSeqAbleDao<ProductCatalogEntity, String> {

    private static final String TABLE_NAME = "tb_product_catalog_v001";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("类目商品关系表", TABLE_NAME);
    }

    @Override
    protected Class<ProductCatalogEntity> initPojoClass() {
        return ProductCatalogEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     *
     * @param pageInfo
     * @return
     */
    public List<ProductCatalogEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<ProductCatalogEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(ProductCatalogEntity.class,
                strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<ProductCatalogEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(ProductCatalogEntity.class,
                    strWhere, "updatedDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<ProductCatalogEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(ProductCatalogEntity.class,
                    strWhere, "updatedDate desc", pageInfo);//降序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<ProductCatalogEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<ProductCatalogEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(ProductCatalogEntity.class,
                    strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<ProductCatalogEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getFinalDb().findAllByWhere(ProductCatalogEntity.class, strWhere, orderBy);
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<ProductCatalogEntity> syncQueryBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(ProductCatalogEntity.class,
                    strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
}
