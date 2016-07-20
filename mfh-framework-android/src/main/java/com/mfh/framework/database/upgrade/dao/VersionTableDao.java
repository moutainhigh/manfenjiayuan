package com.mfh.framework.database.upgrade.dao;

import com.mfh.framework.database.dao.BaseDbDao;
import com.mfh.comn.bean.Pair;
import com.mfh.comn.upgrade.VersionInfo;

/**
 * 版本表dao
 * Created by Administrator on 14-6-7.
 */
public class VersionTableDao extends BaseDbDao<VersionInfo, String> {
    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("版本定义表", "T_COM_VERSION");
    }

    @Override
    protected Class<VersionInfo> initPojoClass() {
        return VersionInfo.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }
}
