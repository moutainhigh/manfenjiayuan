package com.mfh.litecashier.database.dao;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.litecashier.database.entity.CompanyHumanEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.database.dao.BaseDbDao;

import java.util.List;

/**
 * 公司账号管理系统
 * 主键由后台数据提供
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class CompanyHumanDao extends BaseDbDao<CompanyHumanEntity, String> {

    private static final String TABLE_NAME = "tb_company_human";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("公司账号管理系统", TABLE_NAME);
    }

    @Override
    protected Class<CompanyHumanEntity> initPojoClass() {
        return CompanyHumanEntity.class;
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
    public List<CompanyHumanEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<CompanyHumanEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(CompanyHumanEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public List<CompanyHumanEntity> queryAll() {
        return queryAllBy(null);
    }
    public List<CompanyHumanEntity> queryAllBy(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(CompanyHumanEntity.class, strWhere, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

    public List<CompanyHumanEntity> queryAllByDesc(String strWhere) {
        try {
            return getFinalDb().findAllByWhere(CompanyHumanEntity.class, strWhere, "updatedDate desc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }
}
