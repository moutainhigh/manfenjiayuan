package com.manfenjiayuan.mixicook_vip.database;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseSeqAbleDao;

import java.util.List;

/**
 * 首页商品
 * 主键：自增
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class HomeGoodsTempDao extends BaseSeqAbleDao<HomeGoodsTempEntity, String> {

    private static final String TABLE_NAME = "tb_homegoodstemp_v0001_t01";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("首页商品", TABLE_NAME);
    }

    @Override
    protected Class<HomeGoodsTempEntity> initPojoClass() {
        return HomeGoodsTempEntity.class;
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
    public List<HomeGoodsTempEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<HomeGoodsTempEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(HomeGoodsTempEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<HomeGoodsTempEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(HomeGoodsTempEntity.class, strWhere, "updatedDate asc", pageInfo);//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<HomeGoodsTempEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        try {
            return getFinalDb().findAllByWhere(HomeGoodsTempEntity.class, strWhere, "updatedDate desc", pageInfo);//降序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<HomeGoodsTempEntity> queryAll() {
        return queryAllBy(null);
    }

    public List<HomeGoodsTempEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(HomeGoodsTempEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<HomeGoodsTempEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getFinalDb().findAllByWhere(HomeGoodsTempEntity.class, strWhere, orderBy);
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public List<HomeGoodsTempEntity> syncQueryBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(HomeGoodsTempEntity.class, strWhere, "updatedDate asc");//升序
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
            return null;
        }
    }
}
