package com.mfh.enjoycity.database;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.database.dao.BaseDbDao;

import java.util.List;

/**
 * 注册用户·地址
 * Created by Nat.ZZN on 15-8-6..
 */
public class HistorySearchDao extends BaseDbDao<HistorySearchEntity, String> {

    private static final String TABLE_NAME = "history_searh";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("table_history_searh", TABLE_NAME);
    }

    @Override
    protected Class<HistorySearchEntity> initPojoClass() {
        return HistorySearchEntity.class;
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
    public List<HistorySearchEntity> queryAll(PageInfo pageInfo) {
        return queryAll(null, pageInfo);
    }

    public List<HistorySearchEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(HistorySearchEntity.class, strWhere, "createdDate desc", pageInfo);//"id desc"/asc
    }

    public void delete(String strWhere){
        getFinalDb().deleteByWhere(this.pojoClass, strWhere);
    }
}
