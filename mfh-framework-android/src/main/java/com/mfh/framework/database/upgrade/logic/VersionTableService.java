package com.mfh.framework.database.upgrade.logic;

import com.mfh.framework.database.upgrade.dao.VersionTableDao;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.comn.upgrade.VersionInfo;

/**
 * 版本定义表服务类
 * Created by Administrator on 14-6-7.
 */
public class VersionTableService extends BaseService<VersionInfo, String, VersionTableDao> {
    @Override
    protected Class<VersionTableDao> getDaoClass() {
        return VersionTableDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

}
