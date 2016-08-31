package com.mfh.owner.dao.intro;


import com.mfh.framework.database.dao.BaseNetDao;
import com.mfh.framework.database.dao.DaoUrl;
import com.mfh.framework.network.NetProcessor;

/**
 * Created by yxm on 2014/11/26.
 */
public class IntroNetDao extends BaseNetDao {
    @Override
    protected void initUrlInfo(DaoUrl daoUrl) {

    }

    @Override
    protected Class initPojoClass() {
        return null;
    }

    @Override
    protected Class initPkClass() {
        return null;
    }

    @Override
    public void save(Object bean, NetProcessor.ComnProcessor callBack, String... factUrl) {

    }

    @Override
    public void update(Object bean, NetProcessor.ComnProcessor callBack, String... factUrl) {

    }
}
